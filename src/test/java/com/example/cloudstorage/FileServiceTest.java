package com.example.cloudstorage;


import com.example.cloudstorage.exception.FileNotFoundException;
import com.example.cloudstorage.model.AuthentificationRequest;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.model.FileData;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.service.AuthentificationService;
import lombok.SneakyThrows;
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
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CloudStorageApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class FileServiceTest {

    private final String fileName = "text.txt";
    private final String fileNameNew = "text2.txt";

    @Autowired
    private AuthentificationService authentificationService;
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private com.example.cloudstorage.service.FileService fileService;

    @SneakyThrows
    public MultipartFile multipartFileGet(String fileNameTest) {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        URL resource = getClass().getClassLoader().getResource(fileNameTest);

        URLConnection urlConnection = Objects.requireNonNull(resource).openConnection();
        byte[] content = ((InputStream) urlConnection.getContent()).readAllBytes();
        String contentMimeType = urlConnection.getContentType();

        Mockito.when(multipartFile.getContentType()).thenReturn(contentMimeType);
        Mockito.when(multipartFile.getBytes()).thenReturn(content);
        Mockito.when(multipartFile.getSize()).thenReturn((long) content.length);

        return multipartFile;
    }

    @Test
    public void uploadFileTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        MultipartFile multipartFile = multipartFileGet(fileName);
        if (fileRepository.findFileByFileName(fileName).isPresent()) {
            fileService.deleteFile(authToken, fileName);
        }
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
                "Имя файла " + fileName + " изменено на " + fileNameNew);
        assertEquals(expected, actual);
    }

    @Test
    public void getFileTest() throws IOException {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        String fileNameTest = "test.txt";
        if (fileRepository.findFileByFileName(fileName).isPresent()) {
            fileService.deleteFile(authToken, fileName);
        }
        MultipartFile multipartFile = multipartFileGet(fileNameTest);
        fileService.uploadFile(authToken, fileNameTest, multipartFile);
        ResponseEntity<byte[]> fileContent = fileService.getFile(authToken, fileNameTest);
        assertArrayEquals(multipartFile.getBytes(), fileContent.getBody());
    }

    @Test
    public void deleteFileTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        String fileNameTest2 = "testDeleteFile.txt";
        MultipartFile multipartFile = multipartFileGet(fileNameTest2);
        fileService.uploadFile(authToken, fileNameTest2, multipartFile);
        ResponseEntity<String> actual = fileService.deleteFile(authToken, fileNameTest2);
        ResponseEntity<String> expected = ResponseEntity.ok().body("Файл " + fileNameTest2 + " удален");
        assertEquals(expected, actual);
    }

    @Test
    public void deleteFileTestException() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        assertThrows(FileNotFoundException.class, () -> {
            fileService.deleteFile(authToken, "testDeleteFileException.txt");
        });
    }


    @Test
    public void getAllFilesTest() {
        int limit = 2;
        String fileName1 = "test.txt";
        String fileName2 = "text.txt";
        MultipartFile multipartFile1 = multipartFileGet(fileName1);
        MultipartFile multipartFile2 = multipartFileGet(fileName2);
        List<FileData> list = List.of(
                FileData.builder().fileName(fileName1).size(multipartFile1.getSize()).build(),
                FileData.builder().fileName(fileName2).size(multipartFile2.getSize()).build()
        );
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();

//        fileService.uploadFile(authToken,fileName1,multipartFile1);
        fileService.uploadFile(authToken, fileName2, multipartFile2);

        ResponseEntity<List<FileData>> actual = fileService.getAllFiles(authToken, limit);
        ResponseEntity<List<FileData>> expected = ResponseEntity.ok().body(list);

        assertEquals(expected, actual);

    }
}