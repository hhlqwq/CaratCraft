import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/** 在隔离审查库验证替代JDBC驱动, 不修改真实商品、用户或订单. */
public class JdbcCopyrightProbe {
    /** 执行连接、字符集、预编译、事务和分页验证, 密码只从环境读取. */
    public static void main(String[] args) throws Exception {
        String url =
                "jdbc:mariadb://127.0.0.1:3306/caratcraft_copyright_audit_20261014"
                        + "?sslMode=disable&connectionTimeZone=Asia/Shanghai";
        if (Boolean.getBoolean("caratcraft.verify.override")) {
            String location =
                    DriverManager.getDriver(url)
                            .getClass()
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toString();
            check(
                    !location.contains("BOOT-INF") && location.contains("mariadb-java-client"),
                    "外部库替换机制");
        }
        if (Boolean.getBoolean("caratcraft.verify.override.only")) {
            return;
        }
        String password = System.getenv("CARATCRAFT_AUDIT_DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("缺少隔离数据库连接密码");
        }
        try (Connection connection = DriverManager.getConnection(url, "root", password);
                Statement statement = connection.createStatement()) {
            check(connection.getMetaData().getDriverName().contains("MariaDB"), "替代驱动加载");
            statement.execute(
                    "CREATE TEMPORARY TABLE audit_jdbc_probe "
                            + "(id INT PRIMARY KEY, content VARCHAR(100)) CHARACTER SET utf8mb4");
            connection.setAutoCommit(false);
            try (PreparedStatement insert =
                    connection.prepareStatement(
                            "INSERT INTO audit_jdbc_probe(id,content) VALUES (?,?)")) {
                insert.setInt(1, 1);
                insert.setString(2, "中文与表情字符 💎");
                insert.executeUpdate();
                connection.rollback();
                try (ResultSet count =
                        statement.executeQuery("SELECT COUNT(*) FROM audit_jdbc_probe")) {
                    count.next();
                    check(count.getInt(1) == 0, "事务回滚");
                }
                for (int id = 1; id <= 3; id++) {
                    insert.setInt(1, id);
                    insert.setString(2, "中文与表情字符 💎");
                    insert.executeUpdate();
                }
                connection.commit();
            }
            try (PreparedStatement page =
                    connection.prepareStatement(
                            "SELECT id,content FROM audit_jdbc_probe ORDER BY id LIMIT ? OFFSET"
                                    + " ?")) {
                page.setInt(1, 1);
                page.setInt(2, 1);
                try (ResultSet result = page.executeQuery()) {
                    check(result.next() && result.getInt(1) == 2, "预编译分页");
                    check("中文与表情字符 💎".equals(result.getString(2)), "UTF8MB4字符集");
                }
            }
            try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM mall_goods")) {
                result.next();
                check(result.getInt(1) == 3, "新库项目演示商品查询");
            }
            connection.rollback();
            System.out.println("JDBC连接、回滚、提交、预编译分页、字符集和新库查询全部通过.");
        }
    }

    /** 失败时只输出验证项目名称, 不输出连接秘密. */
    private static void check(boolean valid, String name) {
        if (!valid) {
            throw new IllegalStateException("验证未通过: " + name);
        }
        System.out.println("验证通过: " + name);
    }
}
