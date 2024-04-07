package com.masagreen.aidemo.controllers;

import org.springframework.ai.chat.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatCotroller {
    private final ChatClient chatClient;
    public ChatCotroller(ChatClient client){
        this.chatClient = client;
    }
    @GetMapping
    public String generateStr(){
        return chatClient.call("Who won the Swiftie Bowl? If you don't know, just say I don't know");
    }
}
