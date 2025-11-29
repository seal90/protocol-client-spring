package io.github.seal90.protocol.client.generator.spring.extension.properties;

import lombok.Data;

@Data
public class HTTPSpringProtocolClientServiceProperties {

  private String channelName;

  private HTTPSpringProtocolClientChannelProperties channelConfig;
}
