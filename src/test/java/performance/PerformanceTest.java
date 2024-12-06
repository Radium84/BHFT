package performance;

import helpers.common.TestValues;
import helpers.httpconnection.ApiHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static helpers.common.TestValues.CONTAINER_PORT;

@Testcontainers
public class PerformanceTest extends TestValues {
    @Test
    @DisplayName("Отправка N сообщений регулируемой длины")
    void loadTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String str = StringUtils.repeat("a", 10);
        apiHelper.loadPost("/todos", mappedPort, str,100000);
        String logs = todoAppContainer.getLogs();
        analyzeLogs(logs);

    }
    @Test
    @DisplayName("Многопоточка на базе ExecutorService")
    void loadTestWithExecutorService() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String str = StringUtils.repeat("a", 100);
        int numThreads = 2;
        int messagesPerThread = 10; // количество сообщений на поток
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            final int localMessagesPerThread = messagesPerThread;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < localMessagesPerThread; j++) {
                        apiHelper.loadPost("/todos", mappedPort, str, 10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
        System.out.println(mappedPort);
        String logs = todoAppContainer.getLogs();
        analyzeLogs(logs);

    }
    //разбор логов
    private void analyzeLogs(String logs) {
        List<LocalDateTime> timestamps = new ArrayList<>();
        boolean redFlag = false;
        String resultStatusCode = "201";
        String[] logLines = logs.split("n");
        Pattern timestampPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6})Z");
        Pattern statusPattern = Pattern.compile("POST /todos HTTP/1\\.1\" (\\d{3})");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        // Извлекаем временные метки
        for (String line : logLines) {
            Matcher matcher = timestampPattern.matcher(line);
            if (matcher.find()) {
                String timestamp = matcher.group(1);
                timestamps.add(LocalDateTime.parse(timestamp, formatter));
            }
            Matcher statusMatcher = statusPattern.matcher(line);
            if (statusMatcher.find()) {
                String statusCode = statusMatcher.group(1);
                if (!statusCode.equals("201")) {
                    resultStatusCode = statusCode;
                    redFlag = true;
                }
            }
        }


        if (timestamps.size()>= 2) {
            LocalDateTime firstTimestampStart = timestamps.get(0);
            LocalDateTime firstTimestampEnd = timestamps.get(1);
            LocalDateTime lastTimestampEnd = timestamps.get(timestamps.size() - 1);
            LocalDateTime lastTimestampStart = timestamps.get(timestamps.size() - 2);

            Duration durationTotal = Duration.between(firstTimestampStart, lastTimestampEnd);
            Duration firstPost = Duration.between(firstTimestampStart, firstTimestampEnd);
            Duration lastPost = Duration.between(lastTimestampStart, lastTimestampEnd);
            System.out.println("Анализ производительности:");
            System.out.println("Время начала: " + firstTimestampStart);
            System.out.println("Время окончания: " + lastTimestampEnd);
            System.out.println(String.format("Статус наличия ошибок: %s, код обработки %s",redFlag, resultStatusCode));
            System.out.println("Общая продолжительность: " + durationTotal.toMillis() + " мс");
            System.out.println("Количество обработанных запросов: " + timestamps.size());
            System.out.println("Среднее время на запрос: " +
                    (durationTotal.toMillis() / (double)timestamps.size()) + " мс");
            System.out.println("Время обработки в начале отправки: " + firstPost.toMillis() + " мс");
            System.out.println("Время обработки в конце отправки: " + lastPost.toMillis() + " мс");

        } else {
            System.out.println("Недостаточно данных в логах для анализа");
        }
    }

}
