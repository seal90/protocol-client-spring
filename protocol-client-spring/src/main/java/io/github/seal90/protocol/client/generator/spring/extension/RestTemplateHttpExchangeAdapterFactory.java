package io.github.seal90.protocol.client.generator.spring.extension;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.seal90.protocol.client.ProtocolClientAnnotationBeanPostProcessor.CHANNEL_NAME;
import static io.github.seal90.protocol.client.ProtocolClientAnnotationBeanPostProcessor.SERVICE_NAME;

public class RestTemplateHttpExchangeAdapterFactory implements HttpExchangeAdapterFactory {

    private final ApplicationContext applicationContext;

    public RestTemplateHttpExchangeAdapterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public HttpExchangeAdapter create(String serviceName, String channelName, String[] interceptors) {

        boolean nameResolved = false;
        RestTemplate restTemplate = null;
        if(channelName.startsWith("context://")) {
            restTemplate = (RestTemplate)applicationContext.getBean(channelName.replaceFirst("context://", ""));
            nameResolved = true;
        } else {
            RestTemplateBuilder builder = applicationContext.getBean(RestTemplateBuilder.class)
                .rootUri("http://"+serviceName);
            restTemplate = builder.build();
        }

        List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<>();
        for(String interceptorName : interceptors) {
            ClientHttpRequestInterceptor interceptor = (ClientHttpRequestInterceptor)applicationContext.getBean(interceptorName);
            interceptorList.add(interceptor);
        }

        Boolean nameResolvedFlag = nameResolved;
        List<ClientHttpRequestInterceptor> requestInterceptors = restTemplate.getInterceptors();
        interceptorList.addAll(requestInterceptors);
        restTemplate.setInterceptors(interceptorList);
        restTemplate.getClientHttpRequestInitializers().addFirst(request -> {
            Map<String, Object> attributeMap = request.getAttributes();
            attributeMap.put(SERVICE_NAME, serviceName);
            attributeMap.put(CHANNEL_NAME, channelName);
            attributeMap.put(NAME_RESOLVED_FLAG, nameResolvedFlag);
        });

        return RestTemplateAdapter.create(restTemplate);
    }

//    @Data
//    @AllArgsConstructor
//    public static class NameResolveClientHttpRequestInterceptor implements ClientHttpRequestInterceptor, Ordered {
//
//        private HTTPSpringProtocolClientProperties protocolClientProperties;
//
//        @Override
//        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//
//            Object nameResolvedFlag = request.getAttributes().get(NAME_RESOLVED_FLAG);
//            if(Boolean.TRUE.equals(nameResolvedFlag)) {
//                return execution.execute(request, body);
//            }
//
//            Map<String, Object> attributes = request.getAttributes();
//            String serviceName = (String)attributes.get(SERVICE_NAME);
//            String channelName = (String)attributes.get(CHANNEL_NAME);
//
//            Boolean nameResolved = true;
//            String parsedAddress = null;
//            if("".equals(channelName)) {
//                parsedAddress = parseByServiceName(serviceName);
//            } else {
//                if (channelName.startsWith("static://")) {
//                    parsedAddress = channelName.replaceFirst("static://", "");
//                } else if (channelName.startsWith("channel://")) {
//                    parsedAddress = parseByChannelName(channelName.replaceFirst("channel://", ""));
//                } else if (channelName.startsWith("default://")) {
//                    parsedAddress = parseDefaultChannel();
//                } else if (channelName.startsWith("lb://")) {
//                    parsedAddress = "http://" + channelName.replaceFirst("lb://", "");
//                    nameResolved = false;
//                }
//            }
//
//            if(parsedAddress != null) {
//                request.getAttributes().put(NAME_RESOLVED_FLAG, nameResolved);
//
//                URI uri = request.getURI();
//                URI parsedURI = URI.create(parsedAddress);
//                int port = parsedURI.getPort() > -1 ?parsedURI.getPort():uri.getPort();
//                URI newUri = UriComponentsBuilder.fromUri(uri)
//                    .scheme(parsedURI.getScheme())
//                    .host(parsedURI.getHost())
//                    .port(port)
//                    .replacePath(parsedURI.getPath())
//                    .path(uri.getPath())
//                    .build()
//                    .toUri();
//                return execution.execute(new HttpRequest() {
//
//                    @Override
//                    public HttpHeaders getHeaders() {
//                        return request.getHeaders();
//                    }
//
//                    @Override
//                    public HttpMethod getMethod() {
//                        return request.getMethod();
//                    }
//
//                    @Override
//                    public URI getURI() {
//                        return newUri;
//                    }
//
//                    @Override
//                    public Map<String, Object> getAttributes() {
//                        return request.getAttributes();
//                    }
//                }, body);
//            }
//
//            return execution.execute(request, body);
//        }
//
//        @Override
//        public int getOrder() {
//            return -10;
//        }
//
//        private String parseByServiceName(String serviceName) {
//            String parsedAddress = null;
//            HTTPSpringProtocolClientServiceProperties serviceProperties = protocolClientProperties.getServices().get(serviceName);
//            if(serviceProperties != null) {
//                String channelName = serviceProperties.getChannelName();
//                if(channelName != null) {
//                    parsedAddress = parseByChannelName(channelName);
//                    if(parsedAddress == null) {
//                        throw new RuntimeException("Parsed "+serviceName+" by channelName: " + channelName + " fail.");
//                    }
//                } else {
//                    HTTPSpringProtocolClientChannelProperties channelProperties = serviceProperties.getChannelConfig();
//                    parsedAddress = parseChannel(channelProperties);
//                    if(parsedAddress == null) {
//                        throw new RuntimeException("Parsed "+serviceName+" by channel config fail.");
//                    }
//                }
//            } else {
//                String defaultChannelName = protocolClientProperties.getDefaultChannelName();
//                if(defaultChannelName != null) {
//                    HTTPSpringProtocolClientChannelProperties channelProperties = protocolClientProperties.getChannels().get(defaultChannelName);
//                    parsedAddress = parseChannel(channelProperties);
//                }
//            }
//            return parsedAddress;
//        }
//
//        private String parseByChannelName(String channelName) {
//            HTTPSpringProtocolClientChannelProperties channelProperties = protocolClientProperties.getChannels().get(channelName);
//            return parseChannel(channelProperties);
//        }
//
//        private String parseDefaultChannel() {
//            String defaultChannelName = protocolClientProperties.getDefaultChannelName();
//            if(defaultChannelName != null) {
//                HTTPSpringProtocolClientChannelProperties channelProperties = protocolClientProperties.getChannels().get(defaultChannelName);
//                String parsedAddress = parseChannel(channelProperties);
//                if(parsedAddress == null) {
//                    throw new RuntimeException("Parsed defaultChannelName "+defaultChannelName+" fail.");
//                }
//                return parsedAddress;
//            }
//            return null;
//        }
//
//        private String parseChannel(HTTPSpringProtocolClientChannelProperties channelProperties) {
//            String parsedAddress = null;
//            if(channelProperties != null) {
//                String address = channelProperties.getAddress();
//                if(address != null) {
//                    parsedAddress = address;
//                } else {
//                    List<String> addresses = channelProperties.getAddresses();
//                    if(!addresses.isEmpty()) {
//                        parsedAddress = addresses.get(new Random().nextInt(addresses.size()));
//                    }
//                }
//            }
//            return parsedAddress;
//        }
//    }
}