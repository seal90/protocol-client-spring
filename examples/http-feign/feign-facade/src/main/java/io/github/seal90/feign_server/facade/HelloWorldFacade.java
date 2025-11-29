package io.github.seal90.feign_server.facade;

import org.springframework.web.bind.annotation.GetMapping;

public interface HelloWorldFacade {

  @GetMapping("/helloWorld")
  String helloWorld();

}
