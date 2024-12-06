package helpers.datahelpers;

import helpers.common.TestValues;
import helpers.datahelpers.models.Todos;

import java.io.IOException;
import java.util.Properties;

import static helpers.datahelpers.DataSerializer.createJson;

public class TestValuesCalculate {
    public static Properties properties;

    static {
        try {
            PropertiesLoader propertiesLoader = new PropertiesLoader("todos.properties");
            properties = propertiesLoader.getLoadedProperties();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось загрузить свойства", e);
        }
    }

    {

        TestValues.body1 = createJson(new Todos(1L, "Task 1", true));
        TestValues.body2 = createJson(new Todos(2L, "Task 2", false));
        TestValues.body3 = createJson(new Todos(3L, "Task 3", true));
        TestValues.body4 = createJson(new Todos(4L, "Task 4", false));
        TestValues.body5 = createJson(new Todos(5L, "Task 5", true));
        TestValues.updatedBody = createJson(new Todos(11L, "Updated Text", false));
        TestValues.notCompletedJson = createJson(new Todos(1L, "Task 1", null));

    }

}
