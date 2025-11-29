package io.github.seal90.http_server;

import io.github.seal90.http_server.facade.HelloWorldFacade;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class HelloWorldFacadeImpl implements HelloWorldFacade {

  @Override
  public Mono<String> helloWorld() {
    return ReactiveRequestContextHolder.getRequest().map(req -> {
      HttpHeaders httpHeaders = req.getHeaders();
      List<String> serviceNames = httpHeaders.get("PROTOCOL_CLIENT_SERVICE_NAME");
      System.out.println("serviceName: "+serviceNames);
      return "Hello World";
    });
  }
}
