package com.castudy.backend.controller;

import com.castudy.backend.dto.ChatMessageRequest;
import com.castudy.backend.dto.ChatMessageResponse;
import com.castudy.backend.entity.ChatRoom;
import com.castudy.backend.entity.Student;
import com.castudy.backend.service.ChatRoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @GetMapping("/subjects/{subjectId}/chat")
    public ResponseEntity<ChatRoom> getChatRoom(
            @PathVariable Long subjectId
    ) {

        ChatRoom chatRoom =
                chatRoomService.getOrCreateChatRoom(subjectId);

        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/chat/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long chatRoomId
    ) {

        return ResponseEntity.ok(
                chatRoomService.getMessages(chatRoomId)
        );
    }

    @PostMapping("/chat/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ChatMessageRequest request,
            Authentication authentication
    ) {
        System.out.println("===== SEND MESSAGE =====");
        System.out.println("Authentication: " + authentication);
        System.out.println("Principal: " + authentication.getPrincipal());
        System.out.println("Name: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());

        Student student = (Student) authentication.getPrincipal();

        ChatMessageResponse response =
                chatRoomService.sendMessage(chatRoomId, student, request);

        return ResponseEntity.ok(response);
    }
}