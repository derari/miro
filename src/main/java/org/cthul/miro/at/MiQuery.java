package org.cthul.miro.at;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MiQuery {
    
    Select[] select() default {};
    
    Select[] optional() default {};
    
    Select[] internal() default {};
    
    From from() default @From("");
    
    More[] always() default {};
    
    More[] byDefault() default {};
    
    Join[] join() default {};
    
    Where[] where() default {};
    
    OrderBy[] orderBy() default {};
    
    Config[] config() default {};
    
    More[] more() default {};
    
    Impl impl() default @Impl(void.class);
}
