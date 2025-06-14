package io.github.seal90.protocol.client;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bean post processor for {@link ProtocolClient} annotations.
 */
public class ProtocolClientAnnotationBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

  public static final String SERVICE_NAME = "PROTOCOL_CLIENT_SERVICE_NAME";

  public static final String CHANNEL_NAME = "PROTOCOL_CLIENT_CHANNEL_NAME";

  private ApplicationContext applicationContext;

  private Map<String, ProtocolClientTypeGenerator> generators;

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
    String protocol = annotation.protocol();
    ProtocolClientTypeGenerator generator = getGenerator(protocol);
    if(generator != null) {
      return generator.generate(injectionTarget, injectionType, annotation);
    }

    throw new IllegalArgumentException("Unsupported protocol: " + protocol);
  }

  /**
   * Get generator for given protocol
   * @param protocol the protocol
   * @return the generator
   */
  private ProtocolClientTypeGenerator getGenerator(String protocol) {
    if(this.generators == null) {
      Map<String, ProtocolClientTypeGenerator> generators = applicationContext.getBeansOfType(ProtocolClientTypeGenerator.class);
      this.generators = generators.values().stream().collect(Collectors.toMap(ProtocolClientTypeGenerator::supportProtocol, Function.identity()));
    }
    return this.generators.get(protocol);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}