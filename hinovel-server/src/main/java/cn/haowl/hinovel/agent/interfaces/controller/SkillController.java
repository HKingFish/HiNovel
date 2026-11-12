package cn.haowl.hinovel.agent.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.agent.application.service.SkillService;
import cn.haowl.hinovel.agent.interfaces.dto.SkillListItemResponse;
import cn.haowl.hinovel.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : haowl
 * @date : 2026/11/12 16:02
 * @desc : 内置 Skill 目录接口
 */
@Tag(name = "Skill 目录")
@RestController
@RequestMapping("/api/agent/skills")
public class SkillController {

    /** 内置 Skill 目录。 */
    @Autowired
    private SkillService skillService;

    /** 查询内置 Skill 元数据。 */
    @Operation(summary = "查询内置 Skill 目录")
    @GetMapping
    public ApiResponse<List<SkillListItemResponse>> list() {
        StpUtil.checkLogin();
        return ApiResponse.success(skillService.listBuiltin().stream()
                .map(skill -> new SkillListItemResponse(skill.getId().intValue(), skill.getName(), skill.getDescription()))
                .toList());
    }
}
