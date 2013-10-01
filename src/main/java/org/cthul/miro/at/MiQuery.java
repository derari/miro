package org.cthul.miro.at;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MiQuery {
    
    String[] select() default {};
    
    String[] opt_select() default {};
    
    String[] int_select() default {};
    
    String from() default "";
    
    String[] always() default {};
    
    String[] byDefault() default {};
    
    Join[] join() default {};
    
    Where[] where() default {};
    
    Config[] config() default {};
    
    More[] more() default {};
}
