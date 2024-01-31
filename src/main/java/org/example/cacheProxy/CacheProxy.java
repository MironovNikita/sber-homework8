package org.example.cacheProxy;

import java.lang.reflect.Proxy;

public class CacheProxy {

    public static <T> T cache(T object, String cacheFolder, Class<T> clazz) {
        var invocationHandler = new CacheHandler(object, cacheFolder);

        return clazz.cast(Proxy.newProxyInstance(
                object.getClass().getClassLoader(),
                new Class[]{clazz},
                invocationHandler));
    }
}
