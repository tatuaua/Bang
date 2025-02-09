package com.search.bang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.search.bang.service.IndexService;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    private final IndexService indexService;

    public FileController(IndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file) {

        try {
            indexService.updateIndex(file);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("File processing error encountered while updating index: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            log.error("Error encountered while updating index: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}