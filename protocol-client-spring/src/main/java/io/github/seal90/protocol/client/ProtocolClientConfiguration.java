package io.github.seal90.protocol.client;

import io.github.seal90.protocol.client.generator.FeignProtocolClientTypeGenerator;
import io.github.seal90.protocol.client.generator.GrpcProtocolClientTypeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Protocol client configuration
 */
@Configuration(proxyBeanMethods = false)
public class ProtocolClientConfiguration {

  @Bean
  public ProtocolClientTypeGenerator feignProtocolClientTypeGenerator() {
    return new FeignProtocolClientTypeGenerator();
  }

  @Bean
  public ProtocolClientTypeGenerator grpcProtocolClientTypeGenerator() {
    return new GrpcProtocolClientTypeGenerator();
  }

}
