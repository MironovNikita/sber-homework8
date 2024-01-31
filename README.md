
# Сериализация – Serialization

### ✨Немного теории

🤖 **Сериализация** - это процесс преобразования объекта в последовательность байтов, которая может быть сохранена в файле или передана по сети, а затем восстановлена обратно в объект. Этот процесс полезен, когда вам нужно сохранить состояние объекта или передать его через сеть.

В **`Java`** для сериализации и десериализации объектов используется интерфейс **`Serializable`**. Если вы хотите, чтобы объект мог быть сериализован, вы должны имплементировать этот интерфейс. В интерфейсе **`Serializable`** нет методов, это просто маркерный интерфейс, который говорит виртуальной машине Java о том, что объект может быть сериализован.

Процесс сериализации и десериализации выполняется с помощью классов **`ObjectOutputStream`** и **`ObjectInputStream`**.

**Коротенький пример**:
```java
public class SerializationDemo {
    public static void main(String[] args) {
        // Создаем объект для сериализации
        MyClass objectToSerialize = new MyClass("Hello, serialization!");

        // Сериализация объекта
        try (FileOutputStream fileOut = new FileOutputStream("object.ser");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            out.writeObject(objectToSerialize);
            System.out.println("Объект успешно сериализован в object.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Десериализация объекта
        try (FileInputStream fileIn = new FileInputStream("object.ser");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {

            MyClass deserializedObject = (MyClass) in.readObject();
            System.out.println("Объект успешно десериализован: " + deserializedObject.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class MyClass implements Serializable {
    private String message;

    public MyClass(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
```
В этом примере создается объект **`MyClass`** с сообщением, который сериализуется в файл _"object.ser_"

### 🚀Практика

В данной работе представлена реализация 1 задания, связанного с вышеописанной темой:
1. Реализация кэширующего прокси с возможностью опциональной настройки
## Задание

Некоторые методы могут выполняться очень долго, хочется иметь возможность кэшировать результаты возврата. Иногда хочется, чтобы результаты расчёта могли сохраняться при перезапуске JVM.
Например, у нас есть интерфейс **`Service`** c методом `doHardWork()`. Повторный вызов этого метода с теми же параметрами должен возвращать рассчитанный результат из кэша.

```java
void run(Service service) {
    double r1 = service.doHardWork("work1", 10); //считает результат
    double r2 = service.doHardWork("work2", 5);  //считает результат
    double r3 = service.doHardWork("work1", 10); //результат из кэша
}
```

Должна быть возможность тонкой настройки кэша:
1.	Указывать с помощью аннотаций, какие методы кэшировать и как: просчитанный результат хранить в памяти JVM или сериализовывать в файле на диск.
2.	Возможность указывать, какие аргументы метода учитывать при определении уникальности результата, а какие игнорировать (по умолчанию все аргументы учитываются). Например, должна быть возможность указать, что `doHardWork()` должен игнорировать значение второго аргумента, уникальность определяется только по `String` аргументу.
```java
double r1 = service.doHardWork("work1", 10); //считает результат
double r2 = service.doHardWork("work1", 5);  //результат из кэша, несмотря на то, что второй аргумент различается
```
3.	Если возвращаемый тип это `List` – возможность указывать максимальное количество элементов в нём. То есть, если нам возвращается `List` с size = 1 млн, мы можем сказать, что в кэше достаточно хранить 100 тыс элементов.
4.	Возможность указывать название файла/ключа, по которому будет храниться значение. Если оно не задано - использовать имя метода.
5.	Если мы сохраняем результат на диск, должна быть возможность указать, что данный файл надо дополнительно сжимать в `zip`-архив.  
6.	Любые полезные настройки на ваш вкус.
7.	Все настройки кэша должны быть _optional_ и иметь дефолтные настройки.
8.	Все возможные исключения должны быть обработаны с понятным описанием, что делать, чтобы избежать ошибок. (Например, если вы пытаетесь сохранить на диск результат метода, но данный результат не сериализуем, надо кинуть исключение с понятным описанием как это исправить).
9.	Логика по кэшированию должна навешиваться с помощью `DynamicProxy`. Должен быть класс **`CacheProxy`** с методом `cache()`, который принимает ссылку на сервис и возвращает кэшированную версию этого сервиса.  **`CacheProxy`** должен тоже принимать в конструкторе некоторые настройки, например рутовую папку в которой хранить файлы, дефолтные настройки кэша и тд.

