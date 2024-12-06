package apitests;

import helpers.httpconnection.ApiHelper;
import helpers.common.TestValues;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * 6. Попытка удаления уже удаленного id
 * 7. Попытка удаления с отрицательным id
 * 8. Попытка удаления с некорректным форматом id (буквы/спецсимволы)
 * 9. Попытка удаления с id = 0
 */
@Testcontainers
public class DeleteTodosTest extends TestValues {
    @Container
    private GenericContainer<?> todoAppContainer = new GenericContainer<>("todo-app")
            .withExposedPorts(Integer.valueOf(CONTAINER_PORT));

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = TODOS_URL;
    }

    @Test
    @DisplayName("Успешное удаление по id")
    void deleteExistId() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        given()
                .auth()
                .preemptive()
                .basic("admin", "admin")
                .port(mappedPort)
                .when()
                .delete("/todos/{id}", 1)
                .then()
                .statusCode(204);
        given()
                .port(mappedPort)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("", empty())
                .body("size()", is(0));
    }

    @ParameterizedTest
    @DisplayName("Попытка удаления без авторизации или с ошибочными кредами")
    @MethodSource("provideAuthTestData")
    void deleteWithDifferentAuth(String username, String password) throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        RequestSpecification request = given()
                .port(mappedPort);
        if (username != null && password != null) {
            request.auth()
                    .preemptive()
                    .basic(username, password);
        }
        request.when()
                .delete("/todos/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Попытка удаления по несуществующему id")
    void deleteNotExistId() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        given()
                .auth()
                .preemptive()
                .basic("admin", "admin")
                .port(mappedPort)
                .when()
                .delete("/todos/{id}", 2)
                .then()
                .statusCode(404);
        given()
                .port(mappedPort)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }


    private static Stream<Arguments> provideAuthTestData() {
        return Stream.of(
                Arguments.of(null, null, 401),
                Arguments.of("wrongAdmin", "admin", 401),
                Arguments.of("admin", "wrongPassword", 401)
        );
    }
}
