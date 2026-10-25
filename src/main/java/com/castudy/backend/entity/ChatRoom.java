package com.castudy.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false, unique = true)
    private Subject subject;

    @Column(nullable = false)
    private Instant createdAt;

    public ChatRoom() {
    }

    public ChatRoom(Subject subject) {
        this.subject = subject;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}