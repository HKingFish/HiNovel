package cn.haowl.hinovel.agent.interfaces.controller;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.spring.SaTokenContextForSpringInJakartaServlet;
import cn.haowl.hinovel.agent.application.service.agent.QaAgent;
import cn.haowl.hinovel.common.config.SaTokenConfig;
import cn.haowl.hinovel.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author : haowl
 * @date : 2026/11/12 16:13
 * @desc : Agent Skill HTTP 鉴权和聊天请求校验
 */
class AgentSkillHttpTest {

    @Test
    void unauthenticatedCatalogueGetsTheActualHttpAndBusinessCodes() throws Exception {
        SaTokenContext previous = SaManager.getSaTokenContext();
        try {
            SaManager.setSaTokenContext(new SaTokenContextForSpringInJakartaServlet());
            MockMvcBuilders.standaloneSetup(new SkillController())
                    .addFilters(new SaTokenConfig().saServletFilter(new ObjectMapper()))
                    .build()
                    .perform(get("/api/agent/skills"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value(1000010001));
        } finally {
            SaManager.setSaTokenContext(previous);
        }
    }

    @Test
    void blankChatQuestionIsRejectedBeforeExecution() throws Exception {
        QaAgent qa = mock(QaAgent.class);
        AIController controller = new AIController();
        ReflectionTestUtils.setField(controller, "qaAgentService", qa);
        MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
                .perform(post("/api/agent/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novelId\":10,\"sessionId\":20,\"question\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(qa);
    }
}
