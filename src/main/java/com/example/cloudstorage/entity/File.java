package com.example.cloudstorage.entity;

import com.example.cloudstorage.model.AuthentificationResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "file_name",nullable = false,unique = true)
    private String fileName;

    @Column(name = "type",nullable = false)
    private String type;
    @Column(name = "content",nullable = false)
    private byte[] fileContent;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private Date createData;

    @Column(name = "size",nullable = false)
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",  nullable = false)
    private User user;


}
