package io.github.seal90.http_server.facade;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/prefix")
public interface HelloWorldFacade {

  @GetExchange("/helloWorld")
  String helloWorld();

}
