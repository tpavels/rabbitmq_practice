package com.tpavels.rabbit.controller;

import com.tpavels.rabbit.event.Message;
import com.tpavels.rabbit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PutMapping("/publish")
    public ResponseEntity<Void> publish() {
        messageService.publishMessage();
        return ResponseEntity.ok(null);
    }

    @GetMapping("/receive")
    public ResponseEntity<Message> receive() {
        Message msg = messageService.receiveMessage();
        return ResponseEntity.ok(msg);
    }
}
