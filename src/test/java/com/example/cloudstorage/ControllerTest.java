package com.example.cloudstorage;

import com.example.cloudstorage.model.AuthentificationRequest;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.service.FileService;
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
    private FileService fileService;

    private static final String header = "auth-token";
    private static final String query = "filename";

    @SneakyThrows
    public String getAuthToken(){
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
    public void loginAndLogoutControllerTest()  {

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

        fileService.deleteFile(authToken, fileName);

//        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//        mockMvc.perform(multipart("/upload").file(file))
//                .andExpect(status().isOk());
        var request = MockMvcRequestBuilders.multipart("/file")
                .file(file)
                .header(header,authToken)
                .queryParam(query, file.getOriginalFilename());
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());
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
