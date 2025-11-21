package com.pavlov.media.service;

import com.pavlov.media.entity.Media;
import com.pavlov.media.repository.MediaRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public Media saveFile(long userId, MultipartFile file) throws IOException {
        InputStream in = new ByteArrayInputStream(file.getBytes());
        String fileName = file.getOriginalFilename();
        putObject(fileName, in);

        return mediaRepository.save(new Media(userId, fileName, file.getContentType(), file.getSize(), ""));    // todo check chat gpt for more detailed solution
    }

    public String getObject(String fileName) {
        try (InputStream stream = minioClient
                .getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build());) {
            return new String(stream.readAllBytes());
        } catch (ErrorResponseException | InsufficientDataException |
                 InternalException | InvalidKeyException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return "You haven't uploaded anything yet.";
    }

    private void putObject(String fileName, InputStream inputStream) {
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fileName)
                    .stream(inputStream, -1, 10485760).build());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
