package com.pavlov.media.service;

import com.pavlov.media.entity.Media;
import com.pavlov.media.exception.MediaNotFoundException;
import com.pavlov.media.exception.MediaStorageException;
import com.pavlov.media.repository.MediaRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public List<Media> findAll() {
        return mediaRepository.findAll();
    }

    public String readObjectById(long id) {
        try (InputStream stream = minioClient
                .getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(String.valueOf(id))
                        .build())
        ) {
            return new String(stream.readAllBytes());
        } catch (Exception e) {
            throw new MediaNotFoundException(id);
//            throw new MediaStorageException("Failed to read object from MinIO. id=" + id, e);
        }
    }

    public InputStream downloadObjectById(Long id) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(id.toString())
                            .build()
            );
        } catch (Exception e) {
            throw new MediaNotFoundException(id);
//            throw new MediaStorageException("Failed to download file. id=" + id, e);
        }
    }

    public Media getMetaDataById(long id) {
        return mediaRepository.findById(id).orElseThrow(() -> new MediaNotFoundException(id));
    }

//    @Transactional
    public Media saveFile(long userId, MultipartFile file) throws IOException {
        InputStream in = new ByteArrayInputStream(file.getBytes());
        String fileName = file.getOriginalFilename();
        Media saved = mediaRepository.save(new Media(userId, fileName, file.getContentType(), file.getSize(), bucketName));
        putObject(saved.getId().toString(), file.getSize(), in);
        return saved;
    }

    public void removeFile(Long id) {
        mediaRepository.deleteById(id);
        removeObject(id.toString());
    }

    private void putObject(String fileName, long size, InputStream inputStream) {
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fileName)
                    .stream(inputStream, size, -1).build());
        } catch (Exception e) {
            throw new MediaStorageException("Failed to upload file to MinIO", e);
        }
    }

    private void removeObject(String filename) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filename).build());
        } catch (Exception e) {
            throw new MediaStorageException("Failed to remove file to MinIO", e);
        }
    }

}
