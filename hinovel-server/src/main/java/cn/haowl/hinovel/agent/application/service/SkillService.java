package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import cn.haowl.hinovel.agent.domain.repository.SkillRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.haowl.hinovel.agent.constant.SkillConstants.DE_AI_SKILL_ID;
import static cn.haowl.hinovel.agent.enums.SkillErrorCodeConstants.SKILL_NOT_EXISTS;
import static cn.haowl.hinovel.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : 内置 Skill 目录、正文读取和 Agent 挂载
 */
@Service
public class SkillService {

    /** 持久化 Agent 的 Skill 挂载。 */
    @Autowired
    private SkillRepository skillRepository;

    /** 按稳定 ID 索引已校验的目录元数据。 */
    private Map<Long, AgentSkill> catalog;

    /** 启动时校验目录及所有正文。 */
    @PostConstruct
    void loadCatalog() {
        try (var input = new ClassPathResource("skills/catalog.json").getInputStream()) {
            Map<Long, AgentSkill> entries = new HashMap<>();
            List<AgentSkill> skills = new ObjectMapper().readValue(input, new TypeReference<>() {});
            for (AgentSkill skill : skills) {
                if (skill.getName() == null || skill.getName().isBlank()
                        || skill.getDescription() == null || skill.getDescription().isBlank()
                        || entries.putIfAbsent(skill.getId(), skill) != null
                        || readResource(skill.getId()).isBlank()) {
                    throw new IllegalStateException("内置 Skill 资源无效，ID=" + skill.getId());
                }
            }
            if (!entries.keySet().equals(Set.of(1L, 2L, DE_AI_SKILL_ID))
                    || skills.stream().map(AgentSkill::getName).distinct().count() != skills.size()) {
                throw new IllegalStateException("内置 Skill 目录 ID 或名称无效");
            }
            catalog = Map.copyOf(entries);
        } catch (IOException e) {
            throw new UncheckedIOException("无法初始化内置 Skill 目录", e);
        }
    }

    /** 按 ID 顺序返回内置 Skill 元数据。 */
    public List<AgentSkill> listBuiltin() {
        return catalog.values().stream()
                .sorted(Comparator.comparing(AgentSkill::getId))
                .toList();
    }

    /** 在 Read Tool 校验挂载后读取完整正文。 */
    public String readContent(Long skillId) {
        if (skillId == null || !catalog.containsKey(skillId)) {
            throw exception(SKILL_NOT_EXISTS);
        }
        try {
            String content = readResource(skillId);
            if (content.isBlank()) {
                throw new IllegalStateException("内置 Skill 正文为空，ID=" + skillId);
            }
            return content;
        } catch (IOException e) {
            throw new UncheckedIOException("无法读取内置 Skill，ID=" + skillId, e);
        }
    }

    /** 查询 Agent 的有效挂载。 */
    public List<Long> listBoundSkillIds(Long agentId) {
        return skillRepository.findSkillIdsByAgentId(agentId).stream().filter(catalog::containsKey).toList();
    }

    /** 批量查询 Agent 的有效挂载。 */
    public Map<Long, List<Long>> listBoundSkillIds(Collection<Long> agentIds) {
        Map<Long, List<Long>> bindings = new HashMap<>();
        skillRepository.findSkillIdsByAgentIds(agentIds).forEach((agentId, skillIds) ->
                bindings.put(agentId, skillIds.stream().filter(catalog::containsKey).toList()));
        return bindings;
    }

    /** 查询当前 Agent 已挂载的内置 Skill 元数据。 */
    public List<AgentSkill> listBoundSkills(Long agentId) {
        return listBoundSkillIds(agentId).stream().map(catalog::get).toList();
    }

    /** 验证并替换调用方已获准编辑的 Agent 的挂载。 */
    public void replaceAgentBindings(Long agentId, List<Long> skillIds) {
        if (skillIds == null) {
            return;
        }
        List<Long> ids = skillIds.stream().distinct().toList();
        if (ids.stream().anyMatch(id -> id == null || !catalog.containsKey(id))) {
            throw exception(SKILL_NOT_EXISTS);
        }
        skillRepository.replaceBindings(agentId, ids);
    }

    /** 以 UTF-8 读取 classpath 内的正文。 */
    private String readResource(Long skillId) throws IOException {
        return new ClassPathResource("skills/" + skillId + ".md").getContentAsString(StandardCharsets.UTF_8);
    }
}