Дизайн аннотаций, атрибутов аннотаций, классов реализаций остаётся на ваш вкус. Код должен быть читаем, классы не перегружены логикой, классы должны лежать в нужных пакетах.

Пример включения кэширования (можно менять названия классов, методов, аннотаций и атрибутов):

```java
CacheProxy cacheProxy = new CacheProxy(...);
Service service = cacheProxy.cache(new ServiceImpl());
Loader loader = cacheProxy.cache(new LoaderImpl());

interface Service {
    @Cache(cacheType = FILE, fileNamePrefix = "data", zip = true, identityBy = {String.class, double.class})
    List<String> run(String item, double value, Date date);

    @Cache(cacheType = IN_MEMORY, listList = 100_000)
    List<String> work(String item);
}
```

## Описание результатов

Для реализации данного задания был создан интерфейс [**`Service`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/service/Service.java), а также класс, имплементирующий данный интерфейс - [**`ServiceImpl`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/service/ServiceImpl.java).

Кратко о методах [**`Service`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/service/Service.java):
- **`double powerNumber(double number, double degree)`** - метод возведения числа в степень, где `double number` - само число, `double degree` - степень данного числа;
- **`int factorial(int number);`** - метод расчёта факториала числа, где `int number` - число, факториал которого необходимо рассчитать;
- **`List<Integer> dividedWithoutRemainder(int number);`** - метод, возвращающий список делителей числа `int number`, на которое данное число делится без остатка;
- **`int squaring(String word, int number);`** - метод возведения числа в квадрат, где `int number` - возводимое в квадрат число, `String number` - параметр, необходимый для демонстрации возможностей кэширования.

### 📝 Пара слов об аннотации [@Cache](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheData/Cache.java)

Для реализации настраиваемого кэширующего прокси было необходимо реализовать соответствующую аннотацию. В настоящей программе данная аннотация реализована следующим образом:
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    CacheType cacheType() default CacheType.IN_MEMORY;

    String fileNamePrefix() default "";

    Class[] identityBy() default Class.class;

    boolean zip() default false;

    int listSize() default 0;
}
```

Параметры аннотации:
1. [**`CacheType`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheData/CacheType.java) - тип кэширования (либо в память, либо в файл). В дефолтном варианте запись производится в память.
```java
public enum CacheType {
    FILE,
    IN_MEMORY
}
```
2. **`String fileNamePrefix()`** - префикс к названию файла. По дефолту отсутствует.
3. **`Class[] identityBy()`** - массив классов, на основании которого определяется уникальность метода и, соответственно, осуществляется кэширование. По дефолту представлен `Class.class`.
4. **`boolean zip()`** - флаг, по которому определяется, необходимо ли кэшировать данные в _zip-архив_. По дефолту имеет значение `false`.
5. **`int listSize()`** - в случае, если метод возвращает список каких-либо элементов, позволяет ограничить количество значений данного списка, сохраняемых в кэш.

### 🛰️ Переходим к Прокси

Для создания прокси был создан класс [**`CacheProxy`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheProxy/CacheProxy.java). Данный класс обладает всего одним методом `cache`, который принимает в качестве параметров объект, который необходимо проксировать, путь к папке, где будут храниться файлы кэша, и непосредственно класс, к которому нужно привести наш прокси.
```java
public class CacheProxy {

