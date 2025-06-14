package io.github.seal90.protocol.client;

import java.lang.reflect.Member;

/**
 * Protocol client type generator
 */
public interface ProtocolClientTypeGenerator {

  /**
   * Generate protocol client
   * @param injectionTarget injection target
   * @param injectionType injection type
   * @param annotation ProtocolClient
   * @return client
   * @param <T> client type
   */
  <T> T generate(final Member injectionTarget, final Class<T> injectionType, final ProtocolClient annotation);

  /**
   * Support protocol
   * @return Support protocol name
   */
  String supportProtocol();
}
