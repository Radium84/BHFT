package performance;

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
public class PerformanceTest {
    @Container
    private GenericContainer<?> todoAppContainer = new GenericContainer<>("todo-app")
            .withExposedPorts(Integer.valueOf(CONTAINER_PORT));
    @Test
    @DisplayName("Отправка N сообщений регулируемой длины")
    void wsTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String str = StringUtils.repeat("a", 100000);
        apiHelper.loadPost("/todos", mappedPort, str,1000);
        String logs = todoAppContainer.getLogs();
        analyzeLogs(logs);

    }
    @Test
    @DisplayName("Многопоточка на базе ExecutorService")
    void wsTestWithExecutorService() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String str = StringUtils.repeat("a", 10000);
        int numThreads = 30;
        int messagesPerThread = 100; // количество сообщений на поток
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

    private void analyzeLogs(String logs) {
        List<LocalDateTime> timestamps = new ArrayList<>();
        String[] logLines = logs.split("n");
        Pattern timestampPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6})Z");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        // Извлекаем временные метки
        for (String line : logLines) {
            Matcher matcher = timestampPattern.matcher(line);
            if (matcher.find()) {
                String timestamp = matcher.group(1);
                timestamps.add(LocalDateTime.parse(timestamp, formatter));
            }
        }

        if (timestamps.size() >= 2) {
            LocalDateTime firstTimestamp = timestamps.get(0);
            LocalDateTime lastTimestamp = timestamps.get(timestamps.size() - 1);

            Duration duration = Duration.between(firstTimestamp, lastTimestamp);

            System.out.println("Анализ производительности:");
            System.out.println("Время начала: " + firstTimestamp);
            System.out.println("Время окончания: " + lastTimestamp);
            System.out.println("Общая продолжительность: " + duration.toMillis() + " мс");
            System.out.println("Количество обработанных запросов: " + timestamps.size());
            System.out.println("Среднее время на запрос: " +
                    (duration.toMillis() / (double)timestamps.size()) + " мс");
        } else {
            System.out.println("Недостаточно данных в логах для анализа");
        }
    }

}
