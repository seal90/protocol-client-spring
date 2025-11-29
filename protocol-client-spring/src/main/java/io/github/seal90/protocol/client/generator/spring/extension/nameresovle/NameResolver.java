package io.github.seal90.protocol.client.generator.spring.extension.nameresovle;

import io.github.seal90.protocol.client.generator.spring.extension.properties.HTTPSpringProtocolClientChannelProperties;
import io.github.seal90.protocol.client.generator.spring.extension.properties.HTTPSpringProtocolClientProperties;
import io.github.seal90.protocol.client.generator.spring.extension.properties.HTTPSpringProtocolClientServiceProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Random;

public class NameResolver {

  @Data
  @AllArgsConstructor
  public static class Result {
    private Boolean nameResolved;
    private String resolvedAddress;
  }

  public static Result resolve(String serviceName, String channelName, HTTPSpringProtocolClientProperties protocolClientProperties) {
    boolean nameResolved = true;
    String resolvedAddress = null;

    if("".equals(channelName)) {
      resolvedAddress = resolveByServiceName(serviceName, channelName, protocolClientProperties);
      nameResolved = resolvedAddress != null;
    } else {
      if (channelName.startsWith("static://")) {
        resolvedAddress = channelName.replaceFirst("static://", "");
      } else if (channelName.startsWith("channel://")) {
        resolvedAddress = parseByChannelName(channelName.replaceFirst("channel://", ""), protocolClientProperties);
      } else if (channelName.startsWith("default://")) {
        resolvedAddress = parseDefaultChannel(protocolClientProperties);
      } else if (channelName.startsWith("lb://")) {
        resolvedAddress = "http://" + channelName.replaceFirst("lb://", "");
        nameResolved = false;
      }
    }

    return new Result(nameResolved, resolvedAddress);
  }

  private static String resolveByServiceName(String serviceName, String channelName, HTTPSpringProtocolClientProperties protocolClientProperties) {
    String parsedAddress = null;
    HTTPSpringProtocolClientServiceProperties serviceProperties = protocolClientProperties.getServices().get(serviceName);
    if(serviceProperties != null) {
      String resolvedChannelName = serviceProperties.getChannelName();
      if(resolvedChannelName != null) {
        parsedAddress = parseByChannelName(resolvedChannelName, protocolClientProperties);
        if(parsedAddress == null) {
          throw new RuntimeException("Parsed "+serviceName+" by channelName: " + resolvedChannelName + " fail.");
        }
      } else {
        HTTPSpringProtocolClientChannelProperties channelProperties = serviceProperties.getChannelConfig();
        parsedAddress = parseChannel(channelProperties);
        if(parsedAddress == null) {
          throw new RuntimeException("Parsed "+serviceName+" by channel config fail.");
        }
      }
    } else {
      String defaultChannelName = protocolClientProperties.getDefaultChannelName();
      if(defaultChannelName != null) {
        HTTPSpringProtocolClientChannelProperties channelProperties = protocolClientProperties.getChannels().get(defaultChannelName);
        parsedAddress = parseChannel(channelProperties);
      }
    }
    return parsedAddress;
  }

  private static String parseByChannelName(String channelName, HTTPSpringProtocolClientProperties protocolClientProperties) {
    HTTPSpringProtocolClientChannelProperties channelProperties = protocolClientProperties.getChannels().get(channelName);
    return parseChannel(channelProperties);
  }

  private static String parseDefaultChannel(HTTPSpringProtocolClientProperties protocolClientProperties) {
    String defaultChannelName = protocolClientProperties.getDefaultChannelName();
    if(defaultChannelName != null) {
      HTTPSpringProtocolClientChannelProperties channelProperties = protocolClientProperties.getChannels().get(defaultChannelName);
      String parsedAddress = parseChannel(channelProperties);
      if(parsedAddress == null) {
        throw new RuntimeException("Parsed defaultChannelName "+defaultChannelName+" fail.");
      }
      return parsedAddress;
    }
    return null;
  }

  private static String parseChannel(HTTPSpringProtocolClientChannelProperties channelProperties) {
    String parsedAddress = null;
    if(channelProperties != null) {
      String address = channelProperties.getAddress();
      if(address != null) {
        parsedAddress = address;
      } else {
        List<String> addresses = channelProperties.getAddresses();
        if(!addresses.isEmpty()) {
          parsedAddress = addresses.get(new Random().nextInt(addresses.size()));
        }
      }
    }
    return parsedAddress;
  }

}
