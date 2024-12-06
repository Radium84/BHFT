package helpers.httpconnection;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketListener implements WebSocket.Listener {
    private CompletableFuture<String> messageFuture = new CompletableFuture<>();

    @Override
    public void onOpen(WebSocket webSocket) {
        webSocket.request(1);
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageFuture.complete(data.toString());
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    // Обработчики для других случаев (опционально)
    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("Ошибка в WebSocket соединении: " + error.getMessage());
        WebSocket.Listener.super.onError(webSocket, error);
    }

    public CompletableFuture<String> getMessageFuture() {
        return messageFuture;
    }
}
