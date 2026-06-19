package cn.haowl.hinovel.agent.infrastructure.tool;

import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;
import cn.haowl.hinovel.novel.domain.repository.NovelCharacterRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 小说人物图谱管理工具。
 *
 * <p>供问答 Agent 调用，提供人物的查询、新增、修改以及人物关系的管理能力。
 * 使用 LangChain4J {@link Tool} 注解，可被 AiService 自动发现和调用。</p>
 *
 * <p>该工具不注册为 Spring Bean，而是在需要时通过构造方法注入依赖，
 * 以支持不同小说使用独立的上下文。</p>
 *
 * @author haowl
 */
@Slf4j
public class NovelCharacterTool {

    private final Long novelId;
    private final NovelCharacterRepository characterRepository;

    /**
     * 构造人物图谱工具实例。
     *
     * @param novelId             小说 ID
     * @param characterRepository 人物仓储
     */
    public NovelCharacterTool(Long novelId, NovelCharacterRepository characterRepository) {
        this.novelId = novelId;
        this.characterRepository = characterRepository;
    }

    /**
     * 创建工具实例的工厂方法。
     *
     * @param novelId             小说 ID
     * @param characterRepository 人物仓储
     * @return 人物图谱工具实例
     */
    public static NovelCharacterTool create(Long novelId,
                                            NovelCharacterRepository characterRepository) {
        return new NovelCharacterTool(novelId, characterRepository);
    }

    // ==================== 人物查询工具 ====================

