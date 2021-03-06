package org.jumpmind.pos.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;


public class ClassUtils {
    protected static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    public static Class loadClass(String className) {
        if (className == null) {
            throw new ReflectionException("className cannot be null.");
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (Exception ex) {
            throw new ReflectionException("Failed to load class named \"" + className + "\"", ex);
        }
    }

    public static <T> T instantiate(String className) {
        Class clazz = loadClass(className);
        if (clazz == null) {
            throw new ReflectionException("No class found for className:\"" + className + "\"");
        }
        try {
            return (T)clazz.newInstance();
        } catch (Exception ex) {
            throw new ReflectionException("Failed to instansitate class named \"" + className + "\"", ex);
        }
    }

    /**
     * This method first attempts to check the given targetObject's class for an 
     * annotation of the given type.  If that fails, then it uses a Spring AOP
     * Utility to attempt to locate the annotation.  This is useful for CGLIB
     * proxies who don't actually have the annotation of the proxied bean
     * on them, and therefore the actual class being proxied needs to be checked
     * for the annotation.
     * @param annotationClass The annotation type to search for.
     * @param targetObj The object whose class should be searched for the given 
     * annotation type.
     * @return Will return null if the annotation could not found. Otherwise,
     * if the annotation exists on the class of the given targetObj, it will be
     * returned.
     */
    public static <A extends Annotation> A resolveAnnotation(Class<A> annotationClass, Object targetObj) {
        A annotation = targetObj.getClass().getAnnotation(annotationClass);
        if (annotation == null) {
            Class<?> targetClass = AopUtils.getTargetClass(targetObj);
            annotation = targetClass.getAnnotation(annotationClass);
        }

        return annotation;
    }
    
    /**
     * Retrieves all of the classes at or below the given package which have the
     * given annotation.
     * @param packageName The root package to begin searching
     * @param annotation The annotation to search for.
     * @return A list of Class objects.
     */
    public static List<Class<?>> getClassesForPackageAndAnnotation(String packageName, Class<? extends Annotation> annotation) {
        return getClassesForPackageAndAnnotation(packageName, annotation, null, null);
    }
    
    /**
     * Retrieves all of the classes at or below the given package which implement the given interface.
     * @param <T>
     * @param packageName The root package to begin searching
     * @param matchingType The annotation to search for.
     * @return A list of Class objects.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Class<T>> getClassesForPackageAndType(String packageName, Class<T> matchingType) {
        List<Class<T>> result = new ArrayList<>();
        List<Class<?>> classes = getClassesForPackageAndAnnotation(packageName, null, null, matchingType);
        classes.stream().forEach(e -> result.add((Class<T>) e));
        return result;
    }
    
    /**
     * Retrieves all of the classes at or below the given package which have the given annotation OR implement the matchingInterface. 
     * @param packageName The root package to begin searching
     * @param annotation The annotation to search for (optional)
     * @param alwaysIncludeClasses An optional list of classes to always return 
     * in the list of returned classes.
     * @return A list of Class objects.
     */
    protected static List<Class<?>> getClassesForPackageAndAnnotation(String packageName, Class<? extends Annotation> annotation, List<Class<?>> alwaysIncludeClasses, Class<?> matchingType) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (alwaysIncludeClasses != null) {
            classes.addAll(alwaysIncludeClasses);
        }
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        if (annotation != null) {            
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
        } 
        if (matchingType != null) {
            scanner.addIncludeFilter(new AssignableTypeFilter(matchingType));
        }
        
        for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
            try {
                classes.add(Class.forName(bd.getBeanClassName()));
            } catch (ClassNotFoundException ex) {
                logger.error(ex.getMessage());
            }
        }
        return classes;
    }


    public static boolean isSimpleType(Class<?> clazz) {
        if (clazz.isPrimitive()
                || String.class == clazz
                || BigDecimal.class == clazz
                || Money.class == clazz
                || Date.class == clazz) {
            return true;
        } else {
            return false;
        }
    }
}
