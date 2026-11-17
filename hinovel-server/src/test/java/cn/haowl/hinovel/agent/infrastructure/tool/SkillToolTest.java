package cn.haowl.hinovel.agent.infrastructure.tool;

import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import cn.haowl.hinovel.ai.constant.SkillReadStatusEnum;
import cn.haowl.hinovel.ai.domain.entity.SkillReadCall;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单次执行的 Skill 读取及事件。
 */
class SkillToolTest {

    /**
     * 验证返回完整正文且仅发布一次名称。
     */
    @Test
    void returnsFullContentAndPublishesTheNameOnce() {
        String content = "开头" + "x".repeat(24_000) + "尾部";
        SkillTool tool = new SkillTool(List.of(skill(1L, "冷峻悬疑")), id -> content);

        assertEquals(content, tool.loadSkill("冷峻悬疑"));
        assertEquals(content, tool.loadSkill("冷峻悬疑"));
        assertEquals(List.of("冷峻悬疑"), tool.loadedNames());
        assertEquals("冷峻悬疑", tool.pollLoadedName());
        assertNull(tool.pollLoadedName());
        assertEquals(List.of(1, 2), tool.readCalls().stream().map(SkillReadCall::getSequence).toList());
    }

    /**
     * 验证未挂载风格不会读取正文资源。
     */
    @Test
    void unmountedStyleDoesNotReadTheResource() {
        SkillTool tool = new SkillTool(List.of(), id -> fail("读取了未挂载的正文"));

        assertTrue(tool.loadSkill("冷峻悬疑").contains("请继续处理请求"));
        assertEquals(SkillReadStatusEnum.FAILED, tool.readCalls().getFirst().getStatus());
        assertNull(tool.pollLoadedName());
    }

    /**
     * 验证默认 Skill 成功与风格失败的记录顺序。
     */
    @Test
    void recordsDefaultSuccessAndStyleFailureInOrder() {
        SkillTool tool = new SkillTool(List.of(skill(3L, "去 AI 味"), skill(1L, "冷峻悬疑")), id -> {
            if (Long.valueOf(1L).equals(id)) {
                throw new UncheckedIOException(new IOException("资源不可读"));
            }
            return "去 AI 味正文";
        });

        assertEquals("去 AI 味正文", tool.loadDefaultSkill());
        assertTrue(tool.loadSkill("冷峻悬疑").contains("本次未加载"));
        assertEquals(List.of("去 AI 味"), tool.loadedNames());
        assertEquals(List.of(SkillReadStatusEnum.SUCCESS, SkillReadStatusEnum.FAILED),
                tool.readCalls().stream().map(SkillReadCall::getStatus).toList());
        assertEquals("SKILL_READ_FAILED", tool.readCalls().get(1).getErrorCode());
        assertEquals("去 AI 味", tool.pollLoadedName());
        assertNull(tool.pollLoadedName());
    }

    /**
     * 验证调用日志与成功名称各自的顺序。
     */
    @Test
    void logUsesStartOrderAndNamesUseFirstSuccessOrder() throws Exception {
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch allowFirstToFinish = new CountDownLatch(1);
        SkillTool tool = new SkillTool(List.of(skill(1L, "冷峻悬疑"), skill(2L, "温暖细腻")), id -> {
            if (Long.valueOf(1L).equals(id)) {
                firstStarted.countDown();
                try {
                    assertTrue(allowFirstToFinish.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
            }
            return "正文";
        });

        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> tool.loadSkill("冷峻悬疑"));
        try {
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
            tool.loadSkill("温暖细腻");
        } finally {
            allowFirstToFinish.countDown();
        }
        first.get(5, TimeUnit.SECONDS);

        assertEquals(List.of("冷峻悬疑", "温暖细腻"),
                tool.readCalls().stream().map(SkillReadCall::getSkillName).toList());
        assertEquals(List.of("温暖细腻", "冷峻悬疑"), tool.loadedNames());
    }

    /**
     * 构造测试 Skill 元数据。
     */
    private AgentSkill skill(Long id, String name) {
        AgentSkill skill = new AgentSkill();
        skill.setId(id);
        skill.setName(name);
        return skill;
    }
}
