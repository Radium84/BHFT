package datahelpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private final Properties properties;

    public PropertiesLoader(String fileName) throws IOException {
        this.properties = loadProperties(fileName);
    }

    private Properties loadProperties(String fileName) throws IOException {
        Properties props = new Properties();
        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Файл свойств '" + fileName + "' не найден в classpath");
            }
            props.load(inputStream);
        }
        return props;
    }

    public Properties getLoadedProperties() {
        return properties;
    }
}