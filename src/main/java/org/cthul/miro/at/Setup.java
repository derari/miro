package org.cthul.miro.at;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.cthul.objects.instance.Arg;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Setup {
    
    String key() default "";
    
    Class<?> impl();
    
    String factory() default "";
    
    Arg[] args() default {};
}
