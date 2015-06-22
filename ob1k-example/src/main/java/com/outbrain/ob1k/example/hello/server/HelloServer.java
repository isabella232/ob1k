package com.outbrain.ob1k.example.hello.server;

import com.outbrain.ob1k.example.hello.server.services.HelloService;
import com.outbrain.ob1k.server.Server;
import com.outbrain.ob1k.server.build.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author marenzon
 */
public class HelloServer {

  private static final Logger logger = LoggerFactory.getLogger(HelloServer.class);

  public static final int PORT = 8080;
  public static final String CTX_PATH = "/services";
  public static final String HELLO_SERVICE_PATH = "/hello";

  Server server;

  public static void main(final String[] args) {
    new HelloServer().start(PORT);
  }

  public void start(final int port) {
    server = buildServer(port);
    server.start();
    logger.info("## HelloServer is started on port: {} ##", port);
  }

  public void stop() {
    if (server != null)
      server.stop();
  }

  private Server buildServer(final int port) {
    return ServerBuilder.newBuilder().
            configurePorts(builder -> builder.setPort(port)).
            setContextPath(CTX_PATH).
            withServices(builder -> builder.addService(new HelloService(), HELLO_SERVICE_PATH)).
            configureExtraParams(builder -> builder.setRequestTimeout(50, TimeUnit.MILLISECONDS)).
            build();
  }
}