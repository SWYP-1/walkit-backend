package com.walkit.walkit.common.image.controller;

import com.walkit.walkit.common.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/download/{imageName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String imageName) {
        byte[] data = imageService.downloadFile(imageName);
        ByteArrayResource resource = new ByteArrayResource(data);

        String encodedFileName = UriUtils.encode(imageName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"; " +
                                   "filename*=UTF-8''" + encodedFileName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
    }

    @DeleteMapping("/delete/{imageName}")
    public ResponseEntity<String> deleteFile(@PathVariable String imageName) {
        imageService.deleteFile(imageName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}