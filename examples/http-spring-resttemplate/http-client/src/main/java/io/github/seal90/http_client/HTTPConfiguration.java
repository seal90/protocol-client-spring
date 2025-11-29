package io.github.seal90.http_client;

import io.github.seal90.protocol.client.ProtocolClientAnnotationBeanPostProcessor;
import org.springframework.boot.web.client.RestTemplateRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

@Configuration
public class HTTPConfiguration {

  @Bean
  public RestTemplateRequestCustomizer printNameRestTemplateRequestCustomizer() {
    return request -> {
      Map<String, Object> attributes = request.getAttributes();
      Object serviceName = attributes.get(ProtocolClientAnnotationBeanPostProcessor.SERVICE_NAME);
      System.out.println("serviceName: "+serviceName);
    };
  }
}
