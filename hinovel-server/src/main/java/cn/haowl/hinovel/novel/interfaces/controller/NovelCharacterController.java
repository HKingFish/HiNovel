package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.NovelCharacterService;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterGraphVO;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterRelationCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小说人物图谱接口
 * 提供人物管理和关系图谱功能
 */
@Tag(name = "人物图谱", description = "小说人物管理和关系图谱")
@RestController
@RequestMapping("/api/novel/character")
public class NovelCharacterController {

    /**
     * 人物应用服务。
     */
    @Autowired
    private NovelCharacterService characterService;

    // ==================== 人物管理 ====================

    /**
     * 创建小说人物。
     */
    @Operation(summary = "创建人物", description = "为小说创建一个新人物")
    @PostMapping("/characters")
    public ApiResponse<NovelCharacter> createCharacter(
        @Valid @RequestBody CharacterCreateRequest request) {
        return ApiResponse.success(characterService.createCharacter(StpUtil.getLoginIdAsLong(), request));
    }

    /**
     * 更新小说人物。
     */
    @Operation(summary = "更新人物", description = "更新人物信息和人设")
    @PutMapping("/characters/{characterId}")
    public ApiResponse<NovelCharacter> updateCharacter(
            @PathVariable Long characterId,
            @Valid @RequestBody CharacterUpdateRequest request) {
        return ApiResponse.success(
            characterService.updateCharacter(StpUtil.getLoginIdAsLong(), characterId, request));
    }

    /**
     * 删除小说人物。
     */
    @Operation(summary = "删除人物", description = "删除人物及其所有关系")
    @DeleteMapping("/characters/{characterId}")
    public ApiResponse<Void> deleteCharacter(@PathVariable Long characterId) {
        characterService.deleteCharacter(StpUtil.getLoginIdAsLong(), characterId);
        return ApiResponse.success();
    }

    // ==================== 关系管理 ====================

    /**
     * 创建人物关系。
     */
    @Operation(summary = "创建人物关系", description = "建立两个人物之间的关系")
    @PostMapping("/character-relations")
    public ApiResponse<NovelCharacterRelation> createRelation(
        @Valid @RequestBody CharacterRelationCreateRequest request) {
        return ApiResponse.success(characterService.createRelation(StpUtil.getLoginIdAsLong(), request));
    }

    /**
     * 删除人物关系。
     */
    @Operation(summary = "删除人物关系", description = "删除指定的人物关系")
    @DeleteMapping("/character-relations/{relationId}")
    public ApiResponse<Void> deleteRelation(@PathVariable Long relationId) {
        characterService.deleteRelation(StpUtil.getLoginIdAsLong(), relationId);
        return ApiResponse.success();
    }

    /**
     * 查询人物的出向关系。
     */
    @Operation(summary = "获取人物关系", description = "获取某个人物的所有关系")
    @GetMapping("/characters/{characterId}/relations")
    public ApiResponse<List<NovelCharacterRelation>> listCharacterRelations(@PathVariable Long characterId) {
        return ApiResponse.success(
            characterService.listRelationsByCharacter(StpUtil.getLoginIdAsLong(), characterId));
    }

    /**
     * 查询小说人物及其关系图谱。
     */
    @Operation(summary = "获取小说关系图谱", description = "获取小说的完整关系图谱数据")
    @GetMapping("/novels/{novelId}/character-graph")
    public ApiResponse<CharacterGraphVO> getCharacterGraph(@PathVariable Long novelId) {
        return ApiResponse.success(characterService.getCharacterGraph(StpUtil.getLoginIdAsLong(), novelId));
    }
}
