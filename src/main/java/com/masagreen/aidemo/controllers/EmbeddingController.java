package com.masagreen.aidemo.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmbeddingController {

    private final EmbeddingClient embeddingClient;
    private final ChatClient chatClient;

    @Value("classpath:sheep.pdf")
    private Resource  resource;

    @Autowired
    public EmbeddingController(EmbeddingClient embeddingClient, ChatClient chatClient) {
        this.embeddingClient = embeddingClient;
        this.chatClient = chatClient;

    }

    @GetMapping("/ai/embedding")
    public Map<String, EmbeddingResponse> embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingClient.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
    @GetMapping("/ai/ask/{q}")
    public String simpleEmbed(@PathVariable("q") String q){
        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingClient);
        
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource);
        List<Document> documents = pagePdfDocumentReader.get();
        simpleVectorStore.accept(documents);
        String similarDocs = simpleVectorStore.similaritySearch(q).stream().map(Document::getContent).collect(Collectors.joining(System.lineSeparator()));
        
        var prompt= """
            You are a helpful assistant.
            
            Use the following information to answer the question:
            {similarDocs}
            """;
        var system = new SystemPromptTemplate(prompt).createMessage(Map.of("similarDocs", similarDocs));
        var user = new UserMessage(q);
        return chatClient.call(new Prompt(List.of(system, user))).getResult().getOutput().getContent();
    }
}