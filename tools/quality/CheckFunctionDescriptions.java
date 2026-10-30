import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/** 只读检查Java具名方法与构造函数的中文职责, 继承方法沿用接口契约. */
public final class CheckFunctionDescriptions {
    /** 禁止实例化只提供命令行验证入口的工具. */
    private CheckFunctionDescriptions() {}

    /** 从明确的工作区收集自有源码, 解析语法树但不生成类文件或运行注解处理器. */
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) {
            throw new IllegalArgumentException("必须提供唯一工作区根目录");
        }
        Path root = Path.of(arguments[0]).toAbsolutePath().normalize();
        List<Path> paths = new ArrayList<>();
        for (String directory : List.of("backend", "tools", "deploy")) {
            Path owned = root.resolve(directory);
            if (!Files.isDirectory(owned)) {
                continue;
            }
            try (var files = Files.walk(owned)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> !containsGeneratedDirectory(root.relativize(path)))
                        .forEach(paths::add);
            }
        }
        if (paths.isEmpty()) {
            throw new IllegalStateException("未找到可验证的Java源码");
        }
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Java职责检查需要JDK, 不能使用仅含运行时的JRE");
        }
        AtomicInteger missing = new AtomicInteger();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var manager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            var task =
                    (JavacTask)
                            compiler.getTask(
                                    null,
                                    manager,
                                    diagnostics,
                                    List.of("-proc:none"),
                                    null,
                                    manager.getJavaFileObjectsFromPaths(paths));
            DocTrees trees = DocTrees.instance(task);
            for (CompilationUnitTree unit : task.parse()) {
                new TreePathScanner<Void, Void>() {
                    /** 检查普通声明, 重写方法的额外副作用仍由接口契约审阅负责. */
                    @Override
                    public Void visitMethod(MethodTree method, Void unused) {
                        boolean inherited =
                                method.getModifiers().getAnnotations().stream()
                                        .anyMatch(
                                                annotation ->
                                                        annotation
                                                                        .getAnnotationType()
                                                                        .toString()
                                                                        .equals("Override")
                                                                || annotation
                                                                        .getAnnotationType()
                                                                        .toString()
                                                                        .equals(
                                                                                "java.lang.Override"));
                        String comment = trees.getDocComment(getCurrentPath());
                        if (!inherited
                                && (comment == null
                                        || !comment.matches("(?s).*[\\u4e00-\\u9fff].*"))) {
                            long line =
                                    unit.getLineMap()
                                            .getLineNumber(
                                                    trees.getSourcePositions()
                                                            .getStartPosition(unit, method));
                            System.err.println(
                                    unit.getSourceFile().getName()
                                            + ":"
                                            + line
                                            + ": "
                                            + method.getName()
                                            + "需要中文职责注释");
                            missing.incrementAndGet();
                        }
                        return super.visitMethod(method, unused);
                    }
                }.scan(unit, null);
            }
        }
        if (diagnostics.getDiagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)) {
            throw new IllegalStateException("Java源码存在语法错误, 职责检查不能作为通过结果");
        }
        if (missing.get() > 0) {
            System.exit(1);
        }
        System.out.println("Java职责检查通过: " + paths.size() + "个源码文件, 继承契约仍须人工审阅");
    }

    /** 排除构建产物目录, 防止把生成源码作为人工维护对象重复检查. */
    private static boolean containsGeneratedDirectory(Path relative) {
        for (Path part : relative) {
            if (List.of("target", ".tools", "node_modules", "dist").contains(part.toString())) {
                return true;
            }
        }
        return false;
    }
}
