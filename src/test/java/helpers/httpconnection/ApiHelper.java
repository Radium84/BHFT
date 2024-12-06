package helpers.httpconnection;

import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.datahelpers.models.Todos;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static helpers.datahelpers.DataSerializer.createJson;
import static helpers.common.TestValues.*;

public class ApiHelper {

    private final HttpClient httpClient;

    public ApiHelper() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> createTodos(String requestBody, String endpoint, int port) throws Exception {

        URI uri = getUri(endpoint, port);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public List<Todos> getTodos(String endpoint, int port) throws Exception {

        URI uri = getUri(endpoint, port);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(
                response.body(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Todos.class)
        );
    }

    public void loadPost(String endpoint, int port, String text, int count) throws Exception {
        for(int i =1; i<count;i++){
            Todos todos = new Todos((long) i,text, true);
            String body = createJson(todos);
        createTodos(body,endpoint,port);
        }
    }

    private static @NotNull URI getUri(String endpoint, int port) throws MalformedURLException, URISyntaxException {
        URL urlObj = new URL(TODOS_URL);
        String protocol = urlObj.getProtocol();
        String host = urlObj.getHost();
        URI uri = new URI(protocol, null, host, port, endpoint, null, null);
        return uri;
    }
}

