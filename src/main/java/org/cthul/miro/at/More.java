package org.cthul.miro.at;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface More {
    
    String[] using();
    
    String[] select() default {};
    
    String[] opt_select() default {};
    
    String[] int_select() default {};
    
    Join[] join() default {};
    
    Where[] where() default {};
    
    Config[] config() default {};
}
