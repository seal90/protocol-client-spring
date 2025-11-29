package io.github.seal90.http_server;

import io.github.seal90.http_server.facade.HelloWorldFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class HelloWorldFacadeImpl implements HelloWorldFacade {

  @Override
  public String helloWorld() {
    HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
    String serviceName = request.getHeader("PROTOCOL_CLIENT_SERVICE_NAME");
    System.out.println("serviceName: "+serviceName);
    return "Hello World";
  }
}
