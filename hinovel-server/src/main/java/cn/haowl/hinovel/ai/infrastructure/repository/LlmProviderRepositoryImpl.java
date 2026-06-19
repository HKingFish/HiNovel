package cn.haowl.hinovel.ai.infrastructure.repository;

import cn.haowl.hinovel.ai.domain.entity.LlmProvider;
import cn.haowl.hinovel.ai.domain.repository.LlmProviderRepository;
import cn.haowl.hinovel.ai.infrastructure.mapper.LlmProviderMapper;
import cn.haowl.hinovel.common.constant.CommonConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LLM 提供方仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现 LLM 提供方仓储接口。</p>
 *
 * @Author : haowl
 * @Date : 2026/3/12 23:30
 */
@Repository
@RequiredArgsConstructor
public class LlmProviderRepositoryImpl implements LlmProviderRepository {

    private final LlmProviderMapper llmProviderMapper;

    @Override
    public LlmProvider save(LlmProvider provider) {
        if (provider.getId() == null) {
            llmProviderMapper.insert(provider);
        } else {
            llmProviderMapper.updateById(provider);
        }
        return provider;
    }

    @Override
    public Optional<LlmProvider> findById(Long id) {
        return Optional.ofNullable(llmProviderMapper.selectById(id));
    }

    @Override
    public List<LlmProvider> findActiveProviders() {
        LambdaQueryWrapper<LlmProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmProvider::getIsActive, CommonConstants.ENABLED)
                .orderByAsc(LlmProvider::getCreateTime);
        return llmProviderMapper.selectList(wrapper);
    }

    @Override
    public List<LlmProvider> findActiveProvidersByUserId(Long userId) {
        LambdaQueryWrapper<LlmProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmProvider::getUserId, userId)
                .eq(LlmProvider::getIsActive, CommonConstants.ENABLED)
                .orderByAsc(LlmProvider::getCreateTime);
        return llmProviderMapper.selectList(wrapper);
    }

    @Override
    public List<LlmProvider> findByProviderType(String providerType) {
        LambdaQueryWrapper<LlmProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmProvider::getProviderType, providerType)
                .orderByAsc(LlmProvider::getCreateTime);
        return llmProviderMapper.selectList(wrapper);
    }

    @Override
    public List<LlmProvider> findAll() {
        return llmProviderMapper.selectList(null);
    }

    @Override
    public List<LlmProvider> findAllByUserId(Long userId) {
        LambdaQueryWrapper<LlmProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmProvider::getUserId, userId)
                .orderByAsc(LlmProvider::getCreateTime);
        return llmProviderMapper.selectList(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        llmProviderMapper.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<LlmProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmProvider::getName, name);
        return llmProviderMapper.exists(wrapper);
    }

    @Override
    public boolean existsByNameAndUserId(String name, Long userId) {
        LambdaQueryWrapper<LlmProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmProvider::getName, name)
                .eq(LlmProvider::getUserId, userId);
        return llmProviderMapper.exists(wrapper);
    }
}
