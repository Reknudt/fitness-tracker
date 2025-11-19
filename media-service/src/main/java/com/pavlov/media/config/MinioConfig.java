package com.pavlov.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String url;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

//    @Value("${minio.secure}")
//    private Boolean minioSecure;

//    @Bean
//    public MinioClient minioClient() {
//        return MinioClient.builder()
//            .endpoint(url)
//            .credentials(accessKey, secretKey)
//            .build();
//    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().credentials(accessKey, secretKey)
                .endpoint(url, 9000, false).build();
    }
}
