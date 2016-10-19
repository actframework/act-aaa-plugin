package act.aaa;

import act.aaa.util.LoginUserFinder;
import org.osgl.aaa.Principal;
import org.osgl.inject.annotation.InjectTag;
import org.osgl.inject.annotation.LoadValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation specify a field or parameter should be the current
 * login user
 */
@InjectTag
@LoadValue(LoginUserFinder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface LoginUser {
    /**
     * Specify the property name to search the user in the database using {@link Principal#getName() principal name}
     *
     * Default value: empty string.
     *
     * If not specified then it will use {@link act.aaa.AAAConfig.user#key aaa.user.key} configuration as the key
     *
     * @return user key
     */
    String key() default "";
}