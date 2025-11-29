# Protocol client

Welcome to the Protocol Client project!

This project provides a unified and easy-to-use client annotation—[ProtocolClient.java](protocol-client-spring/src/main/java/io/github/seal90/protocol/client/ProtocolClient.java)—that simplifies initializing clients for multiple protocols, such as gRPC, HTTP, and more. With minimal configuration, you can seamlessly interact with services across different communication protocols.

# Getting Started

To invoke a target service, the caller must specify the following elements:

* Service Name: The logical name of the target service. Typically aligns with the communication channel name and is used for service discovery.
* Channel Configuration: Defines how the call is made, including endpoint address, timeout settings, and serialization/deserialization (encode/decode) strategies.
* Communication Payload: The actual data exchanged, structured in a pre-agreed format (e.g., JSON, Protobuf).
* Additional Metadata: Protocol-specific or routing-related information—such as HTTP method and path, authentication tokens, or contextual hints used by the service registry to locate the correct instance of the target service

```java
    // ProtocolClient protocol ProtocolClient
    // ProtocolClient define serviceName for serviceName, channelName for channel, interceptors for additional information
	@ProtocolClient(serviceName = "HelloWorldService", channelName = "HelloWorldServiceChannel", interceptors = {})
    // HelloWorldServiceGrpc.HelloWorldServiceBlockingStub for communication content
	private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub stub;
```
# Example
* HTTP Example: [FeignClientApplication.java](examples/http-feign/feign-client/src/main/java/io/github/seal90/feign_client/FeignClientApplication.java)
* gRPC Example: [GrpcClientApplication.java](examples/grpc-spring/grpc-client/src/main/java/io/github/seal90/grpc_client/GrpcClientApplication.java)