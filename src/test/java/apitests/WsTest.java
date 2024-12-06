package apitests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.common.TestValues;
import helpers.datahelpers.models.Todos;
import helpers.datahelpers.models.Ws;
import helpers.httpconnection.ApiHelper;
import helpers.httpconnection.WebSocketListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
public class WsTest extends TestValues {

    @Test
    @DisplayName("Проверка обновления по WS")
    void wsTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        String wsUri = String.format("%s:%s/ws", TODOS_WS, mappedPort);
        HttpClient client = HttpClient.newHttpClient();
        WebSocketListener listener = new WebSocketListener();
        CompletableFuture<WebSocket> wsFuture = client.newWebSocketBuilder()
                .buildAsync(URI.create(wsUri), listener);
        WebSocket webSocket = wsFuture.join();
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        String receivedData = listener.getMessageFuture().get(5, TimeUnit.SECONDS);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(receivedData);
        Ws ws = objectMapper.treeToValue(jsonNode, Ws.class);
        Todos todos = ws.getData();
        assertAll(
                () -> assertEquals(1L, todos.getId().longValue()),
                () -> assertEquals("Task 1", todos.getText()),
                () -> assertEquals(true, todos.getCompleted()));
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Test complete").join();

    }

}
