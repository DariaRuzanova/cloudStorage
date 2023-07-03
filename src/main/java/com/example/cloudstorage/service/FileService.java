package com.example.cloudstorage.service;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.exception.DuplicateFileNameException;
import com.example.cloudstorage.exception.FileNotFoundException;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.model.Session;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.*;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service

@AllArgsConstructor
@Slf4j
public class FileService {

    private FileRepository fileRepository;
    private AuthentificationService authentificationService;


    public ResponseEntity<String> uploadFile(String authToken, String fileName, MultipartFile multipartFile) {
        Long userId = checkUser(authToken);
        File uploadFile;
        fileRepository.findFileByUserIdAndFileName(userId, fileName).ifPresent(x -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        });
        log.info("Ошибка передачи файла. Файл существует");
        try {
            uploadFile = File.builder()
                    .fileName(fileName)
                    .type(multipartFile.getContentType())
                    .fileContent(multipartFile.getBytes())
                    .size(multipartFile.getSize())
                    .id(User.builder().id(userId).build().getId())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileRepository.save(uploadFile);
        log.info("Пользователь с id {} успешно загрузил файл {}", userId, fileName);
        return ResponseEntity.ok().body("Файл " + fileName + " сохранен");

    }

    public ResponseEntity<String> deleteFile(String authToken, String fileName) {
        Long userId = checkUser(authToken);
        File deleteFile = fileRepository.findFileByUserIdAndFileName(userId, fileName).orElseThrow(() ->
                new FileNotFoundException("Файл с именем " + fileName + " не найден!"));
        fileRepository.deleteById(deleteFile.getId());
        log.info("Пользователь с id {} успешно удалил файл {}", userId, fileName);
        return ResponseEntity.ok().body("Файл " + fileName + " удален");
    }

    public ResponseEntity<byte[]> getFile(String authToken, String fileName) {
        Long userId = checkUser(authToken);
        File uploadFile = fileRepository.findFileByUserIdAndFileName(userId, fileName).orElseThrow(() ->
                new FileNotFoundException("Файл с именем " + fileName + " не найден!"));
        log.info("Пользователь с id {} успешно скачал файл {}", userId, fileName);
        return ResponseEntity.ok()
                .headers(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + uploadFile.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(uploadFile.getFileContent());
    }

    public Long checkUser(String authToken) {
        Session sessionResult = authentificationService.getSession(authToken);
        if (sessionResult == null) {
            log.info("Пользователь не найден");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return sessionResult.getUserID();
    }
}

