package com.dfiliopoulos.ragdocumentqa.service;

import com.dfiliopoulos.ragdocumentqa.dtos.GroundingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroundingServiceTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private PgVectorStore vectorStore;
    private GroundingService groundingService;

    @BeforeEach
    void setUp() {
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        vectorStore = mock(PgVectorStore.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt(any(org.springframework.ai.chat.prompt.Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Grounded answer");

        groundingService = new GroundingService(chatClientBuilder, vectorStore);
        ReflectionTestUtils.setField(groundingService, "ragQAPrompt",
                new ClassPathResource("prompt-templates/RAG-QA-Prompt.st"));
    }

    @Test
    void returnsFallbackAndDoesNotCallModelWhenNoRelevantDocumentsExist() {
        when(vectorStore.doSimilaritySearch(any())).thenReturn(List.of(
                new Document("unrelated content").mutate().score(0.2).build()
        ));

        var response = groundingService.retrieveAnswer(new GroundingRequest("question"));

        assertEquals("No relevant information found.", response.response());
        verify(chatClient, never()).prompt(any(org.springframework.ai.chat.prompt.Prompt.class));
    }

    @Test
    void asksModelWhenRelevantContextExists() {
        when(vectorStore.doSimilaritySearch(any())).thenReturn(List.of(
                new Document("relevant content").mutate().score(0.9).build()
        ));

        var response = groundingService.retrieveAnswer(new GroundingRequest("question"));

        assertEquals("Grounded answer", response.response());
        verify(chatClient).prompt(any(org.springframework.ai.chat.prompt.Prompt.class));
        verify(requestSpec).call();
    }
}
