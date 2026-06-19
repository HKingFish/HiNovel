package cn.haowl.hinovel.novel.domain.entity;

import cn.haowl.hinovel.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Objects;

/**
 * 人物关系实体。
 *
 * <p>存储小说中两个人物之间的关系，包含关系类型和描述。
 * 关系具有方向性：characterId 为主体，targetId 为客体。</p>
 *
 * @author haowl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("novel_character_relation")
public class NovelCharacterRelation extends BaseEntity {

    /**
     * 所属小说 ID。
     */
    private Long novelId;

    /**
     * 人物 ID（主体）。
     */
    private Long characterId;

    /**
     * 目标人物 ID（客体）。
     */
    private Long targetId;

    /**
     * 关系类型。
     *
     * @see RelationType
     */
    private String relationType;

    /**
     * 关系描述。
     */
    private String description;

    // ==================== 关系类型枚举 ====================

    /**
     * 人物关系类型。
     */
    public enum RelationType {
        /**
         * 朋友
         */
        FRIEND,
        /**
         * 敌人
         */
        ENEMY,
        /**
         * 恋人
         */
        LOVER,
        /**
         * 家人
         */
        FAMILY,
        /**
         * 师徒
         */
        MASTER,
        /**
         * 同事
         */
        COLLEAGUE,
        /**
         * 其他
         */
        OTHER
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建人物关系。
     *
     * @param novelId      小说 ID
     * @param characterId  主体人物 ID
     * @param targetId     客体人物 ID
     * @param relationType 关系类型
     * @param description  关系描述
     * @return 人物关系实体
     */
    public static NovelCharacterRelation create(Long novelId, Long characterId,
                                                Long targetId, String relationType,
                                                String description) {
        Objects.requireNonNull(novelId, "小说 ID 不能为空");
        Objects.requireNonNull(characterId, "主体人物 ID 不能为空");
        Objects.requireNonNull(targetId, "客体人物 ID 不能为空");
        if (characterId.equals(targetId)) {
            throw new IllegalArgumentException("主体人物和客体人物不能相同");
        }

        return NovelCharacterRelation.builder()
                .novelId(novelId)
                .characterId(characterId)
                .targetId(targetId)
                .relationType(relationType != null ? relationType : RelationType.OTHER.name())
                .description(description)
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 更新关系信息。
     *
     * @param relationType 新的关系类型
     * @param description  新的关系描述
     */
    public void updateRelation(String relationType, String description) {
        if (relationType != null) {
            this.relationType = relationType;
        }
        if (description != null) {
            this.description = description;
        }
    }

    // ==================== 状态判断 ====================

    /**
     * 判断关系是否属于指定小说。
     *
     * @param novelId 小说 ID
     * @return 是否属于该小说
     */
    public boolean belongsToNovel(Long novelId) {
        return Objects.equals(this.novelId, novelId);
    }

    /**
     * 判断是否涉及指定人物（无论主体或客体）。
     *
     * @param characterId 人物 ID
     * @return 是否涉及该人物
     */
    public boolean involvesCharacter(Long characterId) {
        return Objects.equals(this.characterId, characterId)
                || Objects.equals(this.targetId, characterId);
    }

    /**
     * 判断是否为指定关系类型。
     *
     * @param type 关系类型
     * @return 是否匹配
     */
    public boolean isRelationType(RelationType type) {
        return type != null && type.name().equals(this.relationType);
    }
}
