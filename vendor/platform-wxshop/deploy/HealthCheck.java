import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** 使用JRE自带HTTP客户端执行容器就绪检查, 不依赖额外curl软件包. */
public final class HealthCheck {
  /** 工具类不允许实例化. */
  private HealthCheck() {}

  /** 仅HTTP200视为就绪, 不输出可能包含服务信息的响应正文. */
  public static void main(String[] args) {
    try {
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
      HttpRequest request = HttpRequest.newBuilder(URI.create(args[0]))
          .timeout(Duration.ofSeconds(5)).GET().build();
      int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
      System.exit(status == 200 ? 0 : 1);
    } catch (Exception error) {
      System.exit(1);
    }
  }
}
