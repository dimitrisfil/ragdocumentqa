package com.dfiliopoulos.ragdocumentqa.controller;

import com.dfiliopoulos.ragdocumentqa.service.IngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IngestionControllerTest {

    private IngestionService ingestionService;
    private IngestionController controller;

    @BeforeEach
    void setUp() {
        ingestionService = mock(IngestionService.class);
        controller = new IngestionController(ingestionService);
    }

    @Test
    void returnsSuccessAfterIngestion() {
        var file = new MockMultipartFile("file", "notes.txt", "text/plain", "some text".getBytes());

        var response = controller.ingestFile(file, "page");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("File uploaded and processed successfully", response.getBody());
        verify(ingestionService).ingestFile("some text".getBytes(), "notes.txt", "page");
    }

    @Test
    void returnsServerErrorWhenFileCannotBeRead() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("notes.txt");
        when(file.getBytes()).thenThrow(new IOException("read failed"));

        var response = controller.ingestFile(file, "page");

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error processing file: read failed", response.getBody());
    }
}
