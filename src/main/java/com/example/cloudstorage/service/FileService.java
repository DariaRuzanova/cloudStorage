package com.example.cloudstorage.service;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.exception.DuplicateFileNameException;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.model.Session;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        Session sessionResult = authentificationService.getSession(authToken);
        if (sessionResult == null) {
            log.info("Пользователь не найден");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        }
        Long userId = sessionResult.getUserID();
        File uploadFile;
        fileRepository.findFileByUserIdAndFileName(userId, fileName).orElseThrow(() ->
                new DuplicateFileNameException("Файл " + fileName + " уже существует"));
        log.info("Ошибка передачи файла. Найден дубликат");
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

}

