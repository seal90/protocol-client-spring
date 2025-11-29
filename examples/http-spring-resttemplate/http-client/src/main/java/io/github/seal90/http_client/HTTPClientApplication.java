package io.github.seal90.http_client;

import io.github.seal90.http_server.facade.HelloWorldFacade;
import io.github.seal90.protocol.client.ProtocolClient;
import io.github.seal90.protocol.client.ProtocolClientType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HTTPClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(HTTPClientApplication.class, args);
	}

//	@ProtocolClient(protocol = ProtocolClientType.HTTP_SPRING, serviceName = "httpServer", channelName = "static://http://127.0.0.1:8080")
	@ProtocolClient(protocol = ProtocolClientType.HTTP_SPRING, serviceName = "httpServer", channelName = "channel://CHANNEL-NAME")
	private HelloWorldFacade helloWorldFacade;

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			System.out.println(helloWorldFacade.helloWorld());
			System.exit(0);
		};
	}


}
