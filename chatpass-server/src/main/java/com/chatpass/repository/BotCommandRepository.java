package com.chatpass.repository;

import com.chatpass.entity.BotCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BotCommandRepository
 */
@Repository
public interface BotCommandRepository extends JpaRepository<BotCommand, Long> {

    /**
     * 查找 Bot 的所有命令
     */
    @Query("SELECT c FROM BotCommand c WHERE c.bot.id = :botId ORDER BY c.commandName ASC")
    List<BotCommand> findByBotId(@Param("botId") Long botId);

    /**
     * 查找活跃命令
     */
    @Query("SELECT c FROM BotCommand c WHERE c.bot.id = :botId AND c.isActive = true ORDER BY c.commandName ASC")
    List<BotCommand> findActiveCommands(@Param("botId") Long botId);

    /**
     * 查找特定命令
     */
    @Query("SELECT c FROM BotCommand c WHERE c.bot.id = :botId AND c.commandName = :commandName")
    Optional<BotCommand> findByBotIdAndCommandName(@Param("botId") Long botId, @Param("commandName") String commandName);

    /**
     * 检查命令是否存在
     */
    @Query("SELECT COUNT(c) > 0 FROM BotCommand c WHERE c.bot.id = :botId AND c.commandName = :commandName")
    boolean existsCommand(@Param("botId") Long botId, @Param("commandName") String commandName);

    /**
     * 统计 Bot 的命令数量
     */
    @Query("SELECT COUNT(c) FROM BotCommand c WHERE c.bot.id = :botId")
    Long countByBotId(@Param("botId") Long botId);

    /**
     * 删除 Bot 的所有命令
     */
    @Query("DELETE FROM BotCommand c WHERE c.bot.id = :botId")
    void deleteByBotId(@Param("botId") Long botId);
}