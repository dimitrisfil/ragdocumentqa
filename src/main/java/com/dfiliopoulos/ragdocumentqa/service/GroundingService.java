package com.dfiliopoulos.ragdocumentqa.service;


import com.dfiliopoulos.ragdocumentqa.dtos.GroundingRequest;
import com.dfiliopoulos.ragdocumentqa.dtos.GroundingResponse;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroundingService {

    private static final Logger log = LoggerFactory.getLogger(GroundingService.class);

    private final ChatClient chatClient;

    @Value("classpath:/prompt-templates/RAG-QA-Prompt.st")
    private Resource ragQAPrompt;

    private final PgVectorStore pgVectorStore;

    public GroundingService(ChatClient.Builder chatClientBuilder, @Qualifier("qaVectorStore") PgVectorStore pgVectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.pgVectorStore = pgVectorStore;
    }

    public GroundingResponse retrieveAnswer(GroundingRequest groundingRequest) {
        var results = pgVectorStore.doSimilaritySearch(
                SearchRequest.builder()
                        .query(groundingRequest.prompt())
                        .build()
        );
        log.info("Retrieved results: {} \n results: {}", results.size(), results);

        var context = results.stream()
                .filter(Objects::nonNull)
                .filter(result -> result.getScore() > 0.2)
                .limit(2)
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        if (StringUtils.isNotEmpty(context)) {
            log.info("Matched context: {}", context);
            PromptTemplate promptTemplate = new PromptTemplate(ragQAPrompt);
            var message = promptTemplate.createMessage(Map.of("input", groundingRequest.prompt(),
                    "context", context));
            var responseSpec = chatClient.prompt(new Prompt(List.of(message)))
                    .call()
                    .content();
            log.info("responseSpec : {} ", responseSpec);
            return new GroundingResponse(responseSpec);
        } else {
            log.info("No relevant information found for the query. So sending default response");
            return new GroundingResponse("No relevant information found.");
        }
    }
}
