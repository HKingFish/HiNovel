package cn.haowl.hinovel.agent.application.service;

import cn.haowl.hinovel.agent.domain.repository.SkillRepository;
import cn.haowl.hinovel.agent.domain.entity.AgentSkill;
import cn.haowl.hinovel.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 内置 Skill 目录和挂载验证。
 */
@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    @BeforeEach
    void loadCatalog() {
        skillService.loadCatalog();
    }

    @Test
    void catalogReturnsThreeStableEntriesWithoutQueryingTheDatabase() {
        List<AgentSkill> items = skillService.listBuiltin();

        assertEquals(List.of(1L, 2L, 3L), items.stream().map(AgentSkill::getId).toList());
        assertEquals(List.of("冷峻悬疑", "温暖细腻", "去 AI 味"),
                items.stream().map(AgentSkill::getName).toList());
        assertTrue(items.stream().noneMatch(item -> item.getDescription().isBlank()));
        verifyNoInteractions(skillRepository);
    }

    @Test
    void resourceContentMatchesTheThreeStyles() {
        assertTrue(skillService.readContent(1L).contains("未解决的事实"));
        assertTrue(skillService.readContent(2L).contains("共同记忆"));
        assertTrue(skillService.readContent(3L).contains("删去重复解释"));
    }

    @Test
    void unknownOrNullIdRejectsTheWholeReplacement() {
        assertEquals(1003011001, assertThrows(BusinessException.class,
                () -> skillService.replaceAgentBindings(10L, List.of(1L, 99L))).getCode());
        assertEquals(1003011001, assertThrows(BusinessException.class,
                () -> skillService.replaceAgentBindings(10L, Arrays.asList(1L, null))).getCode());
        verifyNoInteractions(skillRepository);
    }

    @Test
    void duplicateIdsAreRemovedBeforeSaving() {
        skillService.replaceAgentBindings(10L, List.of(1L, 3L, 1L));

        verify(skillRepository).replaceBindings(10L, List.of(1L, 3L));
    }

    @Test
    void nullKeepsBindingsAndEmptyClearsBindings() {
        skillService.replaceAgentBindings(10L, null);
        verifyNoInteractions(skillRepository);

        skillService.replaceAgentBindings(10L, List.of());
        verify(skillRepository).replaceBindings(10L, List.of());
    }
}
