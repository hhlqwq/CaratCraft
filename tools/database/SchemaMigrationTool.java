import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** 只管理V日期版本迁移, 不会执行历史演示种子或目录清理脚本. */
public final class SchemaMigrationTool {
    /** 工具类不允许实例化. */
    private SchemaMigrationTool() {}

    /** 默认只读计划, 只有显式--apply才执行迁移并写入校验台账. */
    public static void main(String[] args) throws Exception {
        boolean apply = List.of(args).contains("--apply");
        Path directory = Path.of("database/migrations");
        for (int index = 0; index < args.length; index++) {
            if ("--dir".equals(args[index])) {
                directory = Path.of(args[++index]);
            } else if (!"--apply".equals(args[index]) && !"--plan".equals(args[index])) {
                throw new IllegalArgumentException("仅支持--plan、--apply和--dir参数");
            }
        }
        String url = System.getenv("MIGRATION_JDBC_URL");
        if (url == null
                || !url.startsWith("jdbc:mariadb://")
                || url.toLowerCase(java.util.Locale.ROOT).matches(".*[?&](password|user)=.*")) {
            throw new IllegalArgumentException("请通过环境变量配置MIGRATION_JDBC_URL, 用户与密码须单独配置");
        }
        List<Path> files;
        try (var paths = Files.list(directory)) {
            files =
                    paths.filter(
                                    path ->
                                            path.getFileName()
                                                    .toString()
                                                    .matches("V[0-9]{8}_[0-9]+_[a-z0-9_]+\\.sql"))
                            .sorted()
                            .toList();
        }
        try (Connection connection =
                DriverManager.getConnection(
                        url,
                        System.getenv("MIGRATION_JDBC_USER"),
                        System.getenv("MIGRATION_JDBC_PASSWORD"))) {
            connection.setAutoCommit(true);
            String lock =
                    "caratcraft:migration:" + digest(connection.getCatalog()).substring(0, 32);
            if (apply) {
                try (var statement = connection.prepareStatement("SELECT GET_LOCK(?,30)")) {
                    statement.setString(1, lock);
                    try (var rows = statement.executeQuery()) {
                        if (!rows.next() || rows.getInt(1) != 1) {
                            throw new IllegalStateException("无法取得迁移锁, 另一个迁移可能正在执行");
                        }
                    }
                }
                try (var statement = connection.createStatement()) {
                    statement.execute(
                            "CREATE TABLE IF NOT EXISTS CC_SCHEMA_MIGRATION (version VARCHAR(191)"
                                + " PRIMARY KEY,sha256 CHAR(64) NOT NULL,applied_at TIMESTAMP NOT"
                                + " NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");
                }
            }
            try {
                for (int index = 0; index < files.size(); index++) {
                    Path file = files.get(index);
                    String version = file.getFileName().toString();
                    String sql =
                            Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n");
                    String checksum = digest(sql);
                    String previous = recordedChecksum(connection, version);
                    if (previous != null && !previous.equals(checksum)) {
                        throw new IllegalStateException("已执行迁移的校验值发生变化, 请新增版本而不是修改旧版本: " + version);
                    }
                    System.out.printf(
                            "[%d/%d] %s: %s%n",
                            index + 1,
                            files.size(),
                            version,
                            previous != null ? "已执行且校验一致" : apply ? "开始执行" : "待执行");
                    if (previous != null || !apply) {
                        continue;
                    }
                    for (String statement : splitStatements(sql)) {
                        try (var command = connection.createStatement()) {
                            command.execute(statement);
                        }
                    }
                    try (var command =
                            connection.prepareStatement(
                                    "INSERT INTO CC_SCHEMA_MIGRATION(version,sha256) VALUES"
                                            + " (?,?)")) {
                        command.setString(1, version);
                        command.setString(2, checksum);
                        command.executeUpdate();
                    }
                }
            } finally {
                if (apply) {
                    try (var statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
                        statement.setString(1, lock);
                        statement.execute();
                    }
                }
            }
            System.out.println(apply ? "版本迁移完成, 请继续检查服务就绪状态" : "只读计划完成, 未修改数据库");
        }
    }

    /** 读取已执行版本, 未建立迁移台账时只读计划视为全部待执行. */
    private static String recordedChecksum(Connection connection, String version) throws Exception {
        try (var statement =
                connection.prepareStatement(
                        "SELECT sha256 FROM CC_SCHEMA_MIGRATION WHERE version=?")) {
            statement.setString(1, version);
            try (var rows = statement.executeQuery()) {
                return rows.next() ? rows.getString(1) : null;
            }
        } catch (java.sql.SQLException error) {
            if ("42S02".equals(error.getSQLState())) {
                return null;
            }
            throw error;
        }
    }

    /** 对源文件UTF-8内容计算校验值, 禁止静默覆盖已执行脚本. */
    private static String digest(String value) throws Exception {
        return HexFormat.of()
                .formatHex(
                        MessageDigest.getInstance("SHA-256")
                                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    /** 按引号和注释边界拆分普通SQL, 明确拒绝客户端SOURCE和DELIMITER语法. */
    private static List<String> splitStatements(String sql) {
        if (sql.contains("/*!")) {
            throw new IllegalArgumentException("版本迁移不支持MySQL可执行注释, 请使用显式SQL语句");
        }
        if (sql.matches("(?s).*(?m)^\\s*(?i:SOURCE|DELIMITER)\\b.*")) {
            throw new IllegalArgumentException("版本迁移不支持SOURCE或DELIMITER, 请使用独立普通SQL版本");
        }
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int index = 0; index < sql.length(); index++) {
            char value = sql.charAt(index);
            char next = index + 1 < sql.length() ? sql.charAt(index + 1) : 0;
            if (lineComment) {
                if (value == '\n') {
                    lineComment = false;
                    current.append('\n');
                }
            } else if (blockComment) {
                if (value == '*' && next == '/') {
                    blockComment = false;
                    index++;
                    current.append(' ');
                }
            } else if (quote != 0) {
                current.append(value);
                if (value == '\\' && quote != '`' && next != 0) {
                    current.append(next);
                    index++;
                } else if (value == quote) {
                    if (next == quote) {
                        current.append(next);
                        index++;
                    } else {
                        quote = 0;
                    }
                }
            } else if (value == '#'
                    || (value == '-'
                            && next == '-'
                            && index + 2 < sql.length()
                            && Character.isWhitespace(sql.charAt(index + 2)))) {
                lineComment = true;
            } else if (value == '/' && next == '*') {
                blockComment = true;
                index++;
            } else if (value == '\'' || value == '"' || value == '`') {
                quote = value;
                current.append(value);
            } else if (value == ';') {
                if (!current.toString().isBlank()) {
                    result.add(current.toString().trim());
                }
                current.setLength(0);
            } else {
                current.append(value);
            }
        }
        if (quote != 0 || blockComment) {
            throw new IllegalArgumentException("迁移包含未闭合引号或注释");
        }
        if (!current.toString().isBlank()) {
            result.add(current.toString().trim());
        }
        return result;
    }
}
