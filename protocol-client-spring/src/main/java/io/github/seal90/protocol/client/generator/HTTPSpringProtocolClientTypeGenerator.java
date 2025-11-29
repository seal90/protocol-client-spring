package io.github.seal90.protocol.client.generator;

import io.github.seal90.protocol.client.ProtocolClient;
import io.github.seal90.protocol.client.ProtocolClientType;
import io.github.seal90.protocol.client.ProtocolClientTypeGenerator;
import io.github.seal90.protocol.client.generator.spring.extension.HttpExchangeAdapterFactory;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.lang.reflect.Member;

public class HTTPSpringProtocolClientTypeGenerator implements ProtocolClientTypeGenerator {

  private HttpExchangeAdapterFactory httpExchangeAdapterFactory;

  public HTTPSpringProtocolClientTypeGenerator(HttpExchangeAdapterFactory httpExchangeAdapterFactory) {
    this.httpExchangeAdapterFactory = httpExchangeAdapterFactory;
  }

  @Override
  public <T> T generate(Member injectionTarget, Class<T> injectionType, ProtocolClient annotation) {
    final String serviceName = annotation.serviceName();
    final String channelName = annotation.channelName();
    final String[] interceptors = annotation.interceptors();

    HttpExchangeAdapter adapter = httpExchangeAdapterFactory.create(serviceName, channelName, interceptors);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(injectionType);
  }

  @Override
  public String supportProtocol() {
    return ProtocolClientType.HTTP_SPRING;
  }

}