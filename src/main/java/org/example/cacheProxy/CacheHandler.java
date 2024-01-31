package org.example.cacheProxy;

import org.example.cacheData.Cache;
import org.example.cacheData.CacheSettings;
import org.example.cacheData.CacheType;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CacheHandler implements InvocationHandler {
    private final Object object;
    private final String cacheFolder;
    private final Map<Method, CacheSettings> cacheSettingsMap = new HashMap<>();
    private final Map<Method, Map<List<Object>, Object>> methodCache = new HashMap<>();

    public CacheHandler(Object object, String cacheFolder) {
        this.object = object;
        this.cacheFolder = cacheFolder;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Cache.class) && !cacheSettingsMap.containsKey(method)) {
            Cache cacheAnnotation = method.getAnnotation(Cache.class);

            cacheSettingsMap.put(method, new CacheSettings(
                    cacheAnnotation.cacheType(),
                    cacheAnnotation.fileNamePrefix(),
                    cacheAnnotation.identityBy(),
                    cacheAnnotation.zip(),
                    cacheAnnotation.listSize()
            ));
        }

        return method.isAnnotationPresent(Cache.class) ? invokeMethod(method, args) : method.invoke(method, args);
    }

    private Object invokeMethod(Method method, Object[] args) throws Throwable {
        // Проверяем, есть ли кэш для этого метода
        if (cacheSettingsMap.containsKey(method)) {
            CacheSettings cacheSettings = cacheSettingsMap.get(method);
            // Проверяем, есть ли уже результат в кэше
            List<Object> key = getKey(method, args);
            if (methodCache.containsKey(method) && methodCache.get(method).containsKey(key)) {
                System.out.printf("Работает возврат кэша из памяти для метода %s с аргументами %s%n", method.getName(),
                        Arrays.toString(args));
                // Возвращаем результат из кэша
                return methodCache.get(method).get(key);
            } else {
                Object result = readFromFile(method, key, cacheSettings);
                if (result != null) {
                    System.out.printf("Работает возврат кэша из файла для метода %s с аргументами %s%n",
                            method.getName(), Arrays.toString(args));
                    return result;
                } else {
                    // Если результата в кэше нет, выполняем метод
                    result = method.invoke(object, args);

                    if (cacheSettings.getCacheType() == CacheType.FILE) {
                        saveToFile(method, key, result, cacheSettings);
                    } else {
                        saveToMemory(method, key, result, cacheSettings);
                    }
                }
                return result;
            }
        } else {
            return method.invoke(object, args);
        }
    }

    private List<Object> getKey(Method method, Object[] args) {
        CacheSettings cacheSettings = cacheSettingsMap.get(method);
        List<Class<?>> identityBy = Arrays.asList(cacheSettings.getIdentityBy());

        if (identityBy.isEmpty()) {
            return Arrays.asList(args);
        } else {
            return Arrays.asList(args).subList(0, identityBy.size());
        }
    }

    private void saveToFile(Method method, List<Object> key, Object result,
                            CacheSettings cacheSettings) {

        String prefixOrMethodName =
                cacheSettings.getFileNamePrefix().isBlank() ? method.getName() : cacheSettings.getFileNamePrefix();

        String fileName = new StringBuilder()
                .append(cacheFolder)
                .append("/")
                .append(prefixOrMethodName)
                .append(key.hashCode())
                .append((cacheSettings.isZip() ? ".zip" : ".txt")).toString();

        try (OutputStream outputStream = new FileOutputStream(fileName);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                     cacheSettings.isZip() ? new GZIPOutputStream(outputStream) : outputStream)) {
            if (result instanceof List && cacheSettings.getListSize() > 0) {
                List<Object> resultList = (List<Object>) result;

                // Создаем новый список и добавляем в него последние элементы из исходного списка
                List<Object> trimmedList = new ArrayList<>();
                int start = Math.max(0, resultList.size() - cacheSettings.getListSize());
                for (int i = start; i < resultList.size(); i++) {
                    trimmedList.add(resultList.get(i));
                }
                // Сохраняем обрезанный список в файл
                objectOutputStream.writeObject(trimmedList);
            } else {
                // Сохраняем результат как обычно
                objectOutputStream.writeObject(result);
            }
        } catch (FileNotFoundException exception) {
            throw new RuntimeException(
                    String.format("Ошибка. Искомая директория %s для сохранения не была найдена", fileName), exception);

        } catch (IOException exception) {
            throw new RuntimeException(
                    String.format("Ошибка работы с файлом %s", fileName), exception);
        }
    }

    private void saveToMemory(Method method, List<Object> key, Object result, CacheSettings cacheSettings) {
        if (!methodCache.containsKey(method)) {
            methodCache.put(method, new LinkedHashMap<>());
        }

        Map<List<Object>, Object> cacheMap = methodCache.get(method);

        // Если метод возвращает список и у нас есть ограничение по количеству элементов в кэше
        if (result instanceof List && cacheSettings.getListSize() > 0) {
            List<Object> resultList = (List<Object>) result;

            // Создаем новый список и добавляем в него последние элементы из исходного списка
            List<Object> trimmedList = new ArrayList<>();
            int start = Math.max(0, resultList.size() - cacheSettings.getListSize());
            for (int i = start; i < resultList.size(); i++) {
                trimmedList.add(resultList.get(i));
            }
            // Сохраняем обрезанный список в кэш
            cacheMap.put(key, trimmedList);
        } else {
            // Сохраняем результат как обычно
            cacheMap.put(key, result);
        }
    }

    private Object readFromFile(Method method, List<Object> key, CacheSettings cacheSettings) {
        String prefixOrMethodName =
                cacheSettings.getFileNamePrefix().isBlank() ? method.getName() : cacheSettings.getFileNamePrefix();

        String fileName = new StringBuilder()
                .append(cacheFolder)
                .append("/")
                .append(prefixOrMethodName)
                .append(key.hashCode())
                .append((cacheSettings.isZip() ? ".zip" : ".txt")).toString();

        File file = new File(fileName);
        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file);
                 ObjectInputStream objectInputStream = new ObjectInputStream(
                         cacheSettings.isZip() ? new GZIPInputStream(inputStream) : inputStream)) {
                return objectInputStream.readObject();
            } catch (FileNotFoundException exception) {
                throw new RuntimeException(
                        String.format("Ошибка. Файл %s не был найден по указанному пути", fileName), exception);

            } catch (IOException exception) {
                throw new RuntimeException(String.format("Ошибка работы с файлом %s", fileName), exception);

            } catch (ClassNotFoundException exception) {
                throw new RuntimeException("Ошибка считывания объекта класса", exception);
            }
        }
        return null;
    }
}
