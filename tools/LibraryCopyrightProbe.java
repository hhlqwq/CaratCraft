import java.io.StringWriter;
import java.util.List;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

/** 验证收敛旧依赖链后仍可使用模板引擎和XML解析, 不调用外部服务. */
public class LibraryCopyrightProbe {
  /** 使用实际发布JAR中的类完成替代兼容检查. */
  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    properties.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    Velocity.init(properties);
    VelocityContext context = new VelocityContext();
    context.put("name", "项目模板");
    context.put("items", List.of("甲", "乙"));
    StringWriter writer = new StringWriter();
    boolean rendered = Velocity.evaluate(context, writer, "copyright-probe",
        "$name:#foreach($item in $items)$item#end");
    if (!rendered || !"项目模板:甲乙".equals(writer.toString())) {
      throw new IllegalStateException("模板引擎兼容检查失败");
    }
    Document document = DocumentHelper.parseText("<root><item>中文内容</item></root>");
    if (!"中文内容".equals(document.getRootElement().elementText("item"))) {
      throw new IllegalStateException("XML解析兼容检查失败");
    }
    System.out.println("验证通过: 实际发布JAR中的Velocity模板和dom4j解析.");
  }
}
