package com.chatpass.service;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 渲染服务
 * 
 * 将 Markdown 消息内容渲染为 HTML
 * 支持 GitHub Flavored Markdown (GFM)
 */
@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    
    // Markdown 渲染版本（用于版本控制）
    public static final int RENDER_VERSION = 1;

    // @提及检测正则
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_\\-]+)");
    private static final Pattern WILDCARD_MENTION_PATTERN = Pattern.compile("@(all|everyone|channel|stream|topic)", Pattern.CASE_INSENSITIVE);

    public MarkdownService() {
        // GFM 扩展：表格、删除线
        List<Extension> extensions = Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create()
        );

        this.parser = Parser.builder()
                .extensions(extensions)
                .build();

        this.renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
    }

    /**
     * 渲染 Markdown 为 HTML
     */
    public String render(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    /**
     * 渲染并处理 @提及
     * 将 @username 转换为带链接的 HTML
     */
    public String renderWithMentions(String markdown, MentionHandler handler) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        // 馄处理：将 @username 替换为特殊标记
        String processed = processMentions(markdown, handler);

        // 渲染 Markdown
        Node document = parser.parse(processed);
        String html = renderer.render(document);

        // 后处理：恢复 @提及为 HTML 链接
        return postProcessMentions(html);
    }

    /**
     * 预处理 @提及
     */
    private String processMentions(String content, MentionHandler handler) {
        StringBuilder result = new StringBuilder();
        
        // 先检测 wildcard mentions (@all, @everyone, etc.)
        Matcher wildcardMatcher = WILDCARD_MENTION_PATTERN.matcher(content);
        int lastEnd = 0;

        while (wildcardMatcher.find()) {
            result.append(content.substring(lastEnd, wildcardMatcher.start()));
            
            String mention = wildcardMatcher.group(1);
            if (handler != null) {
                handler.onWildcardMention(mention.toLowerCase());
            }
            
            // 保留为特殊标记，渲染后处理
            result.append("[[WILDCARD_MENTION:" + mention.toLowerCase() + "]]");
            lastEnd = wildcardMatcher.end();
        }
        result.append(content.substring(lastEnd));

        // 再检测普通 @mentions
        String intermediate = result.toString();
        result = new StringBuilder();
        
        Matcher mentionMatcher = MENTION_PATTERN.matcher(intermediate);
        lastEnd = 0;

        while (mentionMatcher.find()) {
            result.append(intermediate.substring(lastEnd, mentionMatcher.start()));
            
            String username = mentionMatcher.group(1);
            if (handler != null) {
                handler.onMention(username);
            }
            
            // 保留为特殊标记
            result.append("[[MENTION:" + username + "]]");
            lastEnd = mentionMatcher.end();
        }
        result.append(intermediate.substring(lastEnd));

        return result.toString();
    }

    /**
     * 后处理：将标记转换为 HTML
     */
    private String postProcessMentions(String html) {
        // 处理 wildcard mentions
        html = html.replaceAll("\\[\\[WILDCARD_MENTION:([^\\]]+)\\]\\]", 
                "<span class=\"user-mention wildcard-mention\" data-type=\"wildcard\">@\\1</span>");
        
        // 处理普通 mentions
        html = html.replaceAll("\\[\\[MENTION:([^\\]]+)\\]\\]", 
                "<span class=\"user-mention\" data-username=\"\\1\">@\\1</span>");
        
        return html;
    }

    /**
     * 检测消息中的 @提及
     * 返回被提及的用户名列表
     */
    public MentionResult detectMentions(String content) {
        MentionResult result = new MentionResult();
        
        if (content == null || content.isEmpty()) {
            return result;
        }

        // 检测 wildcard mentions
        Matcher wildcardMatcher = WILDCARD_MENTION_PATTERN.matcher(content);
        while (wildcardMatcher.find()) {
            result.hasWildcardMention = true;
            result.wildcardType = wildcardMatcher.group(1).toLowerCase();
        }

        // 检测普通 mentions
        Matcher mentionMatcher = MENTION_PATTERN.matcher(content);
        while (mentionMatcher.find()) {
            // 排除 wildcard mentions
            String username = mentionMatcher.group(1);
            if (!username.equalsIgnoreCase("all") && 
                !username.equalsIgnoreCase("everyone") &&
                !username.equalsIgnoreCase("channel") &&
                !username.equalsIgnoreCase("stream") &&
                !username.equalsIgnoreCase("topic")) {
                result.mentionedUsers.add(username);
            }
        }

        return result;
    }

    /**
     * @提及处理回调
     */
    public interface MentionHandler {
        void onMention(String username);
        void onWildcardMention(String type);
    }

    /**
     * @提及检测结果
     */
    public static class MentionResult {
        public List<String> mentionedUsers = new java.util.ArrayList<>();
        public boolean hasWildcardMention = false;
        public String wildcardType = null;
        
        public boolean hasMentions() {
            return !mentionedUsers.isEmpty() || hasWildcardMention;
        }
    }
}