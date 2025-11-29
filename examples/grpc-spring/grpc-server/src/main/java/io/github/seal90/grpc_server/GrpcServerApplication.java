package io.github.seal90.grpc_server;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.GlobalServerInterceptor;

@SpringBootApplication
public class GrpcServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcServerApplication.class, args);
	}


	@Bean
	@GlobalServerInterceptor
	public ServerInterceptor serverInterceptor() {
		return new ServerInterceptor() {
			@Override
			public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
																																	 Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
				// If necessary, the value passed by the client
				String name = metadata.get(Metadata.Key.of("USER_DEFINE_NAME", Metadata.ASCII_STRING_MARSHALLER));
				System.out.println("Received user define name: " + name);

				ServerCall<ReqT, RespT> newServerCall = new ForwardingServerCall.SimpleForwardingServerCall<>(
						serverCall) {

					@Override
					public void sendHeaders(Metadata headers) {
						// If necessary, values can be passed to the client.
						headers.put(Metadata.Key.of("USER_DEFINE_INSTANCE_NAME", Metadata.ASCII_STRING_MARSHALLER),
								"ONE");
						super.sendHeaders(headers);
					}
				};
				return serverCallHandler.startCall(newServerCall, metadata);
			}
		};
	}
}
