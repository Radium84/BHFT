package apitests;
import helpers.apihelpers.ApiHelper;
import helpers.common.testValuesTestValues;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@Testcontainers
public class getTestValuesTest extends testValuesTestValues {

    @Container
    private GenericContainer<?> todoAppContainer = new GenericContainer<>("todo-app")
            .withExposedPorts(Integer.valueOf(CONTAINER_PORT));

   @BeforeAll
   static void setUp(){
       RestAssured.baseURI = TODOS_URL;
   }
    @Test
    @DisplayName("Получение списка TODOS без квери, валидация полей")
    void clearGetTest() throws Exception {
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
            ApiHelper apiHelper = new ApiHelper();
            apiHelper.createTodos(body1,  "/todos", mappedPort);
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
    @DisplayName("Получение ответа при отсутсвтии TODOS")
    void emptyGetTest(){
        int mappedPort = todoAppContainer.getMappedPort(Integer.parseInt(CONTAINER_PORT));
            given()
                    .when()
                    .port(mappedPort)
                    .get("/todos")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));


        }
}
