package com.example.cloudstorage;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;
import org.junit.ClassRule;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class DBTests {
    private final String login = "petr";
    private final String password = "qwerty";
    private final String authToken = "token";
    private final User user = new User(1L,login,password);
    private final File file1 = new File(1L,"fileName1","fileContent1".getBytes(),user);
    private final File file2 = new File(2L,"fileName2","fileContent2".getBytes(),user);

    UserRepository userRepository;

    FileRepository fileRepository;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>()
            .withDatabaseName("cloud_DB")
            .withUsername("postgres")
            .withPassword("qwerty");

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
