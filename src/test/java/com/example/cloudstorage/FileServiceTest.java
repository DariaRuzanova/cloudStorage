package com.example.cloudstorage;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.model.AuthentificationRequest;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.model.NewFileName;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.service.AuthentificationService;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CloudStorageApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class FileServiceTest {
    private final String fileName = "text.txt";
    private final NewFileName fileNameNew = new NewFileName("text2.txt");
    private User user;
    private File file;
    private final String fileNameTest = "test.txt";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AuthentificationService authentificationService;

    @Autowired
    private com.example.cloudstorage.service.FileService fileService;



//    @BeforeAll
//    void setUp() {
//        user = User.builder()
//                .id(4L)
//                .login("sasha")
//                .password("nmbqwe")
//                .build();
//        file = File.builder()
//                .fileName("test.txt")
//                .type(MediaType.TEXT_PLAIN_VALUE)
//                .fileContent("abracadabra".getBytes())
//                .createData(Date.from(Instant.now()))
//                .size(7L)
//                .user(user)
//                .build();
//
//
//    }
    @SneakyThrows
    public MultipartFile multipartFileGet(){
        MultipartFile multipartFile2 = Mockito.mock(MultipartFile.class);
        URL resource = getClass().getClassLoader().getResource(fileNameTest);

        URLConnection urlConnection = Objects.requireNonNull(resource).openConnection();
        byte[] content = ((InputStream) urlConnection.getContent()).readAllBytes();
        String contentMimeType = urlConnection.getContentType();

        Mockito.when(multipartFile2.getContentType()).thenReturn(contentMimeType);
        Mockito.when(multipartFile2.getBytes()).thenReturn(content);
        Mockito.when(multipartFile2.getSize()).thenReturn((long) content.length);

        return multipartFile2;
    }

    @Test
    public void uploadFileTest() throws IOException {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();

        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);

        URL resource = getClass().getClassLoader().getResource(fileName);
        URLConnection connection = Objects.requireNonNull(resource).openConnection();
        byte[] content = ((InputStream) connection.getContent()).readAllBytes();
        String contentMimeType = connection.getContentType();

        Mockito.when(multipartFile.getContentType()).thenReturn(contentMimeType);
        Mockito.when(multipartFile.getBytes()).thenReturn(content);
        Mockito.when(multipartFile.getSize()).thenReturn((long) content.length);

        ResponseEntity<String> result = fileService.uploadFile(authToken, fileName, multipartFile);
        assertNotNull(result);
    }

    @Test
    public void renameFileTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        ResponseEntity<String> actual = fileService.renameFile(authToken, fileName, fileNameNew);
        ResponseEntity<String> expected = ResponseEntity.ok().body(
                "Имя файла " + fileName + " изменено на " + fileNameNew.getFileName());
        assertEquals(expected, actual);
    }


    @Test
    public void getFileTest() throws IOException {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();

        fileService.deleteFile(authToken, fileNameTest);

        MultipartFile multipartFile = multipartFileGet();
        fileService.uploadFile(authToken, fileNameTest, multipartFile);
        ResponseEntity<byte[]> fileContent = fileService.getFile(authToken, fileNameTest);
        assertArrayEquals(multipartFile.getBytes(), fileContent.getBody());
    }
}