package io.github.seal90.protocol.client.processor;

import io.github.seal90.protocol.client.ProtocolClient;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.client.GrpcClientFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class GrpcClientAnnotationBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

	public static final String SERVICE_NAME = "SPRING_SERVICE_NAME";

	public static final String CHANNEL_NAME = "SPRING_CHANNEL_NAME";

	public static final CallOptions.Key<String> SERVICE_NAME_KEY = CallOptions.Key.create(SERVICE_NAME);

	public static final CallOptions.Key<String> CHANNEL_NAME_KEY = CallOptions.Key.create(CHANNEL_NAME);

	private ApplicationContext applicationContext;

	private GrpcClientFactory grpcClientFactory;

	private GrpcChannelFactory grpcChannelFactory;

	/**
	 * Process the bean's fields annotated with {@link ProtocolClient}.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException – in case of errors
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
		Class<?> clazz = bean.getClass();
		do {
			processFields(clazz, bean);

			clazz = clazz.getSuperclass();
		}
		while (clazz != null);
		return bean;
	}

	/**
	 * Processes the bean's fields annotated with {@link ProtocolClient}.
	 * @param clazz The class to process.
	 * @param bean The bean to process.
	 */
	private void processFields(final Class<?> clazz, final Object bean) {
		for (final Field field : clazz.getDeclaredFields()) {
			final ProtocolClient annotation = AnnotationUtils.findAnnotation(field, ProtocolClient.class);
			if (annotation != null) {
				ReflectionUtils.makeAccessible(field);
				ReflectionUtils.setField(field, bean, processInjectionPoint(field, field.getType(), annotation));
			}
		}
	}

	/**
	 * Processes the given injection point and computes the appropriate value for the
	 * injection.
	 * @param <T> The type of the value to be injected.
	 * @param injectionTarget The target of the injection.
	 * @param injectionType The class that will be used to compute injection.
	 * @param annotation The annotation on the target with the metadata for the injection.
	 * @return The value to be injected for the given injection point.
	 */
	protected <T> T processInjectionPoint(final Member injectionTarget, final Class<T> injectionType,
																				final ProtocolClient annotation) {
		final String serviceName = annotation.serviceName();
		final String channelName = annotation.channelName();
		final String[] interceptors = annotation.interceptors();

		List<ClientInterceptor> interceptorBeans = buildClientInterceptors(interceptors, serviceName, channelName);

		String finalChannelName = channelName(serviceName, channelName);
		if (Channel.class.equals(injectionType)) {
			return handleChannel(finalChannelName, injectionType, interceptorBeans);
		}
		else if (AbstractStub.class.isAssignableFrom(injectionType)) {
			return handleAbstractStub(finalChannelName, injectionType, interceptorBeans);
		}
		else {
			if (injectionTarget != null) {
				throw new InvalidPropertyException(injectionTarget.getDeclaringClass(), injectionTarget.getName(),
						"Unsupported type " + injectionType.getName());
			}
			else {
				throw new BeanInstantiationException(injectionType, "Unsupported grpc stub or channel type");
			}
		}
	}

	private List<ClientInterceptor> buildClientInterceptors(String[] interceptors, String serviceName, String channelName) {
		List<ClientInterceptor> interceptorBeans = new ArrayList<>(interceptors.length);
		for(String interceptor : interceptors) {
			interceptorBeans.add(applicationContext.getBean(interceptor, ClientInterceptor.class));
		}
		// TODO Deal Global interceptors ?
		interceptorBeans.sort(AnnotationAwareOrderComparator.INSTANCE);
		interceptorBeans.addFirst(new ClientInterceptor() {
			@Override
			public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> method,
																																 final CallOptions callOptions, final Channel next) {
				CallOptions withNameCallOptions = callOptions.withOption(SERVICE_NAME_KEY, serviceName)
						.withOption(CHANNEL_NAME_KEY, channelName);
				return next.newCall(method, withNameCallOptions);
			}
		});
		return interceptorBeans;
	}

	/**
	 * Handles the injection of Channel Type.
	 * @param finalChannelName the resolved channel name
	 * @param injectionType the expected channel class
	 * @return the Channel
	 * @param <T> The type of the value to be injected.
	 */
	private <T> T handleChannel(String finalChannelName,
			Class<T> injectionType, List<ClientInterceptor> interceptorBeans) {

		Channel channel = fetchContextBean(finalChannelName, Channel.class);
		// TODO use same channel GrpcClientFactory created?
		if(channel == null) {
			channel = grpcChannelFactory.createChannel(finalChannelName);
		}
		channel = ClientInterceptors.intercept(channel, interceptorBeans);
		return injectionType.cast(channel);
	}

	/**
	 * Handles the injection of AbstractStub Type.
	 * @param finalChannelName the resolved channel name
	 * @param injectionType the expected channel class
	 * @return the AbstractStub
	 * @param <T> The type of the value to be injected.
	 */
	private <T> T handleAbstractStub(String finalChannelName,
			Class<T> injectionType, List<ClientInterceptor> interceptorBeans) {
		AbstractStub<?> stub = fetchContextBean(finalChannelName, AbstractStub.class);
		if(stub == null) {
			stub = (AbstractStub<?>) grpcClientFactory.getClient(finalChannelName, (Class<? extends AbstractStub>) injectionType, null);
		}
		stub = stub.withInterceptors(interceptorBeans.toArray(new ClientInterceptor[]{}));
		return injectionType.cast(stub);
	}

	/**
	 * Computes the channel name to use.
	 * @param serviceName {@link ProtocolClient} annotation serviceName.
	 * @param channelName {@link ProtocolClient} annotation channelName.
	 * @return The channel name to use.
	 */
	private String channelName(String serviceName, String channelName) {
		if (!channelName.isEmpty()) {
			return channelName;
		}
		if (!serviceName.isEmpty()) {
			return serviceName;
		}
		return "default";
	}

	private <T> T fetchContextBean(String name, Class<T> type) {
		if(applicationContext.containsBean(name)) {
			Object bean = applicationContext.getBean(name);
			if(type.isAssignableFrom(bean.getClass())) {
				return type.cast(bean);
			}
		}
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		this.grpcClientFactory = new GrpcClientFactory(applicationContext);
		this.grpcChannelFactory = applicationContext.getBean(GrpcChannelFactory.class);
	}

}