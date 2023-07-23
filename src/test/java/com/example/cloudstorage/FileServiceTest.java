package com.example.cloudstorage;


import com.example.cloudstorage.DTO.AuthentificationRequest;
import com.example.cloudstorage.DTO.AuthentificationResponse;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.exception.DuplicateFileNameException;
import com.example.cloudstorage.exception.FileNotFoundException;
import com.example.cloudstorage.exception.SessionException;
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

    private final String fileName = "text1.txt";
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
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        MultipartFile multipartFile = multipartFileGet(fileName);
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isPresent()) {
            fileService.deleteFile(authToken, fileName);
        }
        String contentType = multipartFile.getContentType();
        byte[] bytes = multipartFile.getBytes();
        long sizeFile = multipartFile.getSize();
        boolean result = fileService.uploadFile(authToken, fileName, bytes, contentType, sizeFile);
        assertTrue(result);
    }

    @SneakyThrows
    @Test
    public void uploadFileTestException() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        MultipartFile multipartFile1 = multipartFileGet(fileName);
        String contentType = multipartFile1.getContentType();
        byte[] bytes = multipartFile1.getBytes();
        long sizeFile = multipartFile1.getSize();
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, bytes, contentType, sizeFile);
        }
        boolean result = fileService.uploadFile(authToken, fileName, bytes, contentType, sizeFile);
//        assertThrows(ResponseStatusException.class, () -> {
//            fileService.uploadFile(authToken, fileName, bytes, contentType, sizeFile);
//        });
        assertFalse(result);
    }

    @Test
    public void renameFileTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileNameNew).isPresent()) {
            fileService.deleteFile(authToken, fileNameNew);
        }
        boolean result = fileService.renameFile(authToken, fileName, fileNameNew);
        assertTrue(result);
    }

    @SneakyThrows
    @Test
    public void renameFileTestException() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        MultipartFile multipartFile1 = multipartFileGet(fileName);
        MultipartFile multipartFile2 = multipartFileGet(fileNameNew);
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, multipartFile1.getBytes(), multipartFile1.getContentType(), multipartFile1.getSize());
        }
        if (fileRepository.findFileByUserIdAndFileName(userId, fileNameNew).isEmpty()) {
            fileService.uploadFile(authToken, fileNameNew, multipartFile2.getBytes(), multipartFile2.getContentType(), multipartFile2.getSize());
        }
        boolean result = fileService.renameFile(authToken, fileName, fileNameNew);
        assertFalse(result);
//        assertThrows(DuplicateFileNameException.class, () -> {
//            fileService.renameFile(authToken, fileName, fileNameNew);
//        });
    }

    @Test
    public void getFileTest() throws IOException {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        String fileNameTest = "test.txt";
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileNameTest).isPresent()) {
            fileService.deleteFile(authToken, fileNameTest);
        }
        MultipartFile multipartFile = multipartFileGet(fileNameTest);
        String contentType = multipartFile.getContentType();
        byte[] bytes = multipartFile.getBytes();
        long sizeFile = multipartFile.getSize();
        fileService.uploadFile(authToken, fileNameTest, bytes, contentType, sizeFile);
        File fileContent = fileService.getFile(authToken, fileNameTest);
        assertArrayEquals(bytes, fileContent.getFileContent());
    }

    @SneakyThrows
    @Test
    public void deleteFileTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        String fileNameTest2 = "testDeleteFile.txt";
        MultipartFile multipartFile = multipartFileGet(fileNameTest2);
        String contentType = multipartFile.getContentType();
        byte[] bytes = multipartFile.getBytes();
        long sizeFile = multipartFile.getSize();
        fileService.uploadFile(authToken, fileNameTest2, bytes, contentType, sizeFile);
        String actual = fileService.deleteFile(authToken, fileNameTest2);
        String expected = "Файл " + fileNameTest2 + " удален";
        assertEquals(expected, actual);
    }

    @Test
    public void deleteFileTestException() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        assertThrows(FileNotFoundException.class, () -> {
            fileService.deleteFile(authToken, "testDeleteFileException.txt");
        });
    }


    @SneakyThrows
    @Test
    public void getAllFilesTest() {
        int limit = 2;
        String fileName1 = "test.txt";
        String fileName2 = "text1.txt";
        MultipartFile multipartFile1 = multipartFileGet(fileName1);
        MultipartFile multipartFile2 = multipartFileGet(fileName2);
        List<FileData> list = List.of(
                FileData.builder().fileName(fileName1).size(multipartFile1.getSize()).build(),
                FileData.builder().fileName(fileName2).size(multipartFile2.getSize()).build()
        );
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        if (fileRepository.findFileByFileName(fileName1).isEmpty()) {
            fileService.uploadFile(authToken, fileName1, multipartFile1.getBytes(), multipartFile1.getContentType(), multipartFile1.getSize());
        }
        if (fileRepository.findFileByFileName(fileName2).isEmpty()) {
            fileService.uploadFile(authToken, fileName2, multipartFile2.getBytes(), multipartFile2.getContentType(), multipartFile2.getSize());
        }
        List<FileData> actual = fileService.getAllFiles(authToken, limit);
        for (FileData file : list) {
            assertTrue(actual.stream().anyMatch(x -> Objects.equals(x.getFileName(), file.getFileName())));
        }
    }

    @Test
    public void checkUserTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("sasha", "nmbqwe"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        Long expected = 3L;
        Long actual = fileService.checkUser(authToken);
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    public void checkFileTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("petr", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        MultipartFile multipartFile = multipartFileGet(fileName);
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, multipartFile.getBytes(), multipartFile.getContentType(), multipartFile.getSize());
        }
        var actual = fileService.checkFile(userId, fileName).getFileName();
        assertEquals(fileName, actual);
    }
}