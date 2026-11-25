package cn.haowl.hinovel.ai.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis 聊天记忆存储。
 */
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    /**
     * Redis 聊天记忆键前缀。
     */
    public static final String CHAT_MEMORY_PREFIX = "CHAT_MEMORY:";

    /**
     * 聊天消息序列化文本存储客户端。
     */
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return ChatMessageDeserializer.messagesFromJson(
            redisTemplate.opsForValue().get(CHAT_MEMORY_PREFIX + memoryId));
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        redisTemplate.opsForValue()
                .set(CHAT_MEMORY_PREFIX + memoryId, ChatMessageSerializer.messagesToJson(messages));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(CHAT_MEMORY_PREFIX + memoryId);
    }
}