    public static <T> T cache(T object, String cacheFolder, Class<T> clazz) {
        var invocationHandler = new CacheHandler(object, cacheFolder);

        return clazz.cast(Proxy.newProxyInstance(
                object.getClass().getClassLoader(),
                new Class[]{clazz},
                invocationHandler));
    }
}
```

В методе `cache` вы можете наблюдать создание объекта класса [**`CacheHandler`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheProxy/CacheHandler.java) - непосредственно класс, который и будет являться обработчиком запросов прокси.

Класс принимает в конструктор 2 параметра:
- `T object` - объект, который он будет проксировать;
- `String cacheFolder` - путь к папке, в которую будут сохраняться файлы кэша.

Также у [**`CacheHandler`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheProxy/CacheHandler.java) есть такие поля, как:
- `private final Map<Method, CacheSettings> cacheSettingsMap = new HashMap<>()` - хэшмапа, содержащая настройки аннотации для каждого метода;
- `private final Map<Method, Map<List<Object>, Object>> methodCache = new HashMap<>()` - хэшмапа, содержащая кэш результатов выполнения методов.

[**`CacheSettings`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheData/CacheSettings.java) - специальный класс, для хранения значений параметров аннотации. Конструктор данного класса выглядит следующим образом:

```java
public CacheSettings(CacheType cacheType, String fileNamePrefix, Class[] identityBy, boolean zip, int listSize) {
        this.cacheType = cacheType;
        this.fileNamePrefix = fileNamePrefix;
        this.classes = identityBy;
        this.zip = zip;
        this.listSize = listSize;
    }
```
Данный класс необходим, чтобы снизить количество обращений к аннотации метода. Сохранение настроек кэша происходит в методе `public Object invoke`, чтобы в дальнейшем обращаться не к методу, а к мапе **`cacheSettingsMap`**.

Помимо `invoke` класс [**`CacheHandler`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheProxy/CacheHandler.java) содержит следующие методы:
- **`private Object invokeMethod(Method method, Object[] args)`** - принимает метод и определяет, как его обрабатывать в соответствии с ранее полученными настройками кэша.
- **`private void saveToFile(Method method, List<Object> key, Object result, CacheSettings cacheSettings)`** - принимает метод, идентифицирующие классы, результат и сохраняет его в файл в соответствии с настройками аннотации для данного метода.
- **`private void saveToMemory(Method method, List<Object> key, Object result, CacheSettings cacheSettings)`** - принимает метод, идентифицирующие классы, результат и сохраняет его в память в соответствии с настройками аннотации для данного метода.
- **`private Object readFromFile(Method method, List<Object> key, CacheSettings cacheSettings)`** - принимает метод, идентифицирующие классы и настройки кэша для чтения необходимого результата из файла.
- **`private List<Object> getKey(Method method, Object[] args)`** - получает уникальный набор идентифицирующих классов метода для дальнейшей работы с ними.

### 🗝️ Пара слов об идентифицирующих классах

В аннотации [**`@Cache`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/cacheData/Cache.java) есть такой параметр, как `identityBy`. Он используется для определения классов, по которым происходит идентификация объекта кэша. Это означает, что при вызове метода с определенными аргументами, результат его выполнения будет кэшироваться отдельно для каждой уникальной комбинации аргументов, которые принадлежат указанным классам. Т.е. параметр `identityBy` позволяет управлять тем, какие аргументы метода должны быть учтены при определении уникальности кэша, и предоставляет гибкость в реализации кэширования методов.

Непосредственно сама уникальность определяется путём добавления хэш-кода на основании передаваемых в метод классов и их значений.

Так, если взять метод 
```java
@Cache(cacheType = FILE, identityBy = {String.class})
    int squaring(String word, int number);
```
 из интерфейса [**`Service`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/service/Service.java), и проверить его в классе [**`Main`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/Main.java):

 ```java
 int square1 = cachedService.squaring("Овал", 8);
 System.out.println("Результат 4: " + square1);

 int squareCached1 = cachedService.squaring("Овал", 8);
 System.out.println("Кэшированный результат 4: " + squareCached1);


 int square2 = cachedService.squaring("Кружочек", 8);
 System.out.println("Результат 5: " + square2);

 int squareCached2 = cachedService.squaring("Кружочек", 8);
 System.out.println("Кэшированный результат 5: " + squareCached2);
 ```
То мы получим совершенно разные хэш-коды в наименовании файлов:
![squaring](https://github.com/MironovNikita/sber-homework8/blob/main/res/squaring.png)

Таким образом, можно сделать вывод, что кэширование осуществляется непосредственно по классу **`String`**, который и указан в аннотации, так как второй параметр - `int` не изменялся.

### 
🔬Проведём небольшое тестирование в классе [**`Main`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/Main.java). Указываем необходимую директорию для хранения наших файлов с кэшем, затем создаём объект **`Service`** и, наконец, реализуем наш кэширующий прокси с помощью **`CacheProxy.cache`**.

```java
String cacheFolder = "src/main/java/org/example/proxyData";

