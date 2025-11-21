package com.pavlov.media.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    private long userId;

    @NotBlank
    @Size(max = 255)
    private String fileName;

    @NotBlank
    @Size(max = 255)
    private String contentType;

    @NotNull
    private Long size;

    @NotNull
    @Size(max = 255)
    private String storageKey;

    private LocalDateTime uploadedAt;

    public Media(long userId, String fileName, String contentType, Long size, String storageKey) {
        this.userId = userId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.storageKey = storageKey;
    }
}
