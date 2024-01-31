package org.example.service;

import org.example.cacheData.Cache;

import java.util.List;

import static org.example.cacheData.CacheType.FILE;

public interface Service {
    @Cache(cacheType = FILE, zip = true)
    double powerNumber(double number, double degree);

    @Cache
    int factorial(int number);

    @Cache(cacheType = FILE, fileNamePrefix = "divideNumber_", listSize = 3)
    List<Integer> dividedWithoutRemainder(int number);

    @Cache(cacheType = FILE, identityBy = {String.class})
    int squaring(String word, int number);
}
