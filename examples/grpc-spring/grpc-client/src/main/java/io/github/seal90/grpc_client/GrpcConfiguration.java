package io.github.seal90.grpc_client;

import io.github.seal90.protocol.client.generator.GrpcProtocolClientTypeGenerator;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GlobalClientInterceptor;

@Configuration
public class GrpcConfiguration {

  @Bean
  @GlobalClientInterceptor
  public ClientInterceptor namePassingClientInterceptor() {
    return new ClientInterceptor() {
      @Override
      public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                 CallOptions callOptions, Channel next) {

        String name = callOptions.getOption(GrpcProtocolClientTypeGenerator.SERVICE_NAME_KEY);

        String proxyChannelName = callOptions
            .getOption(GrpcProtocolClientTypeGenerator.CHANNEL_NAME_KEY);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(
            next.newCall(method, callOptions)) {
          @Override
          public void start(Listener responseListener, Metadata headers) {

            // Passing serviceName, if necessary you can use
            // proxyChannelName to judge and use different keys to
            // pass data
            headers.put(Metadata.Key.of("USER_DEFINE_NAME", Metadata.ASCII_STRING_MARSHALLER),
                name);

            super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                responseListener) {
              @Override
              public void onHeaders(Metadata headers) {
                String instance = headers.get(Metadata.Key.of("USER_DEFINE_INSTANCE_NAME",
                    Metadata.ASCII_STRING_MARSHALLER));
                System.out.println("Received instance name: " + instance);
                super.onHeaders(headers);
              }
            }, headers);
          }
        };
      }
    };
  }
}
