//@formatter:off
//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.vertx:vertx-web:3.9.1
//@formatter:on
import java.util.function.Consumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;


public class server extends AbstractVerticle {

  private static final int LISTEN_PORT_8080 = 8080;

  public static void main(String[] args) {
    String verticleID = server.class.getName();
    VertxOptions options = new VertxOptions();
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    Consumer<Vertx> runner = vertx -> {
      try {
        vertx.deployVerticle(verticleID, deploymentOptions, h -> {
          if (h.succeeded()) {
            System.out.println("Server listening on port " + LISTEN_PORT_8080);
          }
        });
      } catch (Throwable t) {
        t.printStackTrace();
      }
    };

    Vertx vertx = Vertx.vertx(options);
    runner.accept(vertx);
  }

  @Override
  public void start() throws Exception {

    Router router = Router.router(vertx);

    router.route("/live").handler(routingContext -> {
      routingContext.response().putHeader("content-type", "text/plain")
          .end("OK");
    });

    router.route().handler(routingContext -> {
      routingContext.response().putHeader("content-type", "text/html")
          .end("Hello World JBang!");
    });

    vertx.createHttpServer().requestHandler(router).listen(LISTEN_PORT_8080);
  }
}
