package org.cthul.miro.at;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Always {
    
    String key() default "";
    
    String[] require() default {};
    
    String[] select() default {};
    
    String[] internal() default {};
    
    Join[] join() default {};
    
    Where[] where() default {};
    
    OrderBy[] orderBy() default {};
    
    Config[] config() default {};
    
    Put[] put() default {};
}
