package helpers.datahelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.datahelpers.models.Todos;

public class DataSerializer {
    public static String createJson(Todos todos) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(todos);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при преобразовании в JSON", e);
        }
    }
}

