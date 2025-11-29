package io.github.seal90.feign_client;

import io.github.seal90.feign_server.facade.HelloWorldFacade;
import io.github.seal90.protocol.client.ProtocolClient;
import io.github.seal90.protocol.client.ProtocolClientType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FeignClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeignClientApplication.class, args);
	}

	@ProtocolClient(protocol = ProtocolClientType.HTTP_FEIGN, serviceName = "feignServer")
	private HelloWorldFacade helloWorldFacade;

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			System.out.println(helloWorldFacade.helloWorld());
			System.exit(0);
		};
	}


}
