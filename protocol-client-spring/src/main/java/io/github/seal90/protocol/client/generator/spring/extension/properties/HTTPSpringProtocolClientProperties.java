package io.github.seal90.protocol.client.generator.spring.extension.properties;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HTTPSpringProtocolClientProperties {

  private String[] forwardWebHeaders;

  private String defaultChannelName;

  private Map<String, HTTPSpringProtocolClientChannelProperties> channels = new HashMap<>();

  private Map<String, HTTPSpringProtocolClientServiceProperties> services = new HashMap<>();

}
