package io.github.seal90.protocol.client.generator.spring.extension;

import org.springframework.web.service.invoker.HttpExchangeAdapter;

public interface HttpExchangeAdapterFactory {

    public static final String NAME_RESOLVED_FLAG = "PROTOCOL_CLIENT_NAME_RESOLVED";

    HttpExchangeAdapter create(String serviceName, String channelName, String[] interceptors);
}