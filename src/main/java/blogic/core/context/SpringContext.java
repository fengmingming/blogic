package blogic.core.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class SpringContext implements ApplicationContextAware {

    protected static ApplicationContext context;

    public static ApplicationContext INSTANCE() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return SpringContext.INSTANCE().getBean(clazz);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return SpringContext.INSTANCE().getBean(beanName, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return SpringContext.INSTANCE().getBeansOfType(clazz);
    }

    public static Environment getEnvironment() {
        return SpringContext.INSTANCE().getEnvironment();
    }

    public static String getMessage(String code, Object ... args) {
        return SpringContext.INSTANCE().getMessage(code, args, Locale.getDefault());
    }

    public static String getMessage(int code, Object ... args) {
        return SpringContext.INSTANCE().getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    public static String getMessage(int code, Locale locale, Object ... args) {
        return SpringContext.INSTANCE().getMessage(String.valueOf(code), args, locale);
    }

    public static String getMessage(int code, Locale locale, String defaultMessage, Object ... args) {
        return SpringContext.INSTANCE().getMessage(String.valueOf(code), args, defaultMessage, locale);
    }

}
