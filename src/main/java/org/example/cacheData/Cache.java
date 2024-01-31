package org.example.cacheData;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    CacheType cacheType() default CacheType.IN_MEMORY;

    String fileNamePrefix() default "";

    Class[] identityBy() default Class.class;

    boolean zip() default false;

    int listSize() default 0;
}
