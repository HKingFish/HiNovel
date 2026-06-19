package cn.haowl.hinovel.novel.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.haowl.hinovel.common.response.ApiResponse;
import cn.haowl.hinovel.novel.application.service.NovelCharacterService;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacter;
import cn.haowl.hinovel.novel.domain.entity.NovelCharacterRelation;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterRelationCreateRequest;
import cn.haowl.hinovel.novel.interfaces.dto.CharacterUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小说人物图谱接口
 * 提供人物管理和关系图谱功能
 */
@Tag(name = "人物图谱", description = "小说人物管理和关系图谱")
@RestController
@RequestMapping("/api/novel/character")
@RequiredArgsConstructor
public class NovelCharacterController {

    private final NovelCharacterService characterService;

    // ==================== 人物管理 ====================

    @Operation(summary = "创建人物", description = "为小说创建一个新人物")
    @PostMapping("/characters")
    public ApiResponse<NovelCharacter> createCharacter(@Valid @RequestBody CharacterCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(characterService.createCharacter(userId, request));
    }

    @Operation(summary = "更新人物", description = "更新人物信息和人设")
    @PutMapping("/characters/{characterId}")
    public ApiResponse<NovelCharacter> updateCharacter(
            @PathVariable Long characterId,
            @Valid @RequestBody CharacterUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(characterService.updateCharacter(userId, characterId, request));
    }

    @Operation(summary = "删除人物", description = "删除人物及其所有关系")
    @DeleteMapping("/characters/{characterId}")
    public ApiResponse<Void> deleteCharacter(@PathVariable Long characterId) {
        Long userId = StpUtil.getLoginIdAsLong();
        characterService.deleteCharacter(userId, characterId);
        return ApiResponse.success();
    }

    // ==================== 关系管理 ====================

    @Operation(summary = "创建人物关系", description = "建立两个人物之间的关系")
    @PostMapping("/character-relations")
    public ApiResponse<NovelCharacterRelation> createRelation(@Valid @RequestBody CharacterRelationCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(characterService.createRelation(userId, request));
    }

    @Operation(summary = "删除人物关系", description = "删除指定的人物关系")
    @DeleteMapping("/character-relations/{relationId}")
    public ApiResponse<Void> deleteRelation(@PathVariable Long relationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        characterService.deleteRelation(userId, relationId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取人物关系", description = "获取某个人物的所有关系")
    @GetMapping("/characters/{characterId}/relations")
    public ApiResponse<List<NovelCharacterRelation>> listCharacterRelations(@PathVariable Long characterId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(characterService.listRelationsByCharacter(userId, characterId));
    }

    @Operation(summary = "获取小说关系图谱", description = "获取小说的完整关系图谱数据")
    @GetMapping("/novels/{novelId}/character-graph")
    public ApiResponse<CharacterGraphVO> getCharacterGraph(@PathVariable Long novelId) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<NovelCharacter> characters = characterService.listCharactersByNovel(userId, novelId);
        List<NovelCharacterRelation> relations = characterService.listRelationsByNovel(userId, novelId);

        CharacterGraphVO vo = new CharacterGraphVO();
        vo.setCharacters(characters);
        vo.setRelations(relations);
        return ApiResponse.success(vo);
    }

    // ==================== VO ====================

    @lombok.Data
    public static class CharacterGraphVO {
        private List<NovelCharacter> characters;
        private List<NovelCharacterRelation> relations;
    }
}
