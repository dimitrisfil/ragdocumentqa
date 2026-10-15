package com.dfiliopoulos.ragdocumentqa.service;

import com.dfiliopoulos.ragdocumentqa.utils.RagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;

    public IngestionService(@Qualifier("qaVectorStore") PgVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    private void ingestPDFDocs(String ingestType, Resource pdfResource) {
        log.info("Ingesting PDF documents");
        var docs = getPDFDocuments(ingestType, pdfResource);
        vectorStore.add(docs);
        log.info("Ingested {} documents into the vector store.", docs.size());
    }

    private static List<Document> getPDFDocuments(String ingestType, Resource pdfResource) {
        try {
            return switch (ingestType) {
                case "page" -> new PagePdfDocumentReader(pdfResource).get();
                case "paragraph" -> new ParagraphPdfDocumentReader(pdfResource).get();
                default -> throw new IllegalArgumentException("Invalid ingestType: " + ingestType);
            };
        }
        catch (Exception e) {
            log.error("Error occurred while reading PDF documents", e);
            throw new RuntimeException("Error occurred while reading PDF documents", e);
        }
    }

    public void ingestFile(byte[] fileContent, String originalFilename, String ingestType) {
        log.info("Ingestion Service invoked - Ingesting file: {} with type: {}", originalFilename, ingestType);
        Resource docResource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return originalFilename;
            }
        };

        var fileExtension = RagUtils.getFileExtension(originalFilename);
        switch (fileExtension){
            case "pdf" -> {
                log.info("Processing PDF file: {}", originalFilename);
                ingestPDFDocs(ingestType, docResource);
            }
            case "docx" -> {
                log.info("Processing DOCX file: {}", originalFilename);
                ingestWordDocs(ingestType, docResource);
            }
            case "txt" -> {
                log.info("Processing TXT file: {}", originalFilename);
                ingestTextDocuments(docResource);
            }
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
        }
        log.info("Ingestion completed successfully");
    }

    private void ingestTextDocuments(Resource docResource) {
        log.info("Ingesting text documents");
        var textReader = new TextReader(docResource);
        textReader.getCustomMetadata().put("filename", docResource.getFilename());
        var docs = textReader.read();
        vectorStore.add(docs);
        log.info("Ingested {} documents from text files into the vector store.", docs.size());
    }

    private void ingestWordDocs(String ingestType, Resource docResource) {
        log.info("Ingesting Word documents");
        var docs = getWordDocuments(ingestType, docResource);
        vectorStore.add(docs);
        log.info("Ingested {} documents from word files into the vector store.", docs.size());
    }

    private static List<Document> getWordDocuments(String ingestType, Resource docResource) {
        var docs = new TikaDocumentReader(docResource).get();
        return "token".equals(ingestType) ? applyTokenTextSplitter(docs) : docs;
    }

    private static List<Document> applyTokenTextSplitter(List<Document> docs) {
        TokenTextSplitter splitter = new TokenTextSplitter(250, 150, 10, 5000, true);
        return splitter.apply(docs);
    }
}
