package com.pavlov.media.controller;

import com.pavlov.media.entity.Media;
import com.pavlov.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/media-contents")
public class MediaController {

    private final MediaService mediaService;

//    @Autowired
//    private MediaService minioComponent;

    @GetMapping()
    public String downloadFile(@RequestParam("fileName") String fileName) {
        return mediaService.getObject(fileName);
    }

    @PostMapping("/upload")
    public Media uploadFileToMinIO(@RequestParam("file") MultipartFile file) throws IOException {
        long userId = 1;    //todo change after user-service integration
        return mediaService.saveFile(userId, file);
    }

}
