package org.cthul.miro.at;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface More {
    
    String key() default "";
    
    String[] require() default {};
    
    String[] generatedKeys() default {};
    
    String[] naturalKeys() default {};
    
    String[] attributes() default {};
    
    String[] select() default {};
    
    String[] optional() default {};
    
    String[] internal() default {};
    
    Join[] join() default {};
    
    Where[] where() default {};
    
    OrderBy[] orderBy() default {};
    
    Config[] config() default {};
    
    Put[] put() default {};
}