    /**
     * 获取小说的所有人物列表。
     *
     * @return 人物列表信息
     */
    @Tool("获取小说的所有人物列表，包含人物 ID、姓名、角色类型、身份、性别、年龄等基本信息。" +
            "在新增或修改人物前，先调用此工具了解当前人物图谱。")
    public String listCharacters() {
        log.info("获取人物列表，小说ID={}", novelId);
        try {
            List<NovelCharacter> characters = characterRepository.findByNovelId(novelId);
            if (characters.isEmpty()) {
                return "当前小说尚无人物。";
            }
            StringBuilder result = new StringBuilder();
            result.append("共 ").append(characters.size()).append(" 个人物：\n\n");
            for (NovelCharacter character : characters) {
                result.append("- 人物ID：").append(character.getId())
                        .append(" | 姓名：").append(character.getName())
                        .append(" | 角色：").append(formatRoleType(character.getRoleType()));
                if (character.getIdentity() != null) {
                    result.append(" | 身份：").append(character.getIdentity());
                }
                if (character.getGender() != null) {
                    result.append(" | 性别：").append(formatGender(character.getGender()));
                }
                if (character.getAge() != null) {
                    result.append(" | 年龄：").append(character.getAge());
                }
                result.append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            log.error("获取人物列表失败，小说ID={}，异常信息：{}", novelId, e.getMessage(), e);
            return "获取人物列表失败：" + e.getMessage();
        }
    }

    /**
     * 获取指定人物的详细信息。
     *
     * @param characterId 人物 ID
     * @return 人物详细信息
     */
    @Tool("获取指定人物的完整详细信息，包含外貌、性格、背景、目标、能力等。" +
            "在修改人物信息前，先调用此工具了解人物当前设定。")
    public String getCharacterDetail(@P("人物 ID") Long characterId) {
        log.info("获取人物详情，小说ID={}，人物ID={}", novelId, characterId);
        try {
            Optional<NovelCharacter> optional = characterRepository.findById(characterId);
            if (optional.isEmpty()) {
                return "人物不存在，人物ID：" + characterId;
            }
            NovelCharacter character = optional.get();
            if (!character.belongsToNovel(novelId)) {
                return "该人物不属于当前小说，无法查看。";
            }
            return buildCharacterDetailText(character);
        } catch (Exception e) {
            log.error("获取人物详情失败，小说ID={}，人物ID={}，异常信息：{}",
                    novelId, characterId, e.getMessage(), e);
            return "获取人物详情失败：" + e.getMessage();
        }
    }

    // ==================== 人物新增工具 ====================

    /**
     * 创建新人物。
     *
     * @param name        人物姓名
     * @param roleType    角色类型（PROTAGONIST-主角，SUPPORTING-配角，ANTAGONIST-反派，OTHER-其他）
     * @param gender      性别（MALE-男，FEMALE-女，OTHER-其他，可为空）
     * @param identity    身份/职业（可为空）
     * @param personality 性格特点（可为空）
     * @param background  背景故事（可为空）
     * @return 操作结果描述
     */
    @Tool("创建一个新的小说人物。至少需要提供姓名和角色类型，" +
            "其他信息如性别、身份、性格、背景等可选填。" +
            "角色类型可选：PROTAGONIST（主角）、SUPPORTING（配角）、ANTAGONIST（反派）、OTHER（其他）。")
    public String createCharacter(
            @P("人物姓名") String name,
            @P("角色类型：PROTAGONIST/SUPPORTING/ANTAGONIST/OTHER") String roleType,
            @P("性别：MALE/FEMALE/OTHER，可为空") String gender,
            @P("身份/职业，可为空") String identity,
            @P("性格特点描述，可为空") String personality,
            @P("背景故事描述，可为空") String background) {
        log.info("创建人物，小说ID={}，姓名={}", novelId, name);
        try {
            Integer maxOrder = characterRepository.findMaxSortOrder(novelId);
            int sortOrder = maxOrder != null ? maxOrder + 1 : 1;

            NovelCharacter character = NovelCharacter.create(novelId, name, roleType, sortOrder);
            character.updateBasicInfo(name, null, gender, null, identity);
            character.updateCharacterProfile(null, personality, background, null, null);

            NovelCharacter saved = characterRepository.save(character);
            return "人物创建成功，人物ID：" + saved.getId()
                    + "，姓名：" + saved.getName()
                    + "，角色：" + formatRoleType(saved.getRoleType());
        } catch (Exception e) {
            log.error("创建人物失败，小说ID={}，姓名={}，异常信息：{}",
                    novelId, name, e.getMessage(), e);
            return "创建人物失败：" + e.getMessage();
        }
    }

    // ==================== 人物修改工具 ====================

    /**
     * 更新人物的基本信息。
     *
     * @param characterId 人物 ID
     * @param name        姓名（传空字符串表示不修改）
     * @param roleType    角色类型（传空字符串表示不修改）
     * @param gender      性别（传空字符串表示不修改）
     * @param identity    身份/职业（传空字符串表示不修改）
     * @return 操作结果描述
     */
    @Tool("更新人物的基本信息（姓名、角色类型、性别、身份）。" +
            "传入空字符串的字段表示不修改。需要先通过 listCharacters 获取人物列表确认人物 ID。")
    public String updateCharacterBasicInfo(
            @P("人物 ID") Long characterId,
            @P("姓名，空字符串表示不修改") String name,
            @P("角色类型：PROTAGONIST/SUPPORTING/ANTAGONIST/OTHER，空字符串表示不修改") String roleType,
            @P("性别：MALE/FEMALE/OTHER，空字符串表示不修改") String gender,
            @P("身份/职业，空字符串表示不修改") String identity) {
        log.info("更新人物基本信息，小说ID={}，人物ID={}", novelId, characterId);
        try {
            Optional<NovelCharacter> optional = characterRepository.findById(characterId);
            if (optional.isEmpty()) {
                return "人物不存在，人物ID：" + characterId;
            }
            NovelCharacter character = optional.get();
            if (!character.belongsToNovel(novelId)) {
                return "该人物不属于当前小说，无法修改。";
            }

            character.updateBasicInfo(
                    blankToNull(name), null,
                    blankToNull(gender), null, blankToNull(identity)
            );
            if (isNotBlank(roleType)) {
                character.setRoleType(roleType);
            }

            characterRepository.save(character);
            return "人物基本信息更新成功，人物ID：" + characterId + "，姓名：" + character.getName();
        } catch (Exception e) {
            log.error("更新人物基本信息失败，小说ID={}，人物ID={}，异常信息：{}",
                    novelId, characterId, e.getMessage(), e);
            return "更新人物基本信息失败：" + e.getMessage();
        }
    }

    /**
     * 更新人物的人设描述（性格、背景、目标、能力、外貌）。
     *
     * @param characterId 人物 ID
     * @param personality 性格特点（传空字符串表示不修改）
     * @param background  背景故事（传空字符串表示不修改）
     * @param goals       目标/动机（传空字符串表示不修改）
     * @param abilities   能力/技能（传空字符串表示不修改）
     * @param appearance  外貌特征（传空字符串表示不修改）
     * @return 操作结果描述
     */
    @Tool("更新人物的人设描述信息（性格、背景、目标、能力、外貌）。" +
            "传入空字符串的字段表示不修改。需要先通过 getCharacterDetail 了解当前人设。")
    public String updateCharacterProfile(
            @P("人物 ID") Long characterId,
            @P("性格特点描述，空字符串表示不修改") String personality,
            @P("背景故事描述，空字符串表示不修改") String background,
            @P("目标/动机描述，空字符串表示不修改") String goals,
            @P("能力/技能描述，空字符串表示不修改") String abilities,
            @P("外貌特征描述，空字符串表示不修改") String appearance) {
        log.info("更新人物人设描述，小说ID={}，人物ID={}", novelId, characterId);
        try {
            Optional<NovelCharacter> optional = characterRepository.findById(characterId);
            if (optional.isEmpty()) {
                return "人物不存在，人物ID：" + characterId;
            }
            NovelCharacter character = optional.get();
            if (!character.belongsToNovel(novelId)) {
                return "该人物不属于当前小说，无法修改。";
            }

            character.updateCharacterProfile(
                    blankToNull(appearance), blankToNull(personality),
                    blankToNull(background), blankToNull(goals), blankToNull(abilities)
            );

            characterRepository.save(character);
            return "人物人设描述更新成功，人物ID：" + characterId + "，姓名：" + character.getName();
        } catch (Exception e) {
            log.error("更新人物人设描述失败，小说ID={}，人物ID={}，异常信息：{}",
                    novelId, characterId, e.getMessage(), e);
            return "更新人物人设描述失败：" + e.getMessage();
        }
    }

    // ==================== 人物关系工具 ====================

    /**
     * 获取小说的所有人物关系。
     *
     * @return 人物关系列表
     */
    @Tool("获取小说的所有人物关系列表，包含关系 ID、主体人物、目标人物、关系类型和描述。" +
            "在新增关系前，先调用此工具了解已有关系，避免重复创建。")
    public String listCharacterRelations() {
        log.info("获取人物关系列表，小说ID={}", novelId);
        try {
            List<NovelCharacterRelation> relations = characterRepository.findRelationsByNovelId(novelId);
            if (relations.isEmpty()) {
                return "当前小说尚无人物关系。";
            }
            // 加载人物名称映射
            List<NovelCharacter> characters = characterRepository.findByNovelId(novelId);

            StringBuilder result = new StringBuilder();
            result.append("共 ").append(relations.size()).append(" 条人物关系：\n\n");
            for (NovelCharacterRelation relation : relations) {
                String sourceName = findCharacterName(characters, relation.getCharacterId());
                String targetName = findCharacterName(characters, relation.getTargetId());
                result.append("- 关系ID：").append(relation.getId())
                        .append(" | ").append(sourceName)
                        .append(" → ").append(targetName)
                        .append(" | 关系：").append(relation.getRelationType());
                if (relation.getDescription() != null && !relation.getDescription().isBlank()) {
                    result.append(" | 描述：").append(relation.getDescription());
                }
                result.append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            log.error("获取人物关系列表失败，小说ID={}，异常信息：{}", novelId, e.getMessage(), e);
            return "获取人物关系列表失败：" + e.getMessage();
        }
    }

    /**
     * 创建人物关系。
     *
     * @param characterId  主体人物 ID
     * @param targetId     目标人物 ID
     * @param relationType 关系类型
     * @param description  关系描述
     * @return 操作结果描述
     */
    @Tool("创建两个人物之间的关系。关系具有方向性：从主体人物指向目标人物。" +
            "关系类型可选：FRIEND（朋友）、ENEMY（敌人）、LOVER（恋人）、FAMILY（家人）、" +
            "MASTER（师徒）、COLLEAGUE（同事）、OTHER（其他）。" +
            "需要先通过 listCharacters 获取人物列表确认人物 ID。")
    public String createCharacterRelation(
            @P("主体人物 ID") Long characterId,
            @P("目标人物 ID") Long targetId,
            @P("关系类型：FRIEND/ENEMY/LOVER/FAMILY/MASTER/COLLEAGUE/OTHER") String relationType,
            @P("关系描述，如'青梅竹马'、'亦师亦友'等") String description) {
        log.info("创建人物关系，小说ID={}，主体={}，目标={}", novelId, characterId, targetId);
        try {
            NovelCharacterRelation relation = NovelCharacterRelation.create(
                    novelId, characterId, targetId, relationType, description);
            NovelCharacterRelation saved = characterRepository.saveRelation(relation);

            // 获取人物名称用于反馈
            List<NovelCharacter> characters = characterRepository.findByNovelId(novelId);
            String sourceName = findCharacterName(characters, characterId);
            String targetName = findCharacterName(characters, targetId);

            return "人物关系创建成功，关系ID：" + saved.getId()
                    + "，" + sourceName + " → " + targetName
                    + "，关系：" + relationType;
        } catch (Exception e) {
            log.error("创建人物关系失败，小说ID={}，主体={}，目标={}，异常信息：{}",
                    novelId, characterId, targetId, e.getMessage(), e);
            return "创建人物关系失败：" + e.getMessage();
        }
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 构建人物详细信息文本。
     *
     * @param character 人物实体
     * @return 格式化的详细信息
     */
    private String buildCharacterDetailText(NovelCharacter character) {
        StringBuilder detail = new StringBuilder();
        detail.append("【人物详情】\n");
        detail.append("姓名：").append(character.getName()).append("\n");
        detail.append("角色类型：").append(formatRoleType(character.getRoleType())).append("\n");

        if (character.getAlias() != null) {
            detail.append("别名：").append(character.getAlias()).append("\n");
        }
        if (character.getGender() != null) {
            detail.append("性别：").append(formatGender(character.getGender())).append("\n");
        }
        if (character.getAge() != null) {
            detail.append("年龄：").append(character.getAge()).append("\n");
        }
        if (character.getIdentity() != null) {
            detail.append("身份：").append(character.getIdentity()).append("\n");
        }
        if (character.getAppearance() != null) {
            detail.append("外貌：").append(character.getAppearance()).append("\n");
        }
        if (character.getPersonality() != null) {
            detail.append("性格：").append(character.getPersonality()).append("\n");
        }
        if (character.getBackground() != null) {
            detail.append("背景：").append(character.getBackground()).append("\n");
        }
        if (character.getGoals() != null) {
            detail.append("目标：").append(character.getGoals()).append("\n");
        }
        if (character.getAbilities() != null) {
            detail.append("能力：").append(character.getAbilities()).append("\n");
        }
        if (character.getNotes() != null) {
            detail.append("备注：").append(character.getNotes()).append("\n");
        }

        // 加载人物关系
        List<NovelCharacterRelation> relations =
                characterRepository.findRelationsByCharacterId(character.getId());
        if (!relations.isEmpty()) {
            List<NovelCharacter> allCharacters = characterRepository.findByNovelId(novelId);
            detail.append("\n【人物关系】\n");
            for (NovelCharacterRelation relation : relations) {
                String targetName = findCharacterName(allCharacters, relation.getTargetId());
                detail.append("- → ").append(targetName)
                        .append("（").append(relation.getRelationType()).append("）");
                if (relation.getDescription() != null && !relation.getDescription().isBlank()) {
                    detail.append("：").append(relation.getDescription());
                }
                detail.append("\n");
            }
        }

        return detail.toString();
    }

    /**
     * 从人物列表中查找人物姓名。
     *
     * @param characters  人物列表
     * @param characterId 人物 ID
     * @return 人物姓名，未找到时返回"未知人物"
     */
    private String findCharacterName(List<NovelCharacter> characters, Long characterId) {
        return characters.stream()
                .filter(c -> c.getId().equals(characterId))
                .map(NovelCharacter::getName)
                .findFirst()
                .orElse("未知人物");
    }

    /**
     * 格式化角色类型为中文。
     *
     * @param roleType 角色类型
     * @return 中文角色类型
     */
    private String formatRoleType(String roleType) {
        if (roleType == null) {
            return "其他";
        }
        return switch (roleType) {
            case NovelCharacter.ROLE_TYPE_PROTAGONIST -> "主角";
            case NovelCharacter.ROLE_TYPE_SUPPORTING -> "配角";
            case NovelCharacter.ROLE_TYPE_ANTAGONIST -> "反派";
            default -> "其他";
        };
    }

    /**
     * 格式化性别为中文。
     *
     * @param gender 性别
     * @return 中文性别
     */
    private String formatGender(String gender) {
        if (gender == null) {
            return "未知";
        }
        return switch (gender) {
            case "MALE" -> "男";
            case "FEMALE" -> "女";
            default -> "其他";
        };
    }

    /**
     * 空白字符串转 null（用于"不修改"语义）。
     *
     * @param value 字符串值
     * @return 非空白时返回原值，空白时返回 null
     */
    private String blankToNull(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }

    /**
     * 判断字符串是否非空白。
     *
     * @param value 字符串值
     * @return 非空白返回 true
     */
    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
