package com.dfiliopoulos.ragdocumentqa.controller;

import com.dfiliopoulos.ragdocumentqa.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
public class IngestionController {

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);
    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(value = "/api/v1/files/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> ingestFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("ingestType") String ingestType) {
        log.info("Received file: {}", file.getOriginalFilename());
        try {
            byte[] fileContent = file.getBytes();
            ingestionService.ingestFile(fileContent, file.getOriginalFilename(), ingestType);
            return ResponseEntity.ok("File uploaded and processed successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        }
    }

}
