package io.github.seal90.http_client;

import io.github.seal90.protocol.client.ProtocolClientAnnotationBeanPostProcessor;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import java.util.List;

@Configuration
public class HTTPConfiguration {

  @Bean
  public WebClientCustomizer requestInterceptor() {
    return builder -> builder.filter((ClientRequest request, ExchangeFunction next) -> {
      String serviceName = (String)request.attribute(ProtocolClientAnnotationBeanPostProcessor.SERVICE_NAME).get();
      System.out.println("serviceName: "+serviceName);
      return next.exchange(request);
    });
  }
}
