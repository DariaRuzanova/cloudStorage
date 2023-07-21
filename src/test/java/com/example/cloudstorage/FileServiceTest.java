package com.example.cloudstorage;


import com.example.cloudstorage.DTO.AuthentificationRequest;
import com.example.cloudstorage.DTO.AuthentificationResponse;
import com.example.cloudstorage.exception.DuplicateFileNameException;
import com.example.cloudstorage.exception.FileNotFoundException;
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
import org.springframework.web.server.ResponseStatusException;

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

    @SneakyThrows
    @Test
    public void uploadFileTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        MultipartFile multipartFile = multipartFileGet(fileName);
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isPresent()) {
            fileService.deleteFile(authToken, fileName);
        }
        String contentType = multipartFile.getContentType();
        byte[] bytes = multipartFile.getBytes();
        long sizeFile = multipartFile.getSize();
        ResponseEntity<String> result = fileService.uploadFile(authToken, fileName, bytes, contentType, sizeFile);
        assertNotNull(result);
    }

    @SneakyThrows
    @Test
    public void uploadFileTestException() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        MultipartFile multipartFile1 = multipartFileGet(fileName);
        String contentType = multipartFile1.getContentType();
        byte[] bytes = multipartFile1.getBytes();
        long sizeFile = multipartFile1.getSize();
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, bytes, contentType, sizeFile);
        }
        assertThrows(ResponseStatusException.class, () -> {
            fileService.uploadFile(authToken, fileName, bytes, contentType, sizeFile);
        });
    }

    @Test
    public void renameFileTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileNameNew).isPresent()) {
            fileService.deleteFile(authToken, fileNameNew);
        }
        ResponseEntity<String> actual = fileService.renameFile(authToken, fileName, fileNameNew);
        ResponseEntity<String> expected = ResponseEntity.ok().body(
                "Имя файла " + fileName + " изменено на " + fileNameNew);
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    public void renameFileTestException() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        MultipartFile multipartFile1 = multipartFileGet(fileName);
        MultipartFile multipartFile2 = multipartFileGet(fileNameNew);
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, multipartFile1.getBytes(), multipartFile1.getContentType(), multipartFile1.getSize());
        }
        if (fileRepository.findFileByUserIdAndFileName(userId, fileNameNew).isEmpty()) {
            fileService.uploadFile(authToken, fileNameNew, multipartFile2.getBytes(), multipartFile2.getContentType(), multipartFile2.getSize());
        }

        assertThrows(DuplicateFileNameException.class, () -> {
            fileService.renameFile(authToken, fileName, fileNameNew);
        });
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
        String contentType = multipartFile.getContentType();
        byte[] bytes = multipartFile.getBytes();
        long sizeFile = multipartFile.getSize();
        fileService.uploadFile(authToken, fileNameTest, bytes, contentType, sizeFile);
        ResponseEntity<byte[]> fileContent = fileService.getFile(authToken, fileNameTest);
        assertArrayEquals(bytes, fileContent.getBody());
    }

    @SneakyThrows
    @Test
    public void deleteFileTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        String fileNameTest2 = "testDeleteFile.txt";
        MultipartFile multipartFile = multipartFileGet(fileNameTest2);
        String contentType = multipartFile.getContentType();
        byte[] bytes = multipartFile.getBytes();
        long sizeFile = multipartFile.getSize();
        fileService.uploadFile(authToken, fileNameTest2, bytes, contentType, sizeFile);
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


    @SneakyThrows
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
        if (fileRepository.findFileByFileName(fileName1).isEmpty()) {
            fileService.uploadFile(authToken, fileName1, multipartFile1.getBytes(), multipartFile1.getContentType(), multipartFile1.getSize());
        }
        if (fileRepository.findFileByFileName(fileName2).isEmpty()) {
            fileService.uploadFile(authToken, fileName2, multipartFile2.getBytes(), multipartFile2.getContentType(), multipartFile2.getSize());
        }
        ResponseEntity<List<FileData>> actual = fileService.getAllFiles(authToken, limit);
        for (FileData file : list) {
            assertTrue(actual.getBody().stream().anyMatch(x -> Objects.equals(x.getFileName(), file.getFileName())));
        }
    }

    @Test
    public void checkUserTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        Long expected = 3L;
        Long actual = fileService.checkUser(authToken);
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    public void checkFileTest() {
        ResponseEntity<AuthentificationResponse> response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response.getBody()).getAuthToken();
        MultipartFile multipartFile = multipartFileGet(fileName);
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, multipartFile.getBytes(), multipartFile.getContentType(), multipartFile.getSize());
        }
        var actual = fileService.checkFile(userId, fileName).getFileName();
        assertEquals(fileName, actual);
    }
}