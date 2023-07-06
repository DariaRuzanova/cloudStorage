package com.example.cloudstorage;

import com.example.cloudstorage.model.AuthentificationRequest;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.service.AuthentificationService;
import com.example.cloudstorage.service.FileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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
        assertEquals("petr",user.get().getLogin());

        AuthentificationResponse response = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "qwerty"));
        assertNotNull(response);
        assertNotNull(response.getAuthToken());
    }
    @Test
    public void  uploadFileTest() throws IOException {
        AuthentificationResponse response = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "qwerty"));
        String authToken = response.getAuthToken();

        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);

        URL resource = getClass().getClassLoader().getResource("text.txt");
        URLConnection connection = Objects.requireNonNull(resource).openConnection();
        byte[] content = ((InputStream)connection.getContent()).readAllBytes();
        String contentMimeType = connection.getContentType();

        Mockito.when(multipartFile.getContentType()).thenReturn(contentMimeType);
        Mockito.when(multipartFile.getBytes()).thenReturn(content);
        Mockito.when(multipartFile.getSize()).thenReturn((long) content.length);

        ResponseEntity<String> result = fileService.uploadFile(authToken, "text.txt", multipartFile);
        int ttt = 0;





//
//
//
//        Mockito.when(multipartFile.getById(patientInfo.getId()))
//
//        Mockito.when(.getById(patientInfo.getId()))
//        uploadFile(String authToken, String fileName, MultipartFile multipartFile)
    }
    public void findFileByUserIdAndFileName(){

    }
}
