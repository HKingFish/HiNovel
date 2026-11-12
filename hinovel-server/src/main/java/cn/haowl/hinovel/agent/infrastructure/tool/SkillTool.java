package cn.haowl.hinovel.agent.infrastructure.tool;

import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import cn.haowl.hinovel.ai.domain.entity.SkillReadCall;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.haowl.hinovel.agent.constant.SkillConstants.DE_AI_SKILL_ID;
import static cn.haowl.hinovel.agent.constant.SkillConstants.SKILL_READ_FAILED;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : 单次 Agent 执行的 Read Tool 与读取结果
 */
@Slf4j
public class SkillTool {

    /** 模型调用的 Read Tool 名称。 */
    public static final String TOOL_NAME_LOAD = "loadSkill";

    /** 当前 Agent 按名称索引的挂载。 */
    private final Map<String, AgentSkill> boundSkills;

    /** 根据已授权的目录 ID 读取正文。 */
    private final Function<Long, String> readContent;

    /** 按发起顺序保存读取及其结果。 */
    private final Map<Integer, SkillReadCall> calls = new TreeMap<>();

    /** 流中断后停止接受新读取及在途结果。 */
    private boolean cancelled;

    /** 按首次读取成功顺序保存名称。 */
    private final Set<String> loadedNames = new LinkedHashSet<>();

    /** 等待向聊天流推送的成功名称。 */
    private final Queue<String> pendingNames = new ArrayDeque<>();

    /** 为当次执行保存挂载和正文读取方式。 */
    public SkillTool(List<AgentSkill> skills, Function<Long, String> readContent) {
        this.boundSkills = skills.stream().collect(Collectors.toUnmodifiableMap(AgentSkill::getName, skill -> skill));
        this.readContent = readContent;
    }

    /** 判断当前执行是否挂载去 AI 味。 */
    public boolean hasDefaultSkill() {
        return boundSkills.values().stream().anyMatch(skill -> DE_AI_SKILL_ID.equals(skill.getId()));
    }

    /** 在服务端主动读取已挂载的去 AI 味。 */
    public String loadDefaultSkill() {
        return boundSkills.values().stream()
                .filter(skill -> DE_AI_SKILL_ID.equals(skill.getId()))
                .findFirst()
                .map(skill -> loadSkill(skill.getName()))
                .orElse("");
    }

    /** 当前回复首次成功加载的名称快照。 */
    public synchronized List<String> loadedNames() {
        return List.copyOf(loadedNames);
    }

    /** 取出尚未推送的一个成功名称。 */
    public synchronized String pollLoadedName() {
        return pendingNames.poll();
    }

    /** 按发起顺序返回已结束的 Read Tool 调用。 */
    public synchronized List<SkillReadCall> readCalls() {
        return List.copyOf(calls.values());
    }

    /** 将在途读取保留为失败记录，并固定成功名称快照。 */
    public synchronized void cancel() {
        cancelled = true;
    }

    /** 读取已挂载的完整正文，失败时向模型返回可继续处理的信息。 */
    @Tool("按名称读取当前 Agent 已挂载的 Skill 全文。")
    public String loadSkill(@P("可用 Skill 目录中的名称") String name) {
        int order;
        synchronized (this) {
            if (cancelled) {
                return "当前调用已取消。";
            }
            order = calls.size() + 1;
            calls.put(order, new SkillReadCall(order, name == null ? "" : name,
                    SkillReadCall.Status.FAILED, SKILL_READ_FAILED));
        }
        AgentSkill skill = name == null ? null : boundSkills.get(name);
        if (skill != null) {
            try {
                String content = readContent.apply(skill.getId());
                synchronized (this) {
                    if (cancelled) {
                        return "当前调用已取消。";
                    }
                    calls.put(order, new SkillReadCall(order, name, SkillReadCall.Status.SUCCESS, null));
                    if (loadedNames.add(name)) {
                        pendingNames.add(name);
                    }
                }
                return content;
            } catch (RuntimeException e) {
                log.warn("[loadSkill][读取失败，skillId={}]", skill.getId(), e);
            }
        }
        return "无法读取 Skill「" + name + "」。请继续处理请求。若这是用户指定的风格，请说明本次未加载。";
    }
}
