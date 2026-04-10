package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * MutedTopic 实体 - 静音话题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "muted_topics", uniqueConstraints = {
    @UniqueConstraint(name = "uniq_user_stream_topic", 
            columnNames = {"user_profile_id", "stream_id", "topic_name"})
})
public class MutedTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile user;

    @Column(name = "stream_id", nullable = false)
    private Long streamId;

    @Column(name = "topic_name", nullable = false, length = 60)
    private String topicName;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;
}