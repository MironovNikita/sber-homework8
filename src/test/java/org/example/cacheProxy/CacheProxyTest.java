package org.example.cacheProxy;

import org.example.service.Service;
import org.example.service.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CacheProxyTest {
    private Service proxiedService;

    @BeforeEach
    void initialize() {
        Service service = new ServiceImpl();
        proxiedService = CacheProxy.cache(service, "src/test/java/testProxyData", Service.class);
    }

    @DisplayName("Проверка кэширования возведения числа в степень при параметрах" +
            " \"@Cache(cacheType = FILE, zip = true)\"")
    @Test
    void shouldReturnPowerNumberFromCachedZipFile() {
        double result = proxiedService.powerNumber(2, 3);
        double cachedResult = proxiedService.powerNumber(2, 3);

        assertEquals(result, cachedResult);
    }

    @DisplayName("Проверка кэширования вычисления факториала числа при дефолтных параметрах \"@Cache\"")
    @Test
    void shouldReturnFactorialOfNumberFromInMemoryCache() {
        int result = proxiedService.factorial(5);
        int cachedResult = proxiedService.factorial(5);

        assertEquals(result, cachedResult);
    }

    @DisplayName("Проверка кэширования вычисления чисел, делящихся без остатка при " +
            "\"@Cache(cacheType = FILE, fileNamePrefix = \"divideNumber_\", listSize = 3)\"")
    @Test
    void shouldReturnDivideNumbersOfGotNumberButOnlyLastThreeResults() {
        List<Integer> result = proxiedService.dividedWithoutRemainder(30);
        List<Integer> cachedResult = proxiedService.dividedWithoutRemainder(30);

        List<Integer> checkList = result.subList(result.size() - 3, result.size());

        assertEquals(checkList, cachedResult);
    }

    @DisplayName("Проверка кэширования вычисления квадрата чисел при параметрах " +
            " \"@Cache(cacheType = FILE, identityBy = {String.class})\"")
    @Test
    void shouldReturnNumberInSquareButCreateDifferentFilesIfStringIsAnother() {
        int result1 = proxiedService.squaring("Первый", 8);
        int checkedResult1 = proxiedService.squaring("Первый", 8);

        assertEquals(result1, checkedResult1);

        int result2 = proxiedService.squaring("Второй", 8);
        int checkedResult2 = proxiedService.squaring("Второй", 8);

        assertEquals(result2, checkedResult2);
    }
}