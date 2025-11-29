package io.github.seal90.protocol.client.processor;

import feign.RequestInterceptor;
import io.github.seal90.protocol.client.ProtocolClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class FeignClientAnnotationBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

  public static final String SERVICE_NAME = "SPRING_SERVICE_NAME";

  public static final String CHANNEL_NAME = "SPRING_CHANNEL_NAME";

  private ApplicationContext applicationContext;

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
    if(interceptors.length != 0) {
      throw new InvalidPropertyException(injectionTarget.getDeclaringClass(), injectionTarget.getName(),
          "Feign not support interceptors now ");
    }

//    List<RequestInterceptor> interceptorBeans = buildRequestInterceptors(interceptors, serviceName, channelName);

    String finalChannelName = channelName(serviceName, channelName);

    FeignClientFactoryBean factoryBean = new FeignClientFactoryBean();
    factoryBean.setName(finalChannelName);
    factoryBean.setContextId(finalChannelName);
    factoryBean.setType(injectionType);
    factoryBean.setApplicationContext(applicationContext);
    Object obj = registerSingletonBean(finalChannelName, factoryBean);

    return injectionType.cast(obj);
  }

  public Object registerSingletonBean(String beanName,Object singletonObject){

    ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
    DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getAutowireCapableBeanFactory();
    defaultListableBeanFactory.registerSingleton(beanName,singletonObject);

    return configurableApplicationContext.getBean(beanName);
  }

  private List<RequestInterceptor> buildRequestInterceptors(String[] interceptors, String serviceName, String channelName) {
    List<RequestInterceptor> interceptorBeans = new ArrayList<>(interceptors.length);
    for(String interceptor : interceptors) {
      interceptorBeans.add(applicationContext.getBean(interceptor, RequestInterceptor.class));
    }
    return interceptorBeans;
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

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}