package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 小说 Agent 配置实体。
 *
 * 为每本小说配置【作者】、【编辑】和【问答】角色使用的 Agent，
 * 不同角色的 Agent 负责不同的 AI 功能场景。
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("novel_agent_config")
public class NovelAgentConfig extends BaseEntity {

    /**
     * 小说 ID。
     */
    private Long novelId;

    /**
     * 作者 Agent ID（负责内容创作、改写）。
     */
    private Long authorAgentId;

    /**
     * 编辑 Agent ID（负责内容审核、校对）。
     */
    private Long editorAgentId;

    /**
     * 问答 Agent ID（负责 AI 问答交互）。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long qaAgentId;

    /**
     * 创建小说 Agent 配置。
     *
     * @param novelId 小说 ID
     * @return 空配置实体（各 Agent 待后续配置）
     */
    public static NovelAgentConfig create(Long novelId) {
        Objects.requireNonNull(novelId, "小说 ID 不能为空");
        return NovelAgentConfig.builder().novelId(novelId).build();
    }

    /**
     * 配置作者 Agent。
     *
     * @param agentId Agent ID
     */
    public void configureAuthorAgent(Long agentId) {
        this.authorAgentId = agentId;
    }

    /**
     * 配置编辑 Agent。
     *
     * @param agentId Agent ID
     */
    public void configureEditorAgent(Long agentId) {
        this.editorAgentId = agentId;
    }

    /**
     * 配置问答 Agent。
     *
     * @param agentId Agent ID
     */
    public void configureQaAgent(Long agentId) {
        this.qaAgentId = agentId;
    }

    /**
     * 判断配置是否属于指定小说。
     *
     * @param novelId 小说 ID
     * @return 是否属于该小说
     */
    public boolean belongsToNovel(Long novelId) {
        return Objects.equals(this.novelId, novelId);
    }

    /**
     * 判断是否配置作者 Agent。
     */
    public boolean hasAuthorAgent() {
        return authorAgentId != null;
    }

    /**
     * 判断是否配置编辑 Agent。
     */
    public boolean hasEditorAgent() {
        return editorAgentId != null;
    }

    /**
     * 判断是否配置问答 Agent。
     */
    public boolean hasQaAgent() {
        return qaAgentId != null;
    }

    /**
     * 判断三个 Agent 是否均已配置。
     */
    public boolean isFullyConfigured() {
        return hasAuthorAgent() && hasEditorAgent() && hasQaAgent();
    }
}
