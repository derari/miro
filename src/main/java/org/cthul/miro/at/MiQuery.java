package org.cthul.miro.at;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MiQuery {
    
    String[] generatedKeys() default {};
    
    String[] naturalKeys() default {};
    
    String[] attributes() default {};
    
    String[] select() default {};
    
    String[] optional() default {};
    
    String[] internal() default {};
    
    String from() default "";
    
    More[] always() default {};
    
    More[] byDefault() default {};
    
    Join[] join() default {};
    
    Where[] where() default {};
    
    OrderBy[] orderBy() default {};
    
    Config[] config() default {};
    
    More[] more() default {};
    
    Class<?> impl() default void.class;
}
