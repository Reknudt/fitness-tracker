package com.pavlov.media.controller;

import com.pavlov.media.entity.Media;
import com.pavlov.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/media-contents")
public class MediaController {

    private final MediaService mediaService;

    @GetMapping
    public List<Media> getAll() {
        return mediaService.findAll();
    }

    @GetMapping("/{id}")
    public String readFileById(@PathVariable long id) {
        return mediaService.readObjectById(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        InputStream stream = mediaService.downloadObjectById(id);
        Media media = mediaService.getMetaDataById(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + media.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(media.getContentType()))
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/{id}/info")
    public Media getFileMetaData(@PathVariable long id) {
        return mediaService.getMetaDataById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Media uploadToMinIO(@RequestParam("file") MultipartFile file) throws IOException {
        long userId = 1;    //todo change after user-service integration
        return mediaService.saveFile(userId, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mediaService.removeFile(id);
    }

}
