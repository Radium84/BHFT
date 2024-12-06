package apitests;

import helpers.common.TestValues;
import helpers.httpconnection.ApiHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Реализовать:
 * 6. Параметризованный тест: limit=0, limit больше количества объектов todos
 * 7. Параметризованный тест: offset=0, offset равен количеству объектов todos
 * 8. Комбинация limit и offset, превышающая количество задач
 * 9. Обработка специальных символов в поле text
 * 10. Параметризованный тест: отрицательные значения limit и offset, некорректные значения limit и offset
 * 12. Тестирование с большим количеством задач
 * 13. Игнорирование неизвестных параметров запроса
 */
@Testcontainers
public class GetTodosTest extends TestValues {
    @Test
    @DisplayName("Получение списка TODOS без квери, валидация полей")
    void clearGetTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.createTodos(body1, "/todos", mappedPort);
        apiHelper.createTodos(body2, "/todos", mappedPort);
        given()
                .when()
                .port(mappedPort)
                .get("/todos")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("$", hasItems(
                        allOf(
                                hasEntry(is("id"), is((Object) 1)),
                                hasEntry(is("text"), is((Object) "Task 1")),
                                hasEntry(is("completed"), is((Object) true))
                        ),
                        allOf(
                                hasEntry(is("id"), is((Object) 2)),
                                hasEntry(is("text"), is((Object) "Task 2")),
                                hasEntry(is("completed"), is((Object) false))
                        )
                ));
    }

    @Test
    @DisplayName("Получение ответа при отсутствии TODOS")
    void emptyGetTest() {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        given()
                .when()
                .port(mappedPort)
                .get("/todos")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));


    }

    @Test
    @DisplayName("Получение ограниченного числа TODOS ")
    void limitedGetTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String[] bodies = {body1, body2, body3, body4, body5};
        for (String body : bodies) {
            apiHelper.createTodos(body, "/todos", mappedPort);
        }
        given()
                .when()
                .port(mappedPort)
                .queryParam("limit", 3)
                .get("/todos")
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("$", hasItems(
                        allOf(
                                hasEntry(is("id"), is(1))
                        ),
                        allOf(
                                hasEntry(is("id"), is(2))
                        ),
                        allOf(
                                hasEntry(is("id"), is(3))
                        )
                ));


    }

    @Test
    @DisplayName("Пропуск определенного числа TODOs")
    void offsetGetTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String[] bodies = {body1, body2, body3, body4, body5};
        for (String body : bodies) {
            apiHelper.createTodos(body, "/todos", mappedPort);
        }
        given()
                .when()
                .port(mappedPort)
                .queryParam("offset", 2)
                .get("/todos")
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("$", hasItems(
                        allOf(
                                hasEntry(is("id"), is(3))
                        ),
                        allOf(
                                hasEntry(is("id"), is(4))
                        ),
                        allOf(
                                hasEntry(is("id"), is(5))
                        )
                ));


    }

    @Test
    @DisplayName("limit и offset: комбинированное использование")
    void offsetAndLimitGetTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
        ApiHelper apiHelper = new ApiHelper();
        String[] bodies = {body1, body2, body3, body4, body5};
        for (String body : bodies) {
            apiHelper.createTodos(body, "/todos", mappedPort);
        }
        given()
                .when()
                .port(mappedPort)
                .queryParam("offset", 1)
                .queryParam("limit", 2)
                .get("/todos")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("$", hasItems(
                        allOf(
                                hasEntry(is("id"), is(2))
                        ),
                        allOf(
                                hasEntry(is("id"), is(3))
                        )
                ));


    }


}
