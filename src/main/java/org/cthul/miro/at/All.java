package org.cthul.miro.at;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface All {
    
    Select[] select() default {};
    
    Join[] join() default {};
    
    Where[] where() default {};
    
    Setup[] setup() default {};
    
    Put[] put() default {};
}
