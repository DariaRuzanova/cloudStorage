package com.example.cloudstorage;

import com.example.cloudstorage.DTO.AuthentificationRequest;
import com.example.cloudstorage.DTO.AuthentificationResponse;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.service.AuthentificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CloudStorageApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class UserServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthentificationService authentificationService;


    @Test
    public void findUserByLoginAndPasswordTest() {
        var user = userRepository.findUserByLoginAndPassword("petr", "qwerty");
        assertTrue(user.isPresent());
        assertEquals("petr", user.get().getLogin());
    }

    @Test
    public void authentificationLoginTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "qwerty"));
        assertNotNull(response);
        assertNotNull(Objects.requireNonNull(response).getAuthToken());
        assertNotEquals(0, response.getAuthToken().length());
    }

    @Test
    public void authentificationLoginExceptionTest() {
        AuthentificationResponse actual = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "111222"));
        assertNull(actual);
    }


    @Test
    public void logoutTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();

        boolean actual = authentificationService.logout(authToken);
        boolean expected = true;
        assertEquals(expected, actual);
    }
}
