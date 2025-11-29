package io.github.seal90.feign_client;

import feign.RequestInterceptor;
import io.github.seal90.protocol.client.ProtocolClientAnnotationBeanPostProcessor;
import io.github.seal90.protocol.client.generator.feign.extension.GlobalRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Map;

@Configuration
public class FeignConfiguration {

  @Bean
  @GlobalRequestInterceptor
  public RequestInterceptor requestInterceptor() {
    return new RequestInterceptor() {
      @Override
      public void apply(feign.RequestTemplate requestTemplate) {
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Collection<String> serviceName = headers.get(ProtocolClientAnnotationBeanPostProcessor.SERVICE_NAME);
        System.out.println("serviceName: "+serviceName);
      }
    };
  }
}
