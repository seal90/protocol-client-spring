package io.github.seal90.protocol.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ProtocolClient
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolClient {

  /**
   * protocol
   * @return protocol
   */
  String protocol() default ProtocolClientType.GRPC_SPRING;

  /**
   * serviceName
   * @return serviceName
   */
  String serviceName();

  /**
   * channelName
   * @return channelName
   */
  String channelName() default "";

  /**
   * interceptors
   * @return Interceptors related to protocol type
   */
  String[] interceptors() default {};
}
