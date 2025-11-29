package io.github.seal90.protocol.client.generator.spring.extension.properties;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HTTPSpringProtocolClientChannelProperties {

  private String address;

  private List<String> addresses = new ArrayList<>();
}
