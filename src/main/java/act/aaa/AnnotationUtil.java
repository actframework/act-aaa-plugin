package act.aaa;

import org.osgl.util.E;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/*
 * Disclaim: the code is copied from SpringFramework AnnotationUtils
 */
class AnnotationUtil {
    static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        E.NPE(clazz, "Class must not be null");
        A annotation = clazz.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            annotation = findAnnotation(ifc, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        if (!Annotation.class.isAssignableFrom(clazz)) {
            for (Annotation ann : clazz.getAnnotations()) {
                annotation = findAnnotation(ann.annotationType(), annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null || superClass.equals(Object.class)) {
            return null;
        }
        return findAnnotation(superClass, annotationType);
    }

    static <A extends Annotation> A findAnnotation(Method m, Class<A> c) {
        return m.getAnnotation(c);
    }
}
