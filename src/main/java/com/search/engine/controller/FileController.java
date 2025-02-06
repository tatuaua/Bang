package com.search.engine.controller;

import java.io.File;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.search.engine.service.IndexService;

@RestController
@RequestMapping("/files")
public class FileController {

    private final IndexService indexService;

    public FileController(IndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping("/upload")
    public void uploadFile(@RequestParam MultipartFile file) {
        //indexService.updateIndex(file);
    }
}