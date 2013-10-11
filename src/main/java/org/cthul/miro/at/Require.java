package org.cthul.miro.at;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Require {
    
    String[] value();
}
