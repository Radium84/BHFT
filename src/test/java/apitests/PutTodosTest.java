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

import java.util.List;
import java.util.stream.Stream;

import static helpers.datahelpers.DataSerializer.createJson;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * 6. Лишние поля в Json файле
 * 7. Использование невалидных типов данных в теле
 * 8. Отправка пустого тела запроса
 * 9. Обращение к невалидному типу ресурса ("abc")
 */
@Testcontainers
public class PutTodosTest extends TestValues {
    static Stream<Arguments> provideJsonProcessingParams() {
        return Stream.of(
                Arguments.of(new Todos(0L, "Task 2", false)),
                Arguments.of(new Todos(2L, "", true))
        );
    }

    @Test
    @DisplayName("Обновление todo по существующему id")
    void updateCorrectId() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        given()
                .port(mappedPort)
                .body(updatedBody)
                .header("Content-Type", "application/json")
                .when()
                .put("/todos/{id}", 1)
                .then()
                .statusCode(200);
        List<Todos> todos = apiHelper.getTodos("/todos", mappedPort);
        assertAll(
                () -> assertEquals(11L, todos.get(0).getId().longValue()),
                () -> assertEquals("Updated Text", todos.getFirst().getText()),
                () -> assertEquals(false, todos.getFirst().getCompleted()));

    }

    @Test
    @DisplayName("Обновление todo по несуществующему id")
    void updateWrongId() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        given()
                .port(mappedPort)
                .body(updatedBody)
                .header("Content-Type", "application/json")
                .when()
                .put("/todos/{id}", 11)
                .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("Попытка присвоить уже существующий id")
    void updateExistId() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        apiHelper.createTodos(body2, "/todos", mappedPort);
        given()
                .port(mappedPort)
                .body(body1) //id =1 уже существует
                .header("Content-Type", "application/json")
                .when()
                .put("/todos/{id}", 2)
                .then()
                .statusCode(400);

    }

    @Test
    @DisplayName("Попытка обновить todo некорректным json")
    void updateWrongBody() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        given()
                .port(mappedPort)
                .body(notCompletedJson)
                .header("Content-Type", "application/json")
                .when()
                .put("/todos/2")
                .then()
                .statusCode(400);

    }

    @ParameterizedTest
    @MethodSource("provideJsonProcessingParams")
    @DisplayName("Проверка граничных значений")
    public void testInvalidTodosRequests(Todos todos) throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        String body = createJson(todos);
        given()
                .port(mappedPort)
                .body(body)
                .header("Content-Type", "application/json")
                .when()
                .put("/todos/{id}", 1)
                .then()
                .statusCode(200);
    }


}

