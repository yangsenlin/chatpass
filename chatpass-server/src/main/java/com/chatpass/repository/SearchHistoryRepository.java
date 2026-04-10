package com.chatpass.repository;

import com.chatpass.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    List<SearchHistory> findByUserIdOrderByDateSearchedDesc(Long userId);
    
    List<SearchHistory> findByUserIdAndRealmIdOrderByDateSearchedDesc(Long userId, Long realmId);
    
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId ORDER BY sh.dateSearched DESC LIMIT :limit")
    List<SearchHistory> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Query("SELECT DISTINCT sh.query FROM SearchHistory sh WHERE sh.userId = :userId ORDER BY sh.dateSearched DESC")
    List<String> findDistinctQueriesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT sh.query, COUNT(sh) as count FROM SearchHistory sh WHERE sh.userId = :userId GROUP BY sh.query ORDER BY count DESC")
    List<Object[]> findPopularQueriesByUserId(@Param("userId") Long userId);
    
    void deleteByUserId(Long userId);
    
    void deleteByUserIdAndDateSearchedBefore(Long userId, LocalDateTime before);
}