package com.example.cloudstorage.controller;

import com.example.cloudstorage.DTO.AuthentificationRequest;
import com.example.cloudstorage.DTO.AuthentificationResponse;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.exception.DuplicateFileNameException;
import com.example.cloudstorage.exception.SessionException;
import com.example.cloudstorage.model.FileData;
import com.example.cloudstorage.service.AuthentificationService;
import com.example.cloudstorage.service.FileService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j

public class CloudController {
    private final AuthentificationService authentificationService;
    private final FileService fileService;

    @PostMapping("/login")
    public ResponseEntity<AuthentificationResponse> login(@RequestBody AuthentificationRequest authentificationRequest) {
        AuthentificationResponse response = authentificationService.authentificationLogin(authentificationRequest);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader("auth-token") String authToken) {
        boolean flag = authentificationService.logout(authToken);
        if (!flag) {
            throw new SessionException("Пользователь с таким логином не найден");
        }
        return ResponseEntity.ok().body(null);

    }


    @SneakyThrows
    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestBody @NotNull MultipartFile file) {
        boolean flag = fileService.uploadFile(authToken, fileName, file.getBytes(), file.getContentType(), file.getSize());
        if (!flag) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().body("Файл " + fileName + " сохранен");
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName) {
        String response = fileService.deleteFile(authToken, fileName);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> getFile(@RequestHeader("auth-token") @NotNull String authToken,
                                          @RequestParam("filename") @NotNull String fileName) {
        File uploadFile = fileService.getFile(authToken, fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + uploadFile.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(uploadFile.getFileContent());
    }

    @PutMapping("/file")
    public ResponseEntity<String> renameFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestParam("newFileName") @NotNull String newFileName) {
        boolean flag = fileService.renameFile(authToken, fileName, newFileName);
        if (!flag) {
            throw new DuplicateFileNameException("Файл с таким именем уже существует в базе данных");
        }
        return ResponseEntity.ok().body("Имя файла " + fileName + " изменено на " + newFileName);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileData>> getAllFiles(@RequestHeader("auth-token") @NotNull String authToken,
                                                      @RequestParam("limit") @NotNull int limit) {
        List<FileData> listFiles = fileService.getAllFiles(authToken, limit);
        return ResponseEntity.ok().body(listFiles);
    }

}
