package com.pavlov.media.service;

import com.pavlov.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MediaService {

    private final MediaRepository mediaRepository;



}