Service service = new ServiceImpl();
Service cachedService = CacheProxy.cache(service, cacheFolder, Service.class);
```

Затем осуществим вызов ряда методов, обозначенных в интерфейсе [**`Service`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/main/java/org/example/service/Service.java). В результате работы программы получим следующее:

![mainResults](https://github.com/MironovNikita/sber-homework8/blob/main/res/mainResults.png)

Также в указанной нами директории [**`proxyData`**](https://github.com/MironovNikita/sber-homework8/tree/main/src/main/java/org/example/proxyData) полявились файлы, в которых сохранены результаты вызовов методов:

![mainFolder](https://github.com/MironovNikita/sber-homework8/blob/main/res/mainFolder.png)

Цифры, указанные в названии файлов - хэш код, вычисленный на основании передаваемых в метод параметров. При повторном запуске программы в классе **`Main`** получим следующий результат:

![mainResultsRepeat](https://github.com/MironovNikita/sber-homework8/blob/main/res/mainResultsRepeat.png)

Таким образом, можем сделать вывод, что кэширование в файлы и считывание результатов из них проходит успешно!
 
### Ни Main'ом единым 🌐

Также для проверки кэширующего прокси было написано несколько простых тестов, проверяющих работу методов и считывания сохранённых результатов в кэш. Все тесты находятся в классе [**`CacheProxyTest`**](https://github.com/MironovNikita/sber-homework8/blob/main/src/test/java/org/example/cacheProxy/CacheProxyTest.java). Все файлы, которые создаются в результате данных тестов сохраняются в директорию [**`testProxyData`**](https://github.com/MironovNikita/sber-homework8/tree/main/src/test/java/testProxyData).

Результаты выполнения тестов:

![testResults](https://github.com/MironovNikita/sber-homework10/blob/main/res/testResults.png)

Также в консоли происходит отображение работы прокси с передаваемыми его методам параметрами:

![testConsole](https://github.com/MironovNikita/sber-homework8/blob/main/res/testConsole.png)

В результате работы тестов в вышеуказанной директории также появились файлы, содержащие кэш для вызванных методов:

![testFolder](https://github.com/MironovNikita/sber-homework8/blob/main/res/testFolder.png)

Как можно видеть из вновь созданных файлов, в результате изменения передаваемых значений изменилась хэш-сумма, указанная в наименовании самих файлов. Таким образом, с помощью параметра `identityBy` мы можем гибко настраивать уникальность наших вычислений и кэшировать различные данные.

### 📌 Важно 📌

Если вы хотите проверить работу сохранения файлов и записи методов в кэш, вам необходимо удалить вышеупомянутые файлы из директорий сохранения, либо же поменять параметры методов. Также можно изменить папку сохранения данных файлов, но данный шаг может внести путаницу, где по итогу сохраняются последние файлы кэша, что в свою очередь может привести к некорректной работе программы.

### 💡 Примечание

Тесты написаны с помощью библиотеки JUnit (*junit-jupiter*). Соответствующая зависимость добавлена в [**`pom.xml`**](https://github.com/MironovNikita/sber-homework8/blob/main/pom.xml) 

Версия зависимости прописана в блоке *properties /properties*:

```java
<junit.version>5.10.1</junit.version>
```

Результат сборки проекта:

```java
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.292 s
[INFO] Finished at: 2024-02-01T01:24:20+03:00
[INFO] ------------------------------------------------------------------------
```






