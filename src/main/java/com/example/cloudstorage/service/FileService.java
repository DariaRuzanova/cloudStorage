package com.example.cloudstorage.service;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.exception.DuplicateFileNameException;
import com.example.cloudstorage.exception.FileNotFoundException;
import com.example.cloudstorage.model.FileData;
import com.example.cloudstorage.model.NewFileName;
import com.example.cloudstorage.model.Session;
import com.example.cloudstorage.repository.FileRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        File deleteFile = checkFile(userId, fileName);
        fileRepository.deleteById(deleteFile.getId());
        log.info("Пользователь с id {} успешно удалил файл {}", userId, fileName);
        return ResponseEntity.ok().body("Файл " + fileName + " удален");
    }

    public ResponseEntity<byte[]> getFile(String authToken, String fileName) {
        Long userId = checkUser(authToken);
        File uploadFile = checkFile(userId, fileName);
        log.info("Пользователь с id {} успешно скачал файл {}", userId, fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + uploadFile.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(uploadFile.getFileContent());
    }

    public ResponseEntity<String> renameFile(String authToken, String fileName, NewFileName newFileName) {
        Long userId = checkUser(authToken);
        File fileToRename = checkFile(userId, fileName);
        fileRepository.findFileByFileName(newFileName.getFileName()).orElseThrow(() ->
                new DuplicateFileNameException("Файл с таким именем уже существует в базе данных"));
        fileToRename.setFileName(newFileName.getFileName());
        fileRepository.save(fileToRename);
        log.info("Пользователь с id {} успешно изменил имя файла {} на {}", userId, fileName, newFileName.getFileName());
        return ResponseEntity.ok().body("Имя файла "+fileName+" изменено на "+newFileName.getFileName());

    }
    public ResponseEntity<List<FileData>> getAllFiles(String authToken, Integer limit) {
        Long userId = checkUser(authToken);
        List<File> allFiles = fileRepository.findFilesByUserIdWithLimit(userId,limit);
        List<FileData> listFiles = allFiles.stream()
                .map(file -> FileData.builder()
                        .fileName(file.getFileName())
                        .size(file.getSize())
                        .build()).toList();
        log.info("Файлы пользователя с id {}", userId);
        return ResponseEntity.ok().body(listFiles);
    }

    public Long checkUser(String authToken) {
        Session sessionResult = authentificationService.getSession(authToken);
        if (sessionResult == null) {
            log.info("Пользователь не найден");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return sessionResult.getUserID();
    }

    public File checkFile(Long userId, String fileName) {
        return fileRepository.findFileByUserIdAndFileName(userId, fileName).orElseThrow(() ->
                new FileNotFoundException("Файл с именем " + fileName + " не найден!"));
    }



}

