package cn.haowl.hinovel.novel.application.service;

import cn.haowl.hinovel.novel.domain.entity.NovelMemo;
import cn.haowl.hinovel.novel.infrastructure.mapper.NovelMemoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 创作手记应用服务。
 *
 * <p>按小说与用户维度读取、保存创作手记。</p>
 *
 * @author haowl
 * @date 2026-11-12
 * @desc 承接创作手记的读取与保存逻辑，Controller 只做协议适配
 */
@Service
public class NovelMemoService {

    /**
     * 创作手记数据访问。
     */
    @Autowired
    private NovelMemoMapper novelMemoMapper;

    /**
     * 获取指定用户的小说创作手记。
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     * @return 手记，不存在时返回 null
     */
    public NovelMemo getMemo(Long novelId, Long userId) {
        return novelMemoMapper.selectByNovelIdAndUserId(novelId, userId);
    }

    /**
     * 保存创作手记，不存在则创建，存在则更新。
     *
     * @param novelId 小说 ID
     * @param userId  用户 ID
     * @param content 手记内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMemo(Long novelId, Long userId, String content) {
        NovelMemo memo = novelMemoMapper.selectByNovelIdAndUserId(novelId, userId);
        if (memo == null) {
            memo = NovelMemo.builder()
                    .novelId(novelId)
                    .userId(userId)
                    .content(content)
                    .build();
            novelMemoMapper.insert(memo);
        } else {
            memo.setContent(content);
            novelMemoMapper.updateById(memo);
        }
    }
}
