package helpers.common;

import helpers.datahelpers.TestValuesCalculate;

public class TestValues extends TestValuesCalculate {


    public static String CONTAINER_PORT = properties.getProperty("todos.container.port");
    public static String TODOS_URL = properties.getProperty("todos.url");
    public static String TODOS_WS = properties.getProperty("todos.ws");


    public static String body1;
    public static String body2;
    public static String body3;
    public static String body4;
    public static String body5;
    public static String updatedBody;
    public static String notCompletedJson;


}
