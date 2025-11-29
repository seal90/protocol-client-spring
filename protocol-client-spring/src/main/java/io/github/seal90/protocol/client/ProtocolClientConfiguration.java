package io.github.seal90.protocol.client;

import feign.Feign;
import io.github.seal90.protocol.client.generator.FeignProtocolClientTypeGenerator;
import io.github.seal90.protocol.client.generator.GrpcProtocolClientTypeGenerator;
import io.github.seal90.protocol.client.generator.HTTPSpringProtocolClientTypeGenerator;
import io.github.seal90.protocol.client.generator.spring.extension.RestTemplateHttpExchangeAdapterFactory;
import io.github.seal90.protocol.client.generator.spring.extension.WebClientHttpExchangeAdapterFactory;
import io.github.seal90.protocol.client.generator.spring.extension.headerforward.ForwardWebHeaderExchangeFilterFunction;
import io.github.seal90.protocol.client.generator.spring.extension.headerforward.ForwardWebHeaderRequestInterceptor;
import io.github.seal90.protocol.client.generator.spring.extension.headerforward.ServerHttpRequestContextWebFilter;
import io.github.seal90.protocol.client.generator.spring.extension.nameresovle.RestTemplateNameResolveRequestInterceptor;
import io.github.seal90.protocol.client.generator.spring.extension.nameresovle.WebClientNameResolveExchangeFilterFunction;
import io.grpc.Grpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol client configuration
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HTTPSpringProtocolClientProperties.class)
public class ProtocolClientConfiguration {

  @Bean
  @ConditionalOnClass(Feign.class)
  public ProtocolClientTypeGenerator feignProtocolClientTypeGenerator() {
    return new FeignProtocolClientTypeGenerator();
  }

  @Bean
  @ConditionalOnClass(Grpc.class)
  public ProtocolClientTypeGenerator grpcProtocolClientTypeGenerator() {
    return new GrpcProtocolClientTypeGenerator();
  }


  @ConditionalOnClass(RestTemplate.class)
  public static class BlockingProtocolClientConfig {

    @Bean
    public RestTemplateCustomizer forwardWebHeaderRequestInterceptor(HTTPSpringProtocolClientProperties protocolClientProperties) {
      return (restTemplate) -> {
        List<ClientHttpRequestInterceptor> list = new ArrayList<>(restTemplate.getInterceptors());
        list.add(new ForwardWebHeaderRequestInterceptor(protocolClientProperties.getForwardWebHeaders()));
        restTemplate.setInterceptors(list);
      };
    }

    @Bean
    public RestTemplateCustomizer restTemplateNameResolveRequestInterceptor(HTTPSpringProtocolClientProperties protocolClientProperties) {
      return (restTemplate) -> {
        List<ClientHttpRequestInterceptor> list = new ArrayList<>(restTemplate.getInterceptors());
        list.add(new RestTemplateNameResolveRequestInterceptor(protocolClientProperties));
        restTemplate.setInterceptors(list);
      };
    }

    @Bean
    @ConditionalOnMissingBean(ProtocolClientTypeGenerator.class)
    public ProtocolClientTypeGenerator restTemplateProtocolClientTypeGenerator(ApplicationContext applicationContext, HTTPSpringProtocolClientProperties protocolClientProperties) {
      return new HTTPSpringProtocolClientTypeGenerator(new RestTemplateHttpExchangeAdapterFactory(applicationContext));
    }
  }

  @ConditionalOnClass(WebClient.class)
  public static class ReactiveProtocolClientConfig {

    @Bean
    @ConditionalOnClass(WebFilter.class)
    public WebFilter ServerHttpRequestContextWebFilter() {
      return new ServerHttpRequestContextWebFilter();
    }

    @Bean
    public WebClientCustomizer webClientNameResolveExchangeFilterFunction(HTTPSpringProtocolClientProperties protocolClientProperties) {
      return (builder) -> {
        builder.filter(new WebClientNameResolveExchangeFilterFunction(protocolClientProperties));
        builder.filter(new ForwardWebHeaderExchangeFilterFunction(protocolClientProperties.getForwardWebHeaders()));
      };
    }

    @Bean
    @ConditionalOnMissingBean(ProtocolClientTypeGenerator.class)
    public ProtocolClientTypeGenerator webProtocolClientTypeGenerator(ApplicationContext applicationContext) {
      return new HTTPSpringProtocolClientTypeGenerator(new WebClientHttpExchangeAdapterFactory(applicationContext));
    }
  }
}
