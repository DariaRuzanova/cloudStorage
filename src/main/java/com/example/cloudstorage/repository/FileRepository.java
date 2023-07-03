package com.example.cloudstorage.repository;

import com.example.cloudstorage.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findFileByUserIdAndFileName(Long userId, String fileName);

}