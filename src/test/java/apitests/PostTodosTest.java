package apitests;

import helpers.common.TestValues;
import helpers.datahelpers.models.Todos;
import helpers.httpconnection.ApiHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.http.HttpResponse;
import java.util.stream.Stream;

import static helpers.datahelpers.DataSerializer.createJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 6. Лишние поля в Json файле
 * 7. Использование невалидных типов данных в теле
 * 8. Отправка пустого тела запроса
 * 9. Минимальные длины поля text (0)
 */
@Testcontainers
public class PostTodosTest extends TestValues {

    static Stream<Arguments> provideJsonProcessingParams() {
        return Stream.of(
                Arguments.of(new Todos(-1L, "Task 2", false)),
                Arguments.of(new Todos(1L, "Task 2", null))
        );
    }

    @Test
    @DisplayName("Проверка создания одного TODOS")
    void postOneTodos() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        HttpResponse<String> createResponse = apiHelper.createTodos(body1, "/todos", mappedPort);
        assertEquals(createResponse.statusCode(), 201, "При создании TODOS пришел ответ: " + createResponse.statusCode());
        //проверку тела мы уже выполнили в рамках тестирования Get /todos

    }

    @Test
    @DisplayName("Создание сущности с существующим id")
    void postDoubledId() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        HttpResponse<String> createResponse = apiHelper.createTodos(body1, "/todos", mappedPort);
        assertEquals(createResponse.statusCode(), 400, "Вместо 400 получили: " + createResponse.statusCode());

    }

    @ParameterizedTest
    @MethodSource("provideJsonProcessingParams")
    @DisplayName("Проверка недопустимых запросов")
    public void testInvalidTodosRequests(Todos todos) throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String body = createJson(todos);
        HttpResponse<String> response = apiHelper.createTodos(body, "/todos", mappedPort);
        assertEquals(400, response.statusCode(), "Ожидаемый код ответа 400");
    }

    @Test
    @DisplayName("Создание сущности с существующим id>Int 64-bit")
    void postBigId() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String bodyWithBigId = "{ \"id\": 99999999999999999999, \"text\": \"Task 1\", \"completed\": true }";
        apiHelper.createTodos(bodyWithBigId, "/todos", mappedPort);
        HttpResponse<String> createResponse = apiHelper.createTodos(bodyWithBigId, "/todos", mappedPort);
        assertEquals(400, createResponse.statusCode(), "Вместо 400 получили: " + createResponse.statusCode());


    }


}
