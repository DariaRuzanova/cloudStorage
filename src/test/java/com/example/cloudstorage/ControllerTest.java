package com.example.cloudstorage;


import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.model.AuthentificationRequest;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.model.FileData;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.service.AuthentificationService;
import com.example.cloudstorage.service.FileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CloudStorageApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class ControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;
    @Autowired
    private AuthentificationService authentificationService;

    private static final String header = "auth-token";
    private static final String query = "filename";
    private static final String queryNew = "newFileName";

    @SneakyThrows
    public String getAuthToken() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new AuthentificationRequest("petr", "qwerty")));

        MvcResult result = mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String actualJson = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        AuthentificationResponse response = mapper.readValue(actualJson, AuthentificationResponse.class);
        return response.getAuthToken();
    }

    @SneakyThrows
    @Test
    public void loginAndLogoutControllerTest() {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new AuthentificationRequest("petr", "qwerty")));

        MvcResult result = mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String actualJson = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        AuthentificationResponse response = mapper.readValue(actualJson, AuthentificationResponse.class);
        String authToken = response.getAuthToken();
        var request2 = MockMvcRequestBuilders.post("/logout")
                .header(header, authToken);
        mvc.perform(request2)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @SneakyThrows
    @Test
    public void uploadFileTest() {
        String fileName = "hello.txt";
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        String authToken = getAuthToken();

        if (fileRepository.findFileByFileName(fileName).isPresent()) {
            fileService.deleteFile(authToken, fileName);
        }
        var request = MockMvcRequestBuilders.multipart("/file")
                .file(file)
                .header(header, authToken)
                .queryParam(query, file.getOriginalFilename());
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @SneakyThrows
    @Test
    public void getFileTest() {
        String fileName = "hello2.txt";
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World and sun!".getBytes()
        );
        String authToken = getAuthToken();
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, file);
        }
        var request = MockMvcRequestBuilders.get("/file")
                .header(header, authToken)
                .queryParam(query, file.getOriginalFilename());
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(file.getBytes()));


    }

    @SneakyThrows
    @Test
    public void renameFileTest() {
        String fileName = "hello2.txt";
        String newFileName = "helloRename.txt";
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World and sun!".getBytes()
        );

        String authToken = getAuthToken();
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, newFileName).isPresent()) {
            fileService.deleteFile(authToken, newFileName);
        }
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, file);
        }
        var request = MockMvcRequestBuilders.put("/file")
                .header(header, authToken)
                .queryParam(query, fileName)
                .queryParam(queryNew, newFileName);
        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(fileRepository.findFileByFileName("helloRename.txt").isPresent());
        assertFalse(fileRepository.findFileByFileName(fileName).isPresent());

    }

    @SneakyThrows
    @Test
    public void deleteFileTest() {
        String fileName = "hello.txt";
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        String authToken = getAuthToken();
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, file);
        }
        var request = MockMvcRequestBuilders.delete("/file")
                .header(header, authToken)
                .queryParam(query, fileName);
        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        assertTrue(fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty());
    }

    @SneakyThrows
    @Test
    public void getAllFilesTest() {
        String fileName1 = "hello.txt";
        String fileName2 = "hello2.txt";
        String fileName3 = "test.txt";
        String authToken = getAuthToken();
        Long userId = authentificationService.getSession(authToken).getUserID();
        MockMultipartFile file1 = new MockMultipartFile("file", fileName1, MediaType.TEXT_PLAIN_VALUE, "Создаем файл hello.txt".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", fileName1, MediaType.TEXT_PLAIN_VALUE, "Заполняем файл hello2.txt".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("file", fileName1, MediaType.TEXT_PLAIN_VALUE, "Заполнили файл test.txt".getBytes());
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName1).isEmpty()) {
            fileService.uploadFile(authToken, fileName1, file1);
        }
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName2).isEmpty()) {
            fileService.uploadFile(authToken, fileName2, file2);
        }
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName3).isEmpty()) {
            fileService.uploadFile(authToken, fileName3, file3);
        }
        var request = MockMvcRequestBuilders.get("/list")
                .header(header, authToken)
                .queryParam("limit", "3");
        MvcResult result = mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String response = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();

        List<FileData> listFiles = mapper.readValue(response, new TypeReference<>() {
        });
        assertFalse(listFiles.isEmpty());



    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
