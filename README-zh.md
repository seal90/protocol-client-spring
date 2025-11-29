# Protocol client

欢迎使用 Protocol Client 项目！

通过面向对象的方式进行服务调用，不仅能显著减少数据通信相关的样板代码，还能让同一套接口定义在客户端与服务端之间复用，大幅降低双方在字段约定和接口对齐上的沟通成本。

此处提到的服务不仅限于集群内部服务，也包含外部服务提供的接口，以服务维度看外部接口。

为此，本项目提供了一个统一且易于使用的客户端注解——[ProtocolClient.java](protocol-client-spring/src/main/java/io/github/seal90/protocol/client/ProtocolClient.java)，可简化多种协议（如 gRPC、HTTP 等）客户端的初始化过程。只需极少的配置，即可无缝调用基于不同通信协议的服务。

# 服务调用核心要素

要调用目标服务，调用方需明确指定以下要素：

* 服务名称（Service Name）：目标服务的逻辑名称，通常与通信通道名称一致，用于服务发现。
* 通道配置（Channel Configuration）：定义调用方式，包括端点地址、超时设置，以及序列化/反序列化（编码/解码）策略。
* 通信载荷（Communication Payload）：实际传输的数据，需采用预先约定的格式（例如 JSON、Protobuf）。
* 附加元数据（Additional Metadata）：协议或路由相关的额外信息，例如 HTTP 方法与路径、认证令牌，或服务注册中心用于定位目标服务实例所需的上下文提示。

# `ProtocolClient` 注解的四个核心参数

- **`protocol`**  
  指定通信协议及其具体实现方式，命名格式为 `协议_实现方式`（例如 `GRPC_SPRING`、`HTTP_FEIGN`），用于选择底层通信组件。

- **`serviceName`**  
  目标服务的逻辑名称。该名称不仅适用于集群内部服务，也涵盖外部第三方服务。  
  当未显式配置 `channelName` 时，系统将使用 `serviceName` 进行服务发现：
    - 优先从本地配置文件中查找对应地址；
    - 若未找到，则尝试通过配置的外部服务发现机制（如注册中心）进行解析；
    - 类似于主机名解析“先查 hosts 文件，再查 DNS”的机制。  
      若最终仍无法定位服务，可 fallback 到预设的默认通道进行调用。

- **`channelName`**  
  当实际调用的服务地址或通道与 `serviceName` 不一致时，可通过此参数显式指定：
    - 若值以 `static://` 或 `lb://` 开头，将直接作为目标地址使用（支持负载均衡前缀）；
      - static://http://www.spring.io
      - static://https://www.spring.io
      - static://unix:///path/to/file
      - channel://
      - default://
      - lib://SERVICE-NAME
      - context://
    - 其他情况则视为 Spring 上下文中的 bean 名称，从 `applicationContext` 中获取对应的已定义通道实例。

- **`interceptors`**  
  用于对请求进行自定义处理，尤其适用于操作请求头、日志记录、认证注入等场景。  
  在拦截器中，可通过以下常量获取注解上下文信息，例如在服务端发现场景可以获取并使用。
    - `ProtocolClientAnnotationBeanPostProcessor.SERVICE_NAME` → 获取 `serviceName`
    - `ProtocolClientAnnotationBeanPostProcessor.CHANNEL_NAME` → 获取 `channelName`

# 使用

```java
    // ProtocolClient protocol ProtocolClient
    // ProtocolClient define serviceName for serviceName, channelName for channel, interceptors for additional information
	@ProtocolClient(serviceName = "HelloWorldService", channelName = "HelloWorldServiceChannel", interceptors = {})
    // HelloWorldServiceGrpc.HelloWorldServiceBlockingStub for communication content
	private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub stub;
```

# 实现
* GRPC_SPRING
* HTTP_FEIGN
* HTTP_SPRING

# 配置文件

## 默认配置文件
当有多个代码工程时，期望引入某个固定jar包来实现代理header的默认配置，可以新建一个工程，src/main/resources下新建 application.yaml 配置
在业务工程用引用此工程即可，注意本业务工程需要将配置文件放在 src/main/resources/config下，实现更高优先级的配置加载

## 配置文件格式
```yaml
seal:
  spring:
    protocol-client:
      grpc-spring:
        default-channel-config:
          load-balancing-policy: "round_robin"
          negotiation-type: PLAINTEXT # TLS, PLAINTEXT_UPGRADE, PLAINTEXT;
          enable-keep-alive: false
          idle-timeout: 20s
          keep-alive-time: 5s
          keep-alive-timeout: 20s
          keep-alive-without-calls: false
          max-inbound-message-size: 4M
          max-inbound-metadata-size: 8K
          user-agent: 
          default-deadline: 
          secure: true
          ssl-bundle: ""
        default-channel-name:
        channels:
          CHANNEL-NAME:
            addresses:
              - ip:port
              - ip:port
            load-balancing-policy: "round_robin"
            negotiation-type: PLAINTEXT # TLS, PLAINTEXT_UPGRADE, PLAINTEXT;
            enable-keep-alive: false
            idle-timeout: 10s
            keep-alive-time: 10s
            keep-alive-timeout: 10s
            keep-alive-without-calls: false
            max-inbound-message-size: 1M
            max-inbound-metadata-size: 1M
            user-agent:
            default-deadline:
            secure: false
            ssl-bundle: ""              
        services:
          SERVICE-NAME:
            channel-name:
            channel-config:
              addresses:
                - ip:port
                - ip:port
---

seal:
  spring:  
      http-spring:
        forward-web-headers: # Forward the HTTP request headers from the web server 
          - overlay-ns
#        default-channel-config:
        default-channel-name:
        channels:
          CHANNEL-NAME:
            address: https://www.github.com
            addresses:
              - https://www.github.com
              - https://www.github.io
# WebClient.Builder nonsupport
#            redirects: DONT_FOLLOW # FOLLOW_WHEN_POSSIBLE, FOLLOW, DONT_FOLLOW
#            connect-timeout: 5s
#            read-timeout: 10s
#            # call-timeout write-timeout
#            ssl-bundle: ""

        services:
          SERVICE-NAME:
            channel-name:
            channel-config:
              addresses:
                - https://www.github.com
                - https://www.github.io
#              default-headers:
#                - HEADER-KEY: [HEADER-VALUE1, HEADER-VALUE2]

```

# Example
* client -- Header: overlay-ns, hello-header --> Server -- Header: overlay-ns --> Server itself
* HTTP Feign Example: [FeignClientApplication.java](examples/http-feign/feign-client/src/main/java/io/github/seal90/feign_client/FeignClientApplication.java)
* gRPC Spring Example: [GrpcClientApplication.java](examples/grpc-spring/grpc-client/src/main/java/io/github/seal90/grpc_client/GrpcClientApplication.java)
* HTTP Spring WebClient Example: [HTTPClientApplication.java](examples/http-spring-webclient/http-client/src/main/java/io/github/seal90/http_client/HTTPClientApplication.java)
* * HTTP Spring WebClient Example: [HTTPClientApplication.java](examples/http-spring-resttemplate/http-client/src/main/java/io/github/seal90/http_client/HTTPClientApplication.java)

# TODO
* http-resttemplate 响应头不生效
* 优化依赖管理，创建starter项目管理依赖
* ProtocolClient#channelName -> ProtocolClient#channel
* ProtocolClient -> ServiceClient