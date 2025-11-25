package com.pavlov.media.controller;

import com.pavlov.media.entity.Media;
import com.pavlov.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/media-contents")
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/{id}/read")
    public String readFileById(@PathVariable long id) {
        return mediaService.readObjectById(id);
    }

    @GetMapping("/{id}/download")
    public void downloadFileById(@PathVariable long id) {
        mediaService.downloadObjectById(id);
    }

    @GetMapping("/{id}")
    public Media getFileMetaData(@PathVariable long id) {
        return mediaService.getMetaDataById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Media uploadFileToMinIO(@RequestParam("file") MultipartFile file) throws IOException {
        long userId = 1;    //todo change after user-service integration
        return mediaService.saveFile(userId, file);
    }

}
