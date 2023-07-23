package com.example.cloudstorage.service;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.exception.DuplicateFileNameException;
import com.example.cloudstorage.exception.FileNotFoundException;
import com.example.cloudstorage.exception.InputDataException;
import com.example.cloudstorage.exception.SessionException;
import com.example.cloudstorage.model.FileData;
import com.example.cloudstorage.model.Session;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service

@AllArgsConstructor
@Slf4j
public class FileService {

    private FileRepository fileRepository;
    private UserRepository userRepository;
    private AuthentificationService authentificationService;


    public boolean uploadFile(String authToken, String fileName, byte[] bytes, String contentType, long sizeFile) {
        Long userId = checkUser(authToken);
        File uploadFile;
        boolean flag = true;
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isPresent()) {
            flag = false;
            log.error("Файл с именем {} уже существует", fileName);
        }
        User user = userRepository.getReferenceById(userId);
        uploadFile = File.builder()
                .fileName(fileName)
                .type(contentType)
                .fileContent(bytes)
                .size(sizeFile)
                .user(user)
                .build();
        fileRepository.save(uploadFile);
        log.info("Пользователь с id {} успешно загрузил файл {}", userId, fileName);
        return flag;

    }

    public String deleteFile(String authToken, String fileName) {
        Long userId = checkUser(authToken);
        File deleteFile = checkFile(userId, fileName);
        fileRepository.deleteById(deleteFile.getId());
        log.info("Пользователь с id {} успешно удалил файл {}", userId, fileName);
        return "Файл " + fileName + " удален";
    }

    public File getFile(String authToken, String fileName) {
        Long userId = checkUser(authToken);
        File uploadFile = checkFile(userId, fileName);
        log.info("Пользователь с id {} успешно скачал файл {}", userId, fileName);
        return uploadFile;
    }

    public boolean renameFile(String authToken, String fileName, String newFileName) {
        Long userId = checkUser(authToken);
        File fileToRename = checkFile(userId, fileName);
        boolean flag = true;
        if (fileRepository.findFileByUserIdAndFileName(userId, newFileName).isPresent()) {
            flag = false;
            log.warn("Файл с таким именем уже существует в базе данных");

        }
        fileToRename.setFileName(newFileName);
        fileRepository.save(fileToRename);
        log.info("Пользователь с id {} успешно изменил имя файла {} на {}", userId, fileName, newFileName);
        return flag;

    }

    public List<FileData> getAllFiles(String authToken, int limit) {
        Long userId = checkUser(authToken);
        if (limit < 0) {
            log.warn("Значение лимита ошибочно");
            throw new InputDataException("Значение лимита ошибочно");
        }
        List<File> allFiles = fileRepository.findFilesByUserId(userId);
        List<FileData> listFiles = allFiles.stream()
                .map(file -> FileData.builder()
                        .fileName(file.getFileName())
                        .size(file.getSize())
                        .build()).collect(Collectors.toList());
        log.info("Был получен список файлов пользователя с id {}", userId);
        return listFiles;
    }

    public Long checkUser(String authToken) {
        Session sessionResult = authentificationService.getSession(authToken);
        if (sessionResult == null) {
            log.error("Пользователь не найден");
            throw new SessionException("Пользователь с таким логином не найден");
        }
        return Objects.requireNonNull(sessionResult).getUserID();
    }

    public File checkFile(Long userId, String fileName) {
        var checkFile = fileRepository.findFileByUserIdAndFileName(userId, fileName);
        if (checkFile.isEmpty()) {
            log.error("Файл с именем " + fileName + " не найден!");
            throw new FileNotFoundException("Файл с именем " + fileName + " не найден!");
        }
        return checkFile.get();

    }


}

