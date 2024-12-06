# Parallel Test Execution with testcontainers

Проект представляет собой тестовый фреймворк + набор API тестов для контейнеризированного REST API приложения. 
Дополнительно - тестирование WS и реализацию нагрузочного тестирования POST /todos

## Описание фреймворка 

При реализации проекта и выборе решений брались в расчет следующие доводы:
* Наличие контейнера с приложением -> для управления контейнерами Testcontainers.
* Для тестирования REST API - Rest Assured, Hamcrest(дефолт)
* При наличии возможности изолировать тесты на уровне контейнеров - решение поднимать на каждый метод новый контейнер,
без переиспользования. Не лучшая практика для больших приложений, но тестове приложение позволяет такой подход, 
даже с учетом потенциала к росту
* Для уменьшения времени запусков - Junit5 настроен на параллельное исполнение тестов с запуском контейнеров по числу тестов
в рамках одного класса через parallel.mode.classes.default=same_thread. Решен вопрос динамического маппинга портов.
* Вынос пропертей приложения в todos.properties для удобного конфигурирования между стендами.
* Константы и тестовые данные вынесены в TestValues. 
* Тестовые данные генерируются на основе payload классов Todos, Ws c использованием DataSerializer, TestValuesCalculate
* В связи с тем, что использование Rest Assured только для создания todos или однотипного чтения ответа некрасиво, 
а также изза не слишком высокой производительности RA при нагрузке, был реализован класс ApiHelper 
на нативном java.net.http.HttpClient. 
* WebSocketListener реализует интерфейс WebSocket.Listener, используется для WS теста.
* Всего реализовано 4 тестовых класса для каждого endpoints, отдельно тест для проверки работы WS. Тесткейсы для тестов
по возможности оптимизированы, под автоматизацию попали с наибольшим приоритетом. 
* Тест WS проверяет тело сообщения, полученного по WS(отдельный тест на подключение к WS избыточен)

## Нагрузка

Поскольку нагрузка была дополнительной задачей, мне было интересно поработать с многопоточкой, а вот прикручивать Jmeter 
Gatling - не очень.

### Сценарий первый: Отправка сообщений с text длиной 10 символов, в один поток, 1000, 10000, 100000 сообщений.

Результат: При длине текста 10 символов, длительность обработки не меняется и равна в среднем 1 мс, 
зависит скорее от скорости отправки.

### Сценарий второй: Отправка сообщений с text переменной длины 10 символов, 1к сиволов, 10к символов,17к символов в один поток, 1000, 10000, 100000 сообщений.

Результат: 
Количество сообщений не влияет на производительность, как и сценарии 1. Логарифмический рост времени обработки
по мере увеличения тела запроса.
При поле text больше 17к симвлов приложение начинает отвечать 413 ошибкой.

### Сценарий третий: Многопоточная отправка сообщений в 2, 10, 50 потоков.
Результат: Уже на 2 потоках приложение начинает отвечать 400 ошибками


## Технологии
* Java 21
* Junit 5.11.3
* Rest Assured 5.5.0
* Hamcrest 3.0
* Testcontainers 1.20.4
* Maven 3.9.8
