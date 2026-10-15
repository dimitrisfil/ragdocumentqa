package com.dfiliopoulos.ragdocumentqa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class IngestionServiceTest {

    private PgVectorStore vectorStore;
    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        vectorStore = mock(PgVectorStore.class);
        ingestionService = new IngestionService(vectorStore);
    }

    @Test
    void ingestsTextFileAndAddsFilenameMetadata() {
        byte[] content = "hello from the document".getBytes();

        var documents = new AtomicReference<List<Document>>();
        doAnswer(invocation -> {
            documents.set(invocation.getArgument(0));
            return null;
        }).when(vectorStore).add(anyList());
        ingestionService.ingestFile(content, "notes.txt", "ignored");

        List<Document> ingested = documents.get();
        assertEquals(1, ingested.size());
        assertEquals("hello from the document", ingested.getFirst().getText());
        assertEquals("notes.txt", ingested.getFirst().getMetadata().get("filename"));
    }

    @Test
    void rejectsUnsupportedFileExtensions() {
        assertThrows(IllegalArgumentException.class,
                () -> ingestionService.ingestFile(new byte[0], "notes.md", "page"));

        verify(vectorStore, never()).add(anyList());
    }

    @Test
    void rejectsUnknownPdfIngestionType() {
        var exception = assertThrows(RuntimeException.class,
                () -> ingestionService.ingestFile(new byte[0], "notes.pdf", "token"));

        assertEquals("Error occurred while reading PDF documents", exception.getMessage());
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        verify(vectorStore, never()).add(anyList());
    }
}
