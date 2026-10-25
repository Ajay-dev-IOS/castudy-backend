package com.castudy.backend.repository;

import com.castudy.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository
        extends JpaRepository<Message, Long> {

    List<Message> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
    List<Message> findByChatRoomId(Long chatRoomId);
}