package cn.haowl.hinovel.ai.config;

import cn.haowl.hinovel.ai.constant.AiConstants;
import cn.haowl.hinovel.ai.constant.EmbeddingModelType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Embedding 模型配置属性。
 *
 * <p>支持两种模式：
 * <ul>
 *   <li>onnx：本地 ONNX 模型（如 BGE-M3，进程内推理，无需网络调用）</li>
 *   <li>openai：OpenAI 兼容接口（阿里云百炼 text-embedding-v3 等）</li>
 * </ul>
 * </p>
 *
 * @author haowl
 */
@Data
@Component
@ConfigurationProperties(prefix = "hinovel.embedding")
public class EmbeddingProperties {

    /**
     * 嵌入模型类型。
     *
     * @see EmbeddingModelType
     */
    private String type = EmbeddingModelType.OPENAI.getValue();

    /**
     * OpenAI 兼容接口配置（阿里云百炼等）。
     */
    private OpenAi openai = new OpenAi();

    /**
     * ONNX 本地模型配置（如 BGE-M3）。
     */
    private Onnx onnx = new Onnx();

    @Data
    public static class OpenAi {

        /**
         * API 密钥。
         */
        private String apiKey;

        /**
         * API 基础地址（阿里云百炼兼容 OpenAI 协议）。
         */
        private String baseUrl = AiConstants.QWEN_COMPATIBLE_BASE_URL;

        /**
         * 模型名称。
         */
        private String modelName = AiConstants.DEFAULT_EMBEDDING_MODEL;

        /**
         * 向量维度（text-embedding-v3 支持 512、768、1024）。
         */
        private Integer dimensions = AiConstants.DEFAULT_EMBEDDING_DIMENSIONS;
    }

    @Data
    public static class Onnx {

        /**
         * ONNX 模型文件路径（如 /path/to/bge-m3/model.onnx）。
         */
        private String modelPath;

        /**
         * 分词器文件路径（如 /path/to/bge-m3/tokenizer.json）。
         */
        private String tokenizerPath;

        /**
         * 向量维度（BGE-M3 默认 1024）。
         */
        private Integer dimensions = AiConstants.BGE_M3_EMBEDDING_DIMENSIONS;
    }
}
