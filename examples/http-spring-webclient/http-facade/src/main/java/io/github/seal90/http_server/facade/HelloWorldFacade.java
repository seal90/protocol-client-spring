package io.github.seal90.http_server.facade;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/prefix")
public interface HelloWorldFacade {

  @GetExchange("/helloWorld")
  Mono<String> helloWorld();

}
