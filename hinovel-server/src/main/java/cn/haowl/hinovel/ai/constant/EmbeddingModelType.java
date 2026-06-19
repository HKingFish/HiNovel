package cn.haowl.hinovel.ai.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Embedding 模型类型枚举。
 *
 * <p>定义支持的嵌入模型运行模式，用于区分本地 ONNX 模型和远程 API 调用。</p>
 *
 * @author haowl
 */
@Getter
@AllArgsConstructor
public enum EmbeddingModelType {

    /**
     * 本地 ONNX 模型（如 BGE-M3，无需网络调用，进程内推理）。
     */
    ONNX("onnx"),

    /**
     * OpenAI 兼容接口（阿里云百炼、硅基流动等兼容 OpenAI 协议的服务）。
     */
    OPENAI("openai");

    /**
     * 配置文件中对应的类型标识。
     */
    private final String value;
}
