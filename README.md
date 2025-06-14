# Protocol client

Welcome to the Protocol client project!

The Protocol client project provides a [ProtocolClient.java](protocol-client-spring/src/main/java/io/github/seal90/protocol/client/ProtocolClient.java) for quickly initialize the client of different protocols, like gRPC, HTTP. 

# Getting Started

Calling the target service in a service requires specifying the service name, channel, communication content, and additional information of the target service.
* serviceName: The name of the service to be called. The service name is usually the same as the channel.
* channel: The channel to be used to call the target service. Including address, timeout, encode, decode, etc.
* communication content: Agree on a fixed format of data.
* additional information: For example, the data required for the transfer protocol (HTTP METHOD Path); The name of the target service that the server discovers the required target service.

```java
    // ProtocolClient protocol ProtocolClient
    // ProtocolClient define serviceName for serviceName, channelName for channel, interceptors for additional information
	@ProtocolClient(serviceName = "HelloWorldService", channelName = "HelloWorldServiceChannel", interceptors = {})
    // HelloWorldServiceGrpc.HelloWorldServiceBlockingStub for communication content
	private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub stub;
```
# Example
* HTTP Example: [FeignClientApplication.java](examples/feign/feign-client/src/main/java/io/github/seal90/feign_client/FeignClientApplication.java)
* gRPC Example: [GrpcClientApplication.java](examples/grpc/grpc-client/src/main/java/io/github/seal90/grpc_client/GrpcClientApplication.java)