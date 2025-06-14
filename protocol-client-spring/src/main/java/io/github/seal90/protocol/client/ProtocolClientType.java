package io.github.seal90.protocol.client;

/**
 * Protocol client type
 */
public interface ProtocolClientType {

  /**
   * Grpc protocol implement by spring-grpc
   */
  public static final String GRPC_SPRING= "GRPC_SPRING";

  /**
   * Http protocol implement by feign
   */
  public static final String HTTP_FEIGN = "HTTP_FEIGN";
}
