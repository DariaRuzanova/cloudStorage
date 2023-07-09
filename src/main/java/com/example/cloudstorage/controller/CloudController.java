package com.example.cloudstorage.controller;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.model.AuthentificationRequest;
import com.example.cloudstorage.model.AuthentificationResponse;
import com.example.cloudstorage.model.FileData;
import com.example.cloudstorage.model.NewFileName;
import com.example.cloudstorage.service.AuthentificationService;
import com.example.cloudstorage.service.FileService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j

public class CloudController {
    private final AuthentificationService authentificationService;
    private final FileService fileService;

    @PostMapping("/login")
    public ResponseEntity<AuthentificationResponse> login(@RequestBody AuthentificationRequest authentificationRequest) {
        return authentificationService.authentificationLogin(authentificationRequest);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("auth-token") String authToken) {
        authentificationService.logout(authToken);
    }


    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestBody @NotNull MultipartFile file) {
        return fileService.uploadFile(authToken, fileName, file);
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName) {
        return fileService.deleteFile(authToken, fileName);
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> getFile(@RequestHeader("auth-token") @NotNull String authToken,
                                          @RequestParam("filename") @NotNull String fileName) {
        return fileService.getFile(authToken, fileName);
    }

    @PutMapping("/file")
    public ResponseEntity<String> renameFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestBody @NotNull NewFileName newFileName) {
        return fileService.renameFile(authToken, fileName, newFileName);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileData>> getAllFiles(@RequestParam("auth-token") @NotNull String authToken,
                                                      @RequestParam("limit") @NotNull Integer limit) {
        return fileService.getAllFiles(authToken, limit);
    }

}
