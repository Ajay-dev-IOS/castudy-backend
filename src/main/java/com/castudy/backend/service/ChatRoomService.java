package com.castudy.backend.service;

import com.castudy.backend.dto.ChatMessageRequest;
import com.castudy.backend.dto.ChatMessageResponse;
import com.castudy.backend.entity.ChatRoom;
import com.castudy.backend.entity.Message;
import com.castudy.backend.entity.Student;
import com.castudy.backend.entity.Subject;
import com.castudy.backend.repository.ChatRoomRepository;
import com.castudy.backend.repository.MessageRepository;
import com.castudy.backend.repository.StudentRepository;
import com.castudy.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public ChatRoomService(
            ChatRoomRepository chatRoomRepository,
            MessageRepository messageRepository,
            SubjectRepository subjectRepository,
            StudentRepository studentRepository,
            NotificationService notificationService
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    public ChatRoom getOrCreateChatRoom(Long subjectId) {

        return chatRoomRepository
                .findBySubjectId(subjectId)
                .orElseGet(() -> {

                    Subject subject = subjectRepository
                            .findById(subjectId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Subject not found"
                                    )
                            );

                    ChatRoom chatRoom =
                            new ChatRoom(subject);

                    return chatRoomRepository.save(chatRoom);
                });
    }

    public List<ChatMessageResponse> getMessages(Long chatRoomId) {

        List<Message> messages =
                messageRepository
                        .findByChatRoomIdOrderByCreatedAtAsc(
                                chatRoomId
                        );

        return messages.stream()
                .map(message ->
                        new ChatMessageResponse(
                                message.getId(),
                                message.getStudent().getId(),
                                message.getStudent().getName(),
                                message.getMessage(),
                                message.getCreatedAt()
                        )
                )
                .toList();
    }

    public ChatMessageResponse sendMessage(
            Long chatRoomId,
            Student student,
            ChatMessageRequest request
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        Message message = new Message(
                chatRoom,
                student,
                request.getMessage()
        );

        Message savedMessage = messageRepository.save(message);

        // Get all messages in this chat room
        List<Message> existingMessages =
                messageRepository.findByChatRoomId(chatRoomId);

        // Get unique students who participated in this chat
        List<Student> participants = existingMessages.stream()
                .map(Message::getStudent)
                .distinct()
                .toList();
        List<Student> recipients = participants.stream()
                .filter(participant -> !participant.getId().equals(student.getId()))
                .toList();
        for (Student recipient : recipients) {
            notificationService.createNotification(
                    recipient.getId(),
                    "New Chat Message",
                    student.getName() + " sent a new message in the chat",
                    "CHAT_MESSAGE"
            );
        }

        return new ChatMessageResponse(
                savedMessage.getId(),
                student.getId(),
                student.getName(),
                savedMessage.getMessage(),
                savedMessage.getCreatedAt()
        );
    }
}