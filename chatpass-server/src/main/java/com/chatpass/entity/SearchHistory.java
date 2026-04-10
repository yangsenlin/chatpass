package com.chatpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SearchHistory 实体
 * 用户搜索历史记录
 */
@Entity
@Table(name = "search_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "realm_id", nullable = false)
    private Long realmId;

    @Column(name = "query", nullable = false, length = 200)
    private String query;

    @Column(name = "search_type", length = 20)
    private String searchType;

    @Column(name = "results_count")
    private Integer resultsCount;

    @Column(name = "date_searched", nullable = false)
    private LocalDateTime dateSearched;

    // Search types
    public static final String TYPE_MESSAGES = "MESSAGES";
    public static final String TYPE_STREAMS = "STREAMS";
    public static final String TYPE_USERS = "USERS";
    public static final String TYPE_GLOBAL = "GLOBAL";
}