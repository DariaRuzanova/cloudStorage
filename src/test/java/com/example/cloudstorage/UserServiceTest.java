package com.example.cloudstorage;

import com.example.cloudstorage.exception.SessionException;
import com.example.DTO.AuthentificationRequest;
import com.example.DTO.AuthentificationResponse;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.service.AuthentificationService;
import com.example.cloudstorage.service.FileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

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
    private FileRepository fileRepository;

    @Autowired
    private AuthentificationService authentificationService;


    @Autowired
    private FileService fileService;

    @Test
    public void findUserByLoginAndPasswordTest() {
        var user = userRepository.findUserByLoginAndPassword("petr", "qwerty");
        assertTrue(user.isPresent());
        assertEquals("petr", user.get().getLogin());
    }

    @Test
    public void authentificationLoginTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "qwerty"));
        assertNotNull(response);
        assertNotNull(Objects.requireNonNull(response.getBody()).getAuthToken());
        assertNotEquals(0,response.getBody().getAuthToken().length());
    }
    @Test
    public void authentificationLoginExceptionTest(){

        assertThrows(ResponseStatusException.class, ()->{
            authentificationService.authentificationLogin(new AuthentificationRequest("petr", "111222"));
        });
    }


    @Test
    public void logoutTest(){
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();

        ResponseEntity<Void> actual = authentificationService.logout(authToken);
        ResponseEntity<Void> expected = ResponseEntity.ok().body(null);
        assertEquals(expected,actual);

        assertThrows(SessionException.class, ()-> {
            authentificationService.logout(authToken);
        });
    }

}
