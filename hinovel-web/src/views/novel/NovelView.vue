<template>
    <div class="novel-editor-page">
        <!-- 顶部标题栏 -->
        <header class="editor-header">
            <div class="header-left">
                <el-button :icon="ArrowLeft" type="primary" plain class="back-btn" @click="goBack"> 返回 </el-button>
                <el-input v-model="chapterTitle" placeholder="请输入章节标题" class="chapter-title-input" />
            </div>
            <div class="header-center">
                <span class="novel-title-badge">{{ novelDisplayTitle || '加载中...' }}</span>
                <span class="novel-word-count-badge">
                    <el-icon :size="13"><EditPen /></el-icon>
                    总字数：{{ formatWordCount(novelTotalWordCount) }}
                </span>
                <span class="novel-chapter-count-badge">
                    <el-icon :size="13"><Document /></el-icon>
                    共 {{ chapterTotal }} 章
                </span>
            </div>
            <div class="header-right">
                <el-button
                    :type="isFocusMode ? 'warning' : 'default'"
                    :icon="isFocusMode ? Close : FullScreen"
                    class="focus-mode-btn"
                    @click="toggleFocusMode"
                >
                    {{ isFocusMode ? '退出专注' : '专注模式' }}
                </el-button>
                <el-button type="primary" :icon="Upload" @click="publishChapter"> 发布 </el-button>
            </div>
        </header>

        <!-- 编辑器主体 -->
        <div class="editor-body">
            <!-- 左侧边栏 - 章节列表（支持收起/展开） -->
            <aside class="editor-sidebar" :class="{ collapsed: isSidebarCollapsed }">
                <div class="sidebar-main">
                    <div class="sidebar-header">
                        <template v-if="!isSidebarCollapsed">
                            <span class="sidebar-title">
                                <el-icon><List /></el-icon>
                                章节菜单
                            </span>
                            <div class="sidebar-header-actions">
                                <el-tooltip content="一键重排章节号" placement="top" :show-after="300">
                                    <el-button :icon="Sort" text size="small" circle @click="handleReorderChapters" />
                                </el-tooltip>
                                <el-button :icon="Plus" text size="small" circle @click="openCreateChapterDialog" />
                                <el-button
                                    :icon="DArrowLeft"
                                    text
                                    size="small"
                                    circle
                                    @click="isSidebarCollapsed = true"
                                />
                            </div>
                        </template>
                        <template v-else>
                            <div class="sidebar-collapsed-content" @click="isSidebarCollapsed = false">
                                <!-- 展开箭头 -->
                                <el-tooltip content="展开章节菜单" placement="right" :show-after="300">
                                    <div class="sidebar-expand-btn">
                                        <el-icon :size="16"><DArrowRight /></el-icon>
                                    </div>
                                </el-tooltip>
                                <!-- 当前章节竖排信息 -->
                                <template v-if="currentChapterInfo">
                                    <div class="collapsed-chapter-info">
                                        <!-- 发布状态指示点 -->
                                        <span
                                            class="collapsed-status-dot"
                                            :class="
                                                currentChapterInfo.status === 'PUBLISHED'
                                                    ? 'dot-published'
                                                    : currentChapterInfo.needRepublish
                                                      ? 'dot-need-republish'
                                                      : 'dot-draft'
                                            "
                                        ></span>
                                        <!-- 章节序号（竖排） -->
                                        <span class="collapsed-chapter-number"
                                            >第{{ currentChapterInfo.chapterNumber }}章</span
                                        >
                                        <!-- 字数（竖排） -->
                                        <span class="collapsed-word-count">{{
                                            formatWordCount(currentChapterInfo.wordCount)
                                        }}</span>
                                    </div>
                                </template>
                                <!-- 总章节数 -->
                                <div class="collapsed-total">
                                    <span class="collapsed-total-num">{{ chapterTotal }}</span>
                                    <span class="collapsed-total-label">章</span>
                                </div>
                            </div>
                        </template>
                    </div>

                    <div
                        v-show="!isSidebarCollapsed"
                        ref="chapterListRef"
                        class="sidebar-content"
                        @scroll="handleChapterListScroll"
                    >
                        <!-- 章节筛选工具栏 -->
                        <div class="chapter-toolbar">
                            <div class="toolbar-row">
                                <el-input
                                    v-model="chapterSearchKeyword"
                                    placeholder="搜索章节标题..."
                                    size="small"
                                    clearable
                                    class="search-input"
                                >
                                    <template #prefix>
                                        <el-icon>
                                            <Search />
                                        </el-icon>
                                    </template>
                                </el-input>
                            </div>
                            <div class="toolbar-row">
                                <el-select
                                    v-model="chapterFilterStatus"
                                    size="small"
                                    placeholder="发布状态"
                                    clearable
                                    class="filter-select"
                                    @clear="chapterFilterStatus = undefined"
                                >
                                    <el-option label="草稿" :value="0" />
                                    <el-option label="已发布" :value="1" />
                                </el-select>
                                <el-select
                                    v-model="chapterFilterVector"
                                    size="small"
                                    placeholder="入库状态"
                                    clearable
                                    class="filter-select"
                                    @clear="chapterFilterVector = undefined"
                                >
                                    <el-option label="未入库" :value="0" />
                                    <el-option label="已入库" :value="1" />
                                </el-select>
                            </div>
                        </div>
                        <div class="list-summary">
                            共 {{ chapterTotal }} 章
                            <span v-if="isLoadingMoreChapters" class="loading-more">加载中...</span>
                        </div>
                        <draggable
                            v-model="chapters"
                            item-key="id"
                            handle=".drag-handle"
                            ghost-class="chapter-drag-ghost"
                            chosen-class="chapter-drag-chosen"
                            animation="200"
                            @end="handleDragEnd"
                        >
                            <template #item="{ element: chapter }">
                                <div
                                    class="chapter-item"
                                    :class="{ active: currentChapterId === chapter.id }"
                                    @click="selectChapter(chapter.id)"
                                >
                                    <div class="chapter-content">
                                        <div class="chapter-header-row">
                                            <div class="chapter-title-section">
                                                <el-icon class="drag-handle" :size="14"><Rank /></el-icon>
                                                <span class="chapter-number">第{{ chapter.chapterNumber }}章</span>
                                            </div>
                                            <div class="chapter-actions">
                                                <el-button
                                                    :icon="EditPen"
                                                    text
                                                    circle
                                                    size="small"
                                                    @click.stop="openEditChapterDialog(chapter)"
                                                />
                                                <el-button
                                                    :icon="Delete"
                                                    text
                                                    circle
                                                    size="small"
                                                    @click.stop="confirmDeleteChapter(chapter)"
                                                />
                                            </div>
                                        </div>
                                        <div class="chapter-name">{{ chapter.title || '未命名章节' }}</div>
                                        <!-- 状态标志行 -->
                                        <div class="chapter-status-row">
                                            <!-- 发布状态标志 -->
                                            <el-tooltip
                                                :content="getPublishStatusTooltip(chapter)"
                                                placement="top"
                                                :show-after="300"
                                            >
                                                <span class="status-badge" :class="getPublishStatusClass(chapter)">
                                                    <el-icon :size="11">
                                                        <component :is="getPublishStatusIcon(chapter)" />
                                                    </el-icon>
                                                    <span class="status-text">{{
                                                        getPublishStatusLabel(chapter)
                                                    }}</span>
                                                </span>
                                            </el-tooltip>
                                            <!-- 向量存储状态标志 -->
                                            <el-tooltip
                                                :content="
                                                    chapter.vectorStored
                                                        ? '点击重新同步到向量数据库'
                                                        : '点击同步到向量数据库'
                                                "
                                                placement="top"
                                                :show-after="300"
                                            >
                                                <span
                                                    class="status-badge"
                                                    :class="[
                                                        chapter.vectorStored
                                                            ? 'status-vector-done'
                                                            : 'status-vector-none',
                                                        'status-clickable',
                                                    ]"
                                                    @click.stop="syncChapterVector(chapter)"
                                                >
                                                    <el-icon :size="11"><Connection /></el-icon>
                                                    <span class="status-text">{{
                                                        chapter.vectorStored ? '已入库' : '未入库'
                                                    }}</span>
                                                </span>
                                            </el-tooltip>
                                        </div>
                                        <div class="chapter-meta">
                                            <div class="meta-row">
                                                <el-icon :size="10"><Clock /></el-icon>
                                                <span class="meta-label">创建</span>
                                                <span class="meta-time">{{ formatTime(chapter.createTime) }}</span>
                                            </div>
                                            <div class="meta-row">
                                                <el-icon :size="10"><EditPen /></el-icon>
                                                <span class="meta-label">修改</span>
                                                <span class="meta-time">{{ formatTime(chapter.updateTime) }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </draggable>
                    </div>
                </div>
                <!-- 右侧垂直快速定位导航条 -->
                <div v-if="!isSidebarCollapsed && quickJumpAnchors.length > 0" class="quick-jump-rail">
                    <span class="quick-jump-arrow" @click="scrollJumpRail(-1)">
                        <el-icon :size="10"><ArrowUp /></el-icon>
                    </span>
                    <div ref="quickJumpListRef" class="quick-jump-list">
                        <span
                            v-for="anchor in quickJumpAnchors"
                            :key="anchor"
                            class="quick-jump-rail-btn"
                            :class="{ active: activeJumpAnchor === anchor }"
                            @click="jumpToChapter(anchor)"
                            >{{ anchor }}</span
                        >
                    </div>
                    <span class="quick-jump-arrow" @click="scrollJumpRail(1)">
                        <el-icon :size="10"><ArrowDown /></el-icon>
                    </span>
                </div>
            </aside>

            <!-- 中间大纲面板 -->
            <div v-show="!isFocusMode" class="outline-panel" :style="{ width: outlinePanelWidth + 'px' }">
                <div class="outline-header">
                    <el-icon><Document /></el-icon>
                    <span>本章大纲</span>
                </div>

                <div class="outline-body">
                    <!-- 核心区域：大纲文本输入框 -->
                    <div class="panel-section">
                        <el-input
                            v-model="chapterOutline"
                            type="textarea"
                            :rows="15"
                            placeholder="编写本章大纲，梳理剧情思路..."
                            class="outline-textarea resizable-textarea"
                        />
                    </div>

                    <!-- 剧情要点（始终展开） -->
                    <div class="panel-section">
                        <div class="section-label">
                            <el-icon><Flag /></el-icon>
                            剧情要点
                        </div>
                        <el-input
                            v-model="plotPointsText"
                            type="textarea"
                            :rows="6"
                            placeholder="本章的关键剧情要点..."
                            class="outline-textarea resizable-textarea"
                        />
                    </div>

                    <!-- 补充要求 -->
                    <div class="panel-section">
                        <div class="section-label">
                            <el-icon>
                                <ChatDotRound />
                            </el-icon>
                            补充要求
                        </div>
                        <el-input
                            v-model="userRequirement"
                            type="textarea"
                            :rows="4"
                            placeholder="对改写结果不满意？在此补充你的具体要求..."
                            class="outline-textarea"
                        />
                    </div>
                </div>

                <!-- AI 改写按钮固定在底部，不随内容滚动 -->
                <div class="ai-section-fixed">
                    <el-button
                        v-if="!isAiProcessing || aiProcessingType !== 'rewrite'"
                        type="primary"
                        :icon="MagicStick"
                        class="ai-continue-btn"
                        @click="aiRewrite"
                    >
                        <el-icon class="ai-icon">
                            <Cpu />
                        </el-icon>
                        AI 改写
                    </el-button>
                    <el-button v-else type="danger" class="ai-continue-btn" @click="cancelRewrite">
                        <el-icon class="ai-icon">
                            <CircleClose />
                        </el-icon>
                        取消改写
                    </el-button>
                </div>
            </div>

            <!-- 大纲面板与正文面板之间的拖拽手柄 -->
            <div v-show="!isFocusMode" class="panel-divider-handle" @mousedown="startResizeOutline"></div>

            <!-- 中间内容编辑区 -->
            <div class="content-panel">
                <div class="content-header">
                    <span class="content-title">
                        <el-icon><EditPen /></el-icon>
                        正文内容
                    </span>
                    <div class="content-actions">
                        <span class="word-count">字数：{{ wordCount }}</span>
                        <el-tooltip content="选择背景色" placement="bottom" :show-after="300">
                            <div class="bg-color-picker">
                                <div
                                    class="bg-color-btn"
                                    :style="{ backgroundColor: editorBgColor }"
                                    @click="showBgColorPicker = !showBgColorPicker"
                                >
                                    <el-icon :size="14">
                                        <Palette />
                                    </el-icon>
                                </div>
                                <div v-if="showBgColorPicker" class="bg-color-options">
                                    <div
                                        v-for="color in bgColorOptions"
                                        :key="color.value"
                                        class="bg-color-option"
                                        :class="{ active: editorBgColor === color.value }"
                                        :style="{ backgroundColor: color.value }"
                                        @click="selectBgColor(color.value)"
                                    >
                                        <span v-if="editorBgColor === color.value" class="check-icon">✓</span>
                                    </div>
                                </div>
                            </div>
                        </el-tooltip>
                        <el-tooltip content="清除多余空白行" placement="bottom" :show-after="300">
                            <el-button size="small" text class="clean-blank-btn" @click="cleanBlankLines">
                                <el-icon :size="14">
                                    <Delete />
                                </el-icon>
                                清除空行
                            </el-button>
                        </el-tooltip>
                        <el-button
                            size="default"
                            :icon="hasUnsavedChanges ? Warning : Check"
                            :loading="isSaving"
                            :class="[
                                'save-btn',
                                hasUnsavedChanges ? 'save-btn-unsaved' : 'save-btn-saved',
                                { 'save-btn-pulse': hasUnsavedChanges },
                            ]"
                            @click="manualSave"
                        >
                            {{ hasUnsavedChanges ? '未保存' : '已保存' }}
                        </el-button>
                    </div>
                </div>

                <div class="content-body">
                    <TiptapEditor
                        ref="contentEditorRef"
                        v-model="chapterContent"
                        placeholder="开始你的创作之旅..."
                        :annotations="chapterAnnotations"
                        :background-color="editorBgColor"
                        @blur="handleEditorBlur"
                        @stats-change="handleStatsChange"
                        @create-annotation="handleCreateAnnotation"
                        @delete-annotation="handleDeleteAnnotation"
                        @ai-rewrite-annotation="handleAiRewriteAnnotation"
                        @accept-rewrite="handleAcceptRewrite"
                        @view-annotation="handleViewAnnotation"
                        @quick-rewrite="handleQuickRewrite"
                        @accept-quick-rewrite="handleAcceptQuickRewrite"
                    />
                </div>

                <div class="content-footer">
                    <div class="footer-left">
                        <el-icon><Document /></el-icon>
                        <span>字数 {{ wordCount }}</span>
                        <el-divider direction="vertical" />
                        <span>行数 {{ lineCount }}</span>
                        <el-divider direction="vertical" />
                        <span>段落 {{ paragraphCount }}</span>
                    </div>
                    <div class="footer-right">
                        <el-button size="small" class="footer-action-btn" @click="copyChapterContent">
                            <el-icon :size="13">
                                <DocumentCopy />
                            </el-icon>
                            复制
                        </el-button>
                        <el-button size="small" class="footer-action-btn" @click="exportChapterContent">
                            <el-icon :size="13">
                                <Download />
                            </el-icon>
                            导出
                        </el-button>
                    </div>
                </div>
            </div>

            <!-- 正文面板与功能区之间的拖拽手柄 -->
            <div v-show="activeFunctionTab" class="panel-divider-handle" @mousedown="startResizeFunctionPanel"></div>

            <!-- 右侧功能区（活动栏 + 内容面板） -->
            <div class="function-area">
                <!-- 内容面板（点击图标后展开，宽度可拖拽） -->
                <aside v-show="activeFunctionTab" class="function-panel" :style="{ width: functionPanelWidth + 'px' }">
                    <div class="panel-title-bar">
                        <span class="panel-title-text">{{ functionTabTitle }}</span>
                    </div>

                    <!-- 小说大纲面板 -->
                    <div v-show="activeFunctionTab === 'outline'" class="function-tab-content">
                        <div v-loading="isLoadingOutline" class="tab-panel-body">
                            <!-- 展示模式：Markdown 渲染 -->
                            <div v-if="!isEditingNovelOutline" class="novel-outline-display">
                                <div
                                    v-if="novelOutline"
                                    class="markdown-rendered-content"
                                    v-html="renderedNovelOutline"
                                ></div>
                                <div v-else class="outline-empty-hint">
                                    <el-icon>
                                        <Document />
                                    </el-icon>
                                    <span>暂无大纲内容，点击编辑添加</span>
                                </div>
                            </div>
                            <!-- 编辑模式：文本输入框 -->
                            <el-input
                                v-else
                                v-model="editingNovelOutlineContent"
                                type="textarea"
                                :rows="25"
                                placeholder="编写小说整体大纲，梳理故事主线、人物成长弧线、关键剧情节点...&#10;&#10;支持 Markdown 格式：&#10;# 一级标题&#10;## 二级标题&#10;- 列表项&#10;**加粗** *斜体*"
                                class="outline-textarea resizable-textarea"
                            />
                        </div>
                        <div class="tab-panel-footer">
                            <template v-if="!isEditingNovelOutline">
                                <el-button type="primary" size="small" @click="startEditNovelOutline">
                                    <el-icon>
                                        <EditPen />
                                    </el-icon>
                                    编辑大纲
                                </el-button>
                            </template>
                            <template v-else>
                                <el-button size="small" @click="cancelEditNovelOutline">取消</el-button>
                                <el-button type="primary" size="small" @click="saveNovelOutlineHandler">
                                    保存大纲
                                </el-button>
                            </template>
                        </div>
                    </div>

                    <!-- 人物图谱面板 -->
                    <div v-show="activeFunctionTab === 'character'" class="function-tab-content">
                        <div v-loading="isLoadingCharacterGraph" class="tab-panel-body character-graph-body">
                            <!-- 人物详情内联卡片 -->
                            <transition name="slide-up">
                                <div v-if="showCharacterDetail && selectedCharacter" class="character-inline-detail">
                                    <div class="inline-detail-header">
                                        <div class="inline-detail-avatar">
                                            <el-avatar
                                                :size="44"
                                                :src="selectedCharacter.avatarUrl || undefined"
                                                :style="{
                                                    background: selectedCharacter.color || '#6366f1',
                                                    fontSize: '18px',
                                                }"
                                            >
                                                {{ selectedCharacter.name.charAt(0) }}
                                            </el-avatar>
                                            <div class="inline-detail-name">
                                                <span class="detail-name-text">{{ selectedCharacter.name }}</span>
                                                <el-tag
                                                    :type="
                                                        selectedCharacter.roleType === 'PROTAGONIST'
                                                            ? 'danger'
                                                            : selectedCharacter.roleType === 'ANTAGONIST'
                                                              ? 'warning'
                                                              : 'info'
                                                    "
                                                    size="small"
                                                >
                                                    {{ getRoleLabel(selectedCharacter.roleType) }}
                                                </el-tag>
                                            </div>
                                        </div>
                                        <el-button
                                            text
                                            circle
                                            size="small"
                                            :icon="Close"
                                            @click="showCharacterDetail = false"
                                        />
                                    </div>
                                    <div class="inline-detail-body">
                                        <!-- 基本信息 -->
                                        <div class="inline-info-grid">
                                            <div v-if="selectedCharacter.gender" class="inline-info-item">
                                                <span class="inline-info-label">性别</span>
                                                <span class="inline-info-value">{{
                                                    genderMap[selectedCharacter.gender] || selectedCharacter.gender
                                                }}</span>
                                            </div>
                                            <div v-if="selectedCharacter.age" class="inline-info-item">
                                                <span class="inline-info-label">年龄</span>
                                                <span class="inline-info-value">{{ selectedCharacter.age }}</span>
                                            </div>
                                            <div v-if="selectedCharacter.identity" class="inline-info-item">
                                                <span class="inline-info-label">身份</span>
                                                <span class="inline-info-value">{{ selectedCharacter.identity }}</span>
                                            </div>
                                            <div v-if="selectedCharacter.alias" class="inline-info-item">
                                                <span class="inline-info-label">别名</span>
                                                <span class="inline-info-value">{{ selectedCharacter.alias }}</span>
                                            </div>
                                        </div>

                                        <!-- 性格特点 -->
                                        <div v-if="selectedCharacter.personality" class="inline-section">
                                            <div class="inline-section-title">性格特点</div>
                                            <div class="inline-section-text">{{ selectedCharacter.personality }}</div>
                                        </div>

                                        <!-- 背景故事 -->
                                        <div v-if="selectedCharacter.background" class="inline-section">
                                            <div class="inline-section-title">背景故事</div>
                                            <div class="inline-section-text">{{ selectedCharacter.background }}</div>
                                        </div>

                                        <!-- 目标/动机 -->
                                        <div v-if="selectedCharacter.goals" class="inline-section">
                                            <div class="inline-section-title">目标/动机</div>
                                            <div class="inline-section-text">{{ selectedCharacter.goals }}</div>
                                        </div>

                                        <!-- 能力/技能 -->
                                        <div v-if="selectedCharacter.abilities" class="inline-section">
                                            <div class="inline-section-title">能力/技能</div>
                                            <div class="inline-section-text">{{ selectedCharacter.abilities }}</div>
                                        </div>

                                        <!-- 人物关系 -->
                                        <div v-if="selectedCharacterRelations.length > 0" class="inline-relations">
                                            <div class="inline-relations-title">人物关系</div>
                                            <div class="inline-relations-list">
                                                <div
                                                    v-for="rel in selectedCharacterRelations"
                                                    :key="rel.id"
                                                    class="inline-relation-chip"
                                                >
                                                    <el-tag size="small" effect="plain" round>{{
                                                        rel.relationType
                                                    }}</el-tag>
                                                    <span class="inline-relation-name">
                                                        {{
                                                            characterData.find(
                                                                (c) =>
                                                                    c.id ===
                                                                    (rel.characterId === selectedCharacter?.id
                                                                        ? rel.targetId
                                                                        : rel.characterId),
                                                            )?.name || '未知人物'
                                                        }}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- 备注 -->
                                        <div v-if="selectedCharacter.notes" class="inline-section">
                                            <div class="inline-section-title">备注</div>
                                            <div class="inline-section-text">{{ selectedCharacter.notes }}</div>
                                        </div>

                                        <!-- 无任何详细信息时的提示 -->
                                        <div
                                            v-if="
                                                !selectedCharacter.gender &&
                                                !selectedCharacter.age &&
                                                !selectedCharacter.identity &&
                                                !selectedCharacter.personality &&
                                                !selectedCharacter.background &&
                                                selectedCharacterRelations.length === 0
                                            "
                                            class="inline-no-relation"
                                        >
                                            暂无详细信息，可前往人物图谱页面编辑
                                        </div>

                                        <!-- 前往编辑按钮 -->
                                        <div class="inline-detail-footer">
                                            <el-button
                                                size="small"
                                                :icon="EditPen"
                                                class="goto-edit-btn"
                                                @click="router.push(`/novel/${novelId}/characters`)"
                                            >
                                                前往编辑
                                            </el-button>
                                        </div>
                                    </div>
                                </div>
                            </transition>
                            <div ref="miniGraphRef" class="inline-graph-container"></div>
                        </div>
                    </div>

                    <!-- 历史版本面板 -->
                    <div v-show="activeFunctionTab === 'history'" class="function-tab-content">
                        <div v-loading="isLoadingVersions" class="tab-panel-body history-panel-body">
                            <div v-if="historyVersions.length === 0" class="empty-tip">
                                <el-empty description="暂无历史版本" :image-size="60" />
                            </div>
                            <template v-else>
                                <div class="inline-history-list">
                                    <div
                                        v-for="version in historyVersions"
                                        :key="version.id"
                                        class="inline-history-item"
                                        :class="{ active: previewVersionId === version.id }"
                                        @click="previewVersion(version)"
                                    >
                                        <div class="version-info">
                                            <div class="version-left">
                                                <el-icon :size="12">
                                                    <Clock />
                                                </el-icon>
                                                <span class="version-time">{{ formatTime(version.createTime) }}</span>
                                                <!-- 发布标记 -->
                                                <el-tag
                                                    v-if="version.published === 1"
                                                    size="small"
                                                    type="success"
                                                    class="published-tag"
                                                >
                                                    已发布
                                                </el-tag>
                                            </div>
                                            <span class="version-word">{{ version.wordCount }} 字</span>
                                        </div>
                                        <!-- 版本备注 -->
                                        <div v-if="version.remark" class="version-remark">
                                            {{ version.remark }}
                                        </div>
                                        <div class="version-actions">
                                            <el-button
                                                class="restore-btn"
                                                size="small"
                                                @click.stop="restoreVersion(version.id)"
                                            >
                                                恢复
                                            </el-button>
                                            <el-button
                                                text
                                                size="small"
                                                :icon="EditPen"
                                                @click.stop="openRemarkDialog(version)"
                                            >
                                                备注
                                            </el-button>
                                        </div>
                                    </div>
                                </div>
                                <!-- 版本预览 -->
                                <div v-if="previewVersionId" class="inline-version-preview">
                                    <div class="preview-header">
                                        <span>版本预览</span>
                                        <el-button
                                            text
                                            size="small"
                                            @click="
                                                previewVersionId = null
                                                previewVersionContent = ''
                                            "
                                        >
                                            关闭
                                        </el-button>
                                    </div>
                                    <div class="preview-content">
                                        <pre>{{ previewVersionContent }}</pre>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>

                    <!-- AI 问答面板 -->
                    <div v-show="activeFunctionTab === 'chat'" class="function-tab-content">
                        <!-- 会话管理栏 -->
                        <div class="chat-session-bar">
                            <el-select
                                v-model="chatSessionId"
                                placeholder="选择会话"
                                size="small"
                                class="session-select"
                                @change="handleSwitchSession"
                            >
                                <el-option
                                    v-for="session in sortedChatSessions"
                                    :key="session.id"
                                    :label="session.title"
                                    :value="session.id"
                                >
                                    <div class="session-option-item">
                                        <el-icon
                                            v-if="session.favorite === 1"
                                            :size="12"
                                            color="#f59e0b"
                                            class="session-star-icon"
                                        >
                                            <Star />
                                        </el-icon>
                                        <span class="session-option-title">{{ session.title }}</span>
                                    </div>
                                </el-option>
                            </el-select>
                            <el-tooltip content="收藏/取消收藏" placement="bottom" :show-after="300">
                                <el-button
                                    :icon="Star"
                                    text
                                    size="small"
                                    circle
                                    :class="{ 'is-favorited': currentSessionFavorite }"
                                    :disabled="!chatSessionId"
                                    @click="handleToggleFavorite"
                                />
                            </el-tooltip>
                            <el-tooltip content="重命名会话" placement="bottom" :show-after="300">
                                <el-button
                                    :icon="EditPen"
                                    text
                                    size="small"
                                    circle
                                    :disabled="!chatSessionId"
                                    @click="handleRenameSession"
                                />
                            </el-tooltip>
                            <el-tooltip content="新建会话" placement="bottom" :show-after="300">
                                <el-button :icon="Plus" text size="small" circle @click="handleCreateSession" />
                            </el-tooltip>
                            <el-tooltip content="删除当前会话" placement="bottom" :show-after="300">
                                <el-button
                                    :icon="Delete"
                                    text
                                    size="small"
                                    circle
                                    :disabled="!chatSessionId || chatSessions.length <= 1"
                                    @click="handleDeleteSession"
                                />
                            </el-tooltip>
                        </div>
                        <div class="tab-panel-body chat-panel-body">
                            <div class="chat-messages-wrapper">
                                <div ref="chatMessagesRef" class="inline-chat-messages" @scroll="handleChatScroll">
                                    <!-- 加载更多历史消息提示 -->
                                    <div v-if="chatLoadingMore" class="chat-loading-more">加载中...</div>
                                    <div
                                        v-else-if="!chatHasMore && chatMessages.length > CHAT_PAGE_SIZE"
                                        class="chat-no-more"
                                    >
                                        已加载全部消息
                                    </div>
                                    <div
                                        v-for="(msg, index) in chatMessages"
                                        :key="index"
                                        class="chat-message"
                                        :class="[msg.role, { 'is-revoked': msg.revoked === 1 }]"
                                    >
                                        <!-- 撤回消息：微信风格居中提示 -->
                                        <div v-if="msg.revoked === 1" class="revoked-notice">
                                            {{ msg.role === 'user' ? '你' : 'AI' }}撤回了一条消息
                                        </div>
                                        <!-- 正常消息 -->
                                        <template v-else>
                                            <div class="message-avatar">
                                                <el-avatar
                                                    :size="28"
                                                    :style="{
                                                        background: msg.role === 'user' ? '#1a73e8' : '#667eea',
                                                        fontSize: '12px',
                                                    }"
                                                >
                                                    {{ msg.role === 'user' ? '我' : 'AI' }}
                                                </el-avatar>
                                            </div>
                                            <div class="message-content">
                                                <!-- 用户消息：纯文本展示 -->
                                                <div v-if="msg.role === 'user'" class="message-text">
                                                    {{ msg.content }}
                                                </div>
                                                <!-- AI 回复：Markdown 渲染 -->
                                                <div
                                                    v-else
                                                    class="message-text ai-markdown-content"
                                                    v-html="renderMarkdown(msg.content)"
                                                ></div>
                                                <!-- 消息操作按钮 -->
                                                <div v-if="msg.content" class="message-actions">
                                                    <el-tooltip
                                                        v-if="msg.role === 'assistant'"
                                                        content="复制回答"
                                                        placement="bottom"
                                                        :show-after="300"
                                                    >
                                                        <el-button
                                                            text
                                                            size="small"
                                                            class="copy-message-btn"
                                                            @click="copyMessageContent(msg.content)"
                                                        >
                                                            <el-icon :size="13">
                                                                <DocumentCopy />
                                                            </el-icon>
                                                            <span class="copy-btn-text">复制</span>
                                                        </el-button>
                                                    </el-tooltip>
                                                    <el-button
                                                        v-if="msg.id"
                                                        text
                                                        size="small"
                                                        class="revoke-message-btn"
                                                        @click="handleRevokeMessage(msg, index)"
                                                    >
                                                        <el-icon :size="13"><RefreshLeft /></el-icon>
                                                        <span class="copy-btn-text">撤回</span>
                                                    </el-button>
                                                </div>
                                            </div>
                                        </template>
                                    </div>
                                </div>
                                <!-- 一键到底浮动按钮 -->
                                <transition name="fade-slide">
                                    <div
                                        v-show="showScrollToBottomBtn"
                                        class="scroll-to-bottom-btn"
                                        @click="scrollToBottom(true)"
                                    >
                                        <el-icon :size="16">
                                            <ArrowDown />
                                        </el-icon>
                                    </div>
                                </transition>
                            </div>
                        </div>
                        <!-- AI 思考/工具执行状态提示（固定在输入框上方） -->
                        <transition name="fade-slide">
                            <div v-if="isChatLoading" class="chat-thinking-bar">
                                <span class="thinking-dot-anim"> <span></span><span></span><span></span> </span>
                                <span class="thinking-text">{{ chatThinkingText }}</span>
                            </div>
                        </transition>
                        <div class="chat-input-area">
                            <el-input
                                ref="chatInputRef"
                                v-model="chatInput"
                                type="textarea"
                                :rows="10"
                                placeholder="询问关于小说的问题... (Enter发送, Shift+Enter换行)"
                                class="chat-input"
                                :disabled="isChatLoading"
                                @keydown.enter.prevent="handleChatInputEnter"
                            />
                            <div class="chat-action-buttons">
                                <el-button
                                    v-if="isChatLoading"
                                    type="danger"
                                    :icon="CircleClose"
                                    size="default"
                                    class="stop-btn"
                                    @click="handleStopChat"
                                >
                                    停止
                                </el-button>
                                <el-button
                                    v-else
                                    type="primary"
                                    :icon="Promotion"
                                    size="default"
                                    :disabled="!chatInput.trim()"
                                    class="send-btn"
                                    @click="sendChatMessage"
                                >
                                    发送
                                </el-button>
                            </div>
                        </div>
                    </div>

                    <!-- 批注列表面板 -->
                    <div v-show="activeFunctionTab === 'annotations'" class="function-tab-content">
                        <div class="tab-panel-body annotation-list-body">
                            <div v-if="chapterAnnotations.length === 0" class="empty-tip">
                                <el-empty description="暂无批注" :image-size="60" />
                            </div>
                            <div v-else class="annotation-list">
                                <div
                                    v-for="annotation in sortedAnnotations"
                                    :key="annotation.id"
                                    class="annotation-list-item"
                                    :class="{
                                        'is-unread': !isAnnotationRead(annotation.id),
                                        'is-ignored': annotation.status === 'RESOLVED',
                                        'is-accepted': annotation.status === 'ACCEPTED',
                                    }"
                                    @click="handleAnnotationListClick(annotation)"
                                >
                                    <!-- 顶部行：作者 + 时间 + 状态标签（右上角） -->
                                    <div class="annotation-list-header">
                                        <div class="annotation-list-header-left">
                                            <span class="annotation-list-author">
                                                {{ annotation.annotationType === 'SELF' ? '作者' : '审阅者' }}
                                            </span>
                                            <span class="annotation-list-time">
                                                {{ formatAnnotationListTime(annotation.createTime) }}
                                            </span>
                                            <span v-if="!isAnnotationRead(annotation.id)" class="unread-dot"></span>
                                        </div>
                                        <!-- 状态标签（右上角展示） -->
                                        <span
                                            class="annotation-status-tag"
                                            :class="'status-' + annotation.status.toLowerCase()"
                                        >
                                            {{ annotationStatusLabel(annotation.status) }}
                                        </span>
                                    </div>
                                    <div class="annotation-list-original">
                                        "{{ truncateText(annotation.originalText, 40) }}"
                                    </div>
                                    <div class="annotation-list-content">
                                        {{ truncateText(annotation.annotationContent, 60) }}
                                    </div>
                                    <!-- 操作按钮（仅待处理状态显示） -->
                                    <div v-if="annotation.status === 'PENDING'" class="annotation-list-actions">
                                        <button class="adopt-btn" @click.stop="handleAdoptAnnotation(annotation)">
                                            采纳
                                        </button>
                                        <button class="ignore-btn" @click.stop="handleResolveAnnotation(annotation.id)">
                                            忽略
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 创作手记面板 -->
                    <div v-show="activeFunctionTab === 'memo'" class="function-tab-content">
                        <div v-loading="isLoadingMemo" class="tab-panel-body">
                            <!-- 展示模式：Markdown 渲染 -->
                            <div v-if="!isEditingMemo" class="novel-outline-display">
                                <div
                                    v-if="memoContent"
                                    class="markdown-rendered-content"
                                    v-html="renderedMemoContent"
                                ></div>
                                <div v-else class="outline-empty-hint">
                                    <el-icon>
                                        <Notebook />
                                    </el-icon>
                                    <span>暂无手记，点击编辑记录创作灵感</span>
                                </div>
                            </div>
                            <!-- 编辑模式 -->
                            <el-input
                                v-else
                                v-model="editingMemoContent"
                                type="textarea"
                                :rows="35"
                                placeholder="记录创作灵感、人物设定备忘、剧情构思...&#10;&#10;支持 Markdown 格式"
                                class="outline-textarea resizable-textarea"
                            />
                        </div>
                        <div class="tab-panel-footer">
                            <template v-if="!isEditingMemo">
                                <el-button type="primary" size="small" @click="startEditMemo">
                                    <el-icon>
                                        <EditPen />
                                    </el-icon>
                                    编辑手记
                                </el-button>
                            </template>
                            <template v-else>
                                <el-button size="small" @click="cancelEditMemo">取消</el-button>
                                <el-button type="primary" size="small" :loading="isSavingMemo" @click="saveMemoHandler">
                                    保存手记
                                </el-button>
                            </template>
                        </div>
                    </div>

                    <!-- 小说设置面板 -->
                    <div v-show="activeFunctionTab === 'settings'" class="function-tab-content">
                        <div v-loading="isLoadingSettings" class="tab-panel-body settings-panel-body">
                            <div class="settings-tab-bar">
                                <div
                                    class="settings-tab-item"
                                    :class="{ active: settingsActiveTab === 'agent' }"
                                    @click="settingsActiveTab = 'agent'"
                                >
                                    <el-icon :size="14">
                                        <UserFilled />
                                    </el-icon>
                                    Agent 配置
                                </div>
                                <div
                                    class="settings-tab-item"
                                    :class="{ active: settingsActiveTab === 'ai-settings' }"
                                    @click="settingsActiveTab = 'ai-settings'"
                                >
                                    <el-icon :size="14">
                                        <Cpu />
                                    </el-icon>
                                    AI 功能
                                </div>
                            </div>

                            <!-- Agent 配置内容 -->
                            <div v-show="settingsActiveTab === 'agent'" class="settings-scroll-area">
                                <div class="settings-card">
                                    <div class="settings-card-header">
                                        <el-icon :size="15" color="#667eea">
                                            <UserFilled />
                                        </el-icon>
                                        <span>作者 Agent</span>
                                    </div>
                                    <el-select
                                        v-model="agentConfig.authorAgentId"
                                        placeholder="选择作者 Agent"
                                        clearable
                                        style="width: 100%"
                                    >
                                        <el-option
                                            v-for="agent in userAgentList"
                                            :key="agent.id"
                                            :label="agent.name"
                                            :value="agent.id"
                                        />
                                    </el-select>
                                    <div class="settings-card-hint">用于辅助小说创作的 Agent</div>
                                </div>

                                <div class="settings-card">
                                    <div class="settings-card-header">
                                        <el-icon :size="15" color="#f59e0b">
                                            <EditPen />
                                        </el-icon>
                                        <span>编辑 Agent</span>
                                    </div>
                                    <el-select
                                        v-model="agentConfig.editorAgentId"
                                        placeholder="选择编辑 Agent"
                                        clearable
                                        style="width: 100%"
                                    >
                                        <el-option
                                            v-for="agent in userAgentList"
                                            :key="agent.id"
                                            :label="agent.name"
                                            :value="agent.id"
                                        />
                                    </el-select>
                                    <div class="settings-card-hint">用于审核和优化内容的 Agent</div>
                                </div>

                                <div class="settings-card">
                                    <div class="settings-card-header">
                                        <el-icon :size="15" color="#10b981">
                                            <ChatDotRound />
                                        </el-icon>
                                        <span>问答 Agent</span>
                                    </div>
                                    <el-select
                                        v-model="agentConfig.qaAgentId"
                                        placeholder="选择问答 Agent"
                                        clearable
                                        style="width: 100%"
                                    >
                                        <el-option
                                            v-for="agent in userAgentList"
                                            :key="agent.id"
                                            :label="agent.name"
                                            :value="agent.id"
                                        />
                                    </el-select>
                                    <div class="settings-card-hint">用于基于小说内容的智能问答 Agent</div>
                                </div>
                            </div>

                            <!-- AI 功能配置内容 -->
                            <div v-show="settingsActiveTab === 'ai-settings'" class="settings-scroll-area">
                                <NovelAiSettingsForm
                                    v-model="novelSettingsForm"
                                    :show-inherit-alert="!hasOwnSettings"
                                />
                            </div>
                        </div>
                        <div class="settings-footer">
                            <el-button
                                type="primary"
                                :loading="isSavingSettings"
                                class="settings-save-btn"
                                @click="saveSettingsPanel"
                            >
                                <el-icon>
                                    <Check />
                                </el-icon>
                                保存设置
                            </el-button>
                        </div>
                    </div>
                </aside>

                <!-- 活动栏（竖排图标） -->
                <div class="activity-bar">
                    <el-tooltip content="小说大纲" placement="left" :show-after="300">
                        <div
                            class="activity-icon"
                            :class="{ active: activeFunctionTab === 'outline' }"
                            @click="switchFunctionTab('outline')"
                        >
                            <el-icon :size="20">
                                <Document />
                            </el-icon>
                        </div>
                    </el-tooltip>
                    <el-tooltip content="人物图谱" placement="left" :show-after="300">
                        <div
                            class="activity-icon"
                            :class="{ active: activeFunctionTab === 'character' }"
                            @click="switchFunctionTab('character')"
                        >
                            <el-icon :size="20">
                                <Share />
                            </el-icon>
                        </div>
                    </el-tooltip>
                    <el-tooltip content="历史版本" placement="left" :show-after="300">
                        <div
                            class="activity-icon"
                            :class="{ active: activeFunctionTab === 'history' }"
                            @click="switchFunctionTab('history')"
                        >
                            <el-icon :size="20">
                                <Clock />
                            </el-icon>
                        </div>
                    </el-tooltip>
                    <el-tooltip content="AI 问答" placement="left" :show-after="300">
                        <div
                            class="activity-icon"
                            :class="{ active: activeFunctionTab === 'chat' }"
                            @click="switchFunctionTab('chat')"
                        >
                            <el-icon :size="20">
                                <ChatDotRound />
                            </el-icon>
                        </div>
                    </el-tooltip>
                    <el-tooltip content="批注列表" placement="left" :show-after="300">
                        <div
                            class="activity-icon"
                            :class="{ active: activeFunctionTab === 'annotations' }"
                            @click="switchFunctionTab('annotations')"
                        >
                            <el-icon :size="20">
                                <ChatLineSquare />
                            </el-icon>
                            <!-- 未读批注红点 -->
                            <span v-if="unreadAnnotationCount > 0" class="activity-badge">
                                {{ unreadAnnotationCount > 9 ? '9+' : unreadAnnotationCount }}
                            </span>
                        </div>
                    </el-tooltip>
                    <el-tooltip content="创作手记" placement="left" :show-after="300">
                        <div
                            class="activity-icon"
                            :class="{ active: activeFunctionTab === 'memo' }"
                            @click="switchFunctionTab('memo')"
                        >
                            <el-icon :size="20">
                                <Notebook />
                            </el-icon>
                        </div>
                    </el-tooltip>
                    <el-tooltip content="小说设置" placement="left" :show-after="300">
                        <div
                            class="activity-icon"
                            :class="{ active: activeFunctionTab === 'settings' }"
                            @click="switchFunctionTab('settings')"
                        >
                            <el-icon :size="20">
                                <Setting />
                            </el-icon>
                        </div>
                    </el-tooltip>
                </div>
            </div>
        </div>

        <!-- 创建章节弹窗 -->
        <el-dialog v-model="showCreateChapter" title="创建新章节" width="400px" class="chapter-dialog" align-center>
            <div class="chapter-dialog-body">
                <div class="chapter-number-badge">
                    <span class="chapter-number-label">第</span>
                    <el-input-number
                        v-model="newChapterNumber"
                        :min="1"
                        :step="1"
                        controls-position="right"
                        size="default"
                    />
                    <span class="chapter-number-label">章</span>
                </div>
                <el-input
                    v-model="newChapterTitle"
                    placeholder="请输入章节标题"
                    size="large"
                    clearable
                    @keyup.enter="addChapter"
                />
            </div>
            <template #footer>
                <el-button @click="showCreateChapter = false">取消</el-button>
                <el-button
                    type="primary"
                    :loading="isCreatingChapter"
                    :disabled="!newChapterTitle.trim()"
                    @click="addChapter"
                >
                    创建
                </el-button>
            </template>
        </el-dialog>

        <!-- 编辑章节弹窗 -->
        <el-dialog v-model="showEditChapter" title="编辑章节" width="400px" class="chapter-dialog" align-center>
            <div class="chapter-dialog-body">
                <div class="chapter-number-badge">
                    <span class="chapter-number-label">第</span>
                    <el-input-number
                        v-model="editingChapter.chapterNumber"
                        :min="1"
                        :step="1"
                        controls-position="right"
                        size="default"
                    />
                    <span class="chapter-number-label">章</span>
                </div>
                <el-input
                    v-model="editingChapter.title"
                    placeholder="请输入章节标题"
                    size="large"
                    clearable
                    @keyup.enter="updateChapter"
                />
            </div>
            <template #footer>
                <el-button @click="showEditChapter = false">取消</el-button>
                <el-button
                    type="primary"
                    :loading="isUpdatingChapter"
                    :disabled="!editingChapter.title.trim()"
                    @click="updateChapter"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>

        <!-- 重新入库确认弹窗 -->
        <el-dialog
            v-model="showResyncDialog"
            title="重新入库确认"
            width="420px"
            class="resync-confirm-dialog"
            align-center
        >
            <div v-if="resyncChapter" class="resync-confirm-body">
                <div class="resync-icon-wrapper">
                    <el-icon :size="36" color="#e6a23c"><WarningFilled /></el-icon>
                </div>
                <div class="resync-chapter-title">《{{ resyncChapter.title }}》</div>
                <div class="resync-desc">该章节已入库，重新入库将删除旧数据</div>
                <div class="resync-warning-tip">
                    <el-icon :size="14"><WarningFilled /></el-icon>
                    <span>重新入库将重新生成向量数据</span>
                </div>
            </div>
            <template #footer>
                <el-button @click="showResyncDialog = false">取消</el-button>
                <el-button type="warning" @click="confirmResync">重新入库</el-button>
            </template>
        </el-dialog>

        <!-- AI 大纲弹窗 -->
        <el-dialog
            v-model="showAiOutlineDialog"
            title="AI 生成的章节大纲"
            width="600px"
            class="ai-outline-dialog"
            align-center
        >
            <div class="ai-outline-content">
                <div class="ai-outline-tip">
                    <el-icon>
                        <Cpu />
                    </el-icon>
                    <span>以下是 AI 根据章节内容自动总结的大纲，你可以选择替换当前大纲或仅作参考。</span>
                </div>
                <el-input v-model="aiOutlineContent" type="textarea" :rows="12" readonly class="ai-outline-textarea" />
            </div>
            <template #footer>
                <el-button @click="showAiOutlineDialog = false">仅作参考</el-button>
                <el-button type="primary" @click="handleReplaceOutline"> 替换我的大纲 </el-button>
            </template>
        </el-dialog>

        <!-- AI 审核结果弹窗 -->
        <el-dialog
            v-model="showAuditResult"
            :title="
                'AI 审核结果 - ' +
                (auditResult?.overallAssessment === 'pass'
                    ? '通过'
                    : auditResult?.overallAssessment === 'warning'
                      ? '警告'
                      : '失败')
            "
            width="800px"
            class="audit-result-dialog"
            align-center
        >
            <div v-if="auditResult" class="audit-result-content">
                <div class="audit-summary">
                    <div class="summary-item">
                        <span class="summary-label">整体评估：</span>
                        <el-tag
                            :type="
                                auditResult.overallAssessment === 'pass'
                                    ? 'success'
                                    : auditResult.overallAssessment === 'warning'
                                      ? 'warning'
                                      : 'danger'
                            "
                        >
                            {{
                                auditResult.overallAssessment === 'pass'
                                    ? '通过'
                                    : auditResult.overallAssessment === 'warning'
                                      ? '警告'
                                      : '失败'
                            }}
                        </el-tag>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">问题数量：</span>
                        <span>{{ auditResult.issues.length }}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">处理时间：</span>
                        <span>{{ auditResult.processingTime }}ms</span>
                    </div>
                </div>

                <div v-if="auditResult.issues.length > 0" class="audit-issues">
                    <h3 class="issues-title">发现的问题</h3>
                    <div
                        v-for="issue in auditResult.issues"
                        :key="issue.id"
                        class="issue-item"
                        :class="'issue-severity-' + issue.severity"
                    >
                        <div class="issue-header">
                            <el-tag
                                :type="
                                    issue.severity === 'high'
                                        ? 'danger'
                                        : issue.severity === 'medium'
                                          ? 'warning'
                                          : 'info'
                                "
                                size="small"
                            >
                                {{
                                    issue.issueType === 'consistency'
                                        ? '一致性'
                                        : issue.issueType === 'logic'
                                          ? '逻辑'
                                          : issue.issueType === 'character'
                                            ? '人物'
                                            : '剧情'
                                }}
                            </el-tag>
                            <el-tag
                                :type="
                                    issue.severity === 'high'
                                        ? 'danger'
                                        : issue.severity === 'medium'
                                          ? 'warning'
                                          : 'info'
                                "
                                size="small"
                            >
                                {{ issue.severity === 'high' ? '高' : issue.severity === 'medium' ? '中' : '低' }}严重
                            </el-tag>
                        </div>
                        <div class="issue-content">
                            <div class="original-text">
                                <span class="text-label">原文：</span>
                                <span class="text-content">{{ issue.originalText }}</span>
                            </div>
                            <div class="issue-reason">
                                <span class="reason-label">原因：</span>
                                <span class="reason-content">{{ issue.reason }}</span>
                            </div>
                            <div v-if="issue.suggestion" class="issue-suggestion">
                                <span class="suggestion-label">建议：</span>
                                <span class="suggestion-content">{{ issue.suggestion }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="audit-actions">
                    <el-button @click="showAuditResult = false">关闭</el-button>
                    <el-button
                        v-if="auditResult.overallAssessment !== 'pass'"
                        type="primary"
                        @click="regenerateContent"
                    >
                        重新生成
                    </el-button>
                </div>
            </div>
        </el-dialog>

        <!-- 版本备注弹窗 -->
        <el-dialog v-model="showRemarkDialog" title="编辑版本备注" width="400px" align-center>
            <el-input
                v-model="editingRemark"
                type="textarea"
                :rows="3"
                placeholder="请输入版本备注..."
                maxlength="100"
                show-word-limit
            />
            <template #footer>
                <el-button @click="showRemarkDialog = false">取消</el-button>
                <el-button type="primary" :loading="isSavingRemark" @click="saveVersionRemark"> 保存 </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, onMounted, onUnmounted, ref, toRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
    ArrowDown,
    ArrowLeft,
    ArrowUp,
    ChatDotRound,
    ChatLineSquare,
    Check,
    CircleClose,
    Clock,
    Close,
    Connection,
    Cpu,
    DArrowLeft,
    DArrowRight,
    Delete,
    Document,
    DocumentCopy,
    Download,
    EditPen,
    Flag,
    FullScreen,
    List,
    MagicStick,
    Notebook,
    Plus,
    Promotion,
    Rank,
    RefreshLeft,
    Search,
    Setting,
    Share,
    Sort,
    Star,
    SuccessFilled,
    Upload,
    UserFilled,
    Warning,
    WarningFilled,
} from '@element-plus/icons-vue'
import { ElIcon, ElInput, ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { marked } from 'marked'
import draggable from 'vuedraggable'
import {
    batchUpdateChapterSort,
    type Chapter,
    type ChapterVersion,
    createChapter,
    deleteChapter,
    getAiOutline,
    getChapter,
    getChapterOutline,
    getChapterVersions,
    getNovelChaptersPage,
    getNovelOutline,
    publishChapterApi,
    reorderChapters as reorderChaptersApi,
    replaceOutlineWithAi,
    restoreChapterVersion,
    saveChapterOutline,
    saveNovelOutline,
    syncChapterVectorApi,
    updateChapter as updateChapterApi,
    updateChapterNumber as updateChapterNumberApi,
    updateVersionRemark,
} from '@/api/chapter'
import {
    aiAudit,
    type AIAuditIssue,
    type AIAuditorRequest,
    type AIAuditorResponse,
    type AIAuthorRequest,
    aiBatchAudit,
    aiChatStream,
    aiRegenerate,
    aiRewriteStream,
    type ChatSessionItem,
    createChatSession,
    deleteChatSession,
    listChatMessagesPage,
    listChatSessions,
    renameChatSession,
    revokeChatMessage,
    savePartialChatResponse,
    toggleChatSessionFavorite,
    type ToolExecutedEvent,
} from '@/api/ai'
import {
    createDefaultSettings,
    getEffectiveSettings,
    getNovelLevelSettings,
    type NovelSettings,
    saveNovelSettings,
} from '@/api/novel-settings'
import NovelAiSettingsForm from '@/components/novel/NovelAiSettingsForm.vue'
import {
    getNovelAgentConfig,
    getNovelDetail,
    type Novel,
    type NovelAgentConfig,
    type NovelAgentConfigRequest,
    saveNovelAgentConfig,
} from '@/api/novel-platform'
import { type AgentConfig, getAvailableAgentList } from '@/api/agent'
import { getCharacterGraph, type NovelCharacter, type NovelCharacterRelation } from '@/api/novel-character'
import { useNovelRouteStore } from '@/store/novel-route'
import { type ChatMessageLocal, useChatStore } from '@/store/chat'
import { useLayoutStore } from '@/store/layout'
import TiptapEditor from '@/components/editor/TiptapEditor.vue'
import { getNovelMemo, saveNovelMemo } from '@/api/novel-memo'
import {
    acceptAnnotation as acceptAnnotationApi,
    annotationAiRewriteStream,
    type ChapterAnnotation as AnnotationData,
    createAnnotation,
    deleteAnnotation as deleteAnnotationApi,
    getChapterAnnotations,
    markAnnotationViewed,
    resolveAnnotation as resolveAnnotationApi,
    saveAiRewriteResult as saveAiRewriteResultApi,
} from '@/api/annotation'

// 配置 marked 选项，支持流式输出中的换行渲染
marked.setOptions({
    breaks: true,
    gfm: true,
})

defineOptions({ name: 'NovelView' })

const route = useRoute()
const router = useRouter()
const novelRouteStore = useNovelRouteStore()
const novelId = computed(() => Number(route.params.id))

// 小说基本信息
const novelInfo = ref<Novel | null>(null)
const novelDisplayTitle = computed(() => novelInfo.value?.title || '')
const novelTotalWordCount = computed(() => novelInfo.value?.wordCount || 0)

/**
 * 加载小说基本信息（标题、总字数等）
 */
const loadNovelInfo = async () => {
    try {
        const res = await getNovelDetail(novelId.value)
        novelInfo.value = res.data
    } catch (error) {
        console.error('加载小说信息失败:', error)
    }
}

// 章节标题
const chapterTitle = ref('')

// 当前章节ID
const currentChapterId = ref(0)

// 章节内容
const chapterContent = ref('')

// 编辑器ref
const contentEditorRef = ref<InstanceType<typeof TiptapEditor> | null>(null)

/**
 * 滚动编辑器到底部
 * 用于AI生成内容时自动跟随
 */
const scrollEditorToBottom = () => {
    contentEditorRef.value?.scrollToBottom()
}

// 保存状态
const hasUnsavedChanges = ref(false)
const isSaving = ref(false)
const lastSavedContent = ref('')

// 大纲保存状态
const hasOutlineUnsavedChanges = ref(false)
const isOutlineSaving = ref(false)
const lastSavedOutline = ref({
    chapterOutline: '',
    plotPointsText: '',
    emotionTone: '',
    sceneSetting: '',
    selectedCharacters: [] as number[],
})

// 本地草稿管理
const DRAFT_KEY = computed(() => `novel_draft_${novelId.value}_${currentChapterId.value}`)
const draftContent = ref('') // 本地草稿内容
const lastDraftSaveTime = ref<Date | null>(null)

// 保存草稿到本地存储（含正文和大纲）
const saveDraftToLocal = () => {
    if (!currentChapterId.value) return

    const draft = {
        content: chapterContent.value,
        title: chapterTitle.value,
        chapterId: currentChapterId.value,
        novelId: novelId.value,
        saveTime: new Date().toISOString(),
        wordCount: chapterContent.value.length,
        // 大纲相关字段
        chapterOutline: chapterOutline.value,
        plotPointsText: plotPointsText.value,
        emotionTone: emotionTone.value,
        sceneSetting: sceneSetting.value,
        selectedCharacters: selectedCharacters.value,
    }

    try {
        localStorage.setItem(DRAFT_KEY.value, JSON.stringify(draft))
        draftContent.value = chapterContent.value
        lastDraftSaveTime.value = new Date()
        console.log('草稿已保存到本地:', new Date().toLocaleTimeString())
    } catch (e) {
        console.error('保存草稿失败:', e)
    }
}

// 从本地存储恢复草稿（含正文和大纲）
const loadDraftFromLocal = (): boolean => {
    if (!currentChapterId.value) return false

    try {
        const draftStr = localStorage.getItem(DRAFT_KEY.value)
        if (draftStr) {
            const draft = JSON.parse(draftStr)
            // 检查是否是对应当前章节
            if (draft.chapterId == currentChapterId.value && draft.novelId == novelId.value) {
                // 判断正文是否有变化（trim 后比较，避免空白差异导致误判）
                const draftContentTrimmed = (draft.content || '').trim()
                const currentContentTrimmed = (chapterContent.value || '').trim()
                const hasContentDiff = draftContentTrimmed.length > 0 && draftContentTrimmed !== currentContentTrimmed
                const hasOutlineDiff =
                    (draft.chapterOutline && draft.chapterOutline !== chapterOutline.value) ||
                    (draft.plotPointsText && draft.plotPointsText !== plotPointsText.value) ||
                    (draft.emotionTone && draft.emotionTone !== emotionTone.value) ||
                    (draft.sceneSetting && draft.sceneSetting !== sceneSetting.value)

                if (hasContentDiff || hasOutlineDiff) {
                    ElMessageBox.confirm(
                        `检测到本地有未保存的草稿（${new Date(draft.saveTime).toLocaleString()}，${draft.wordCount}字），是否恢复？`,
                        '恢复草稿',
                        {
                            confirmButtonText: '恢复草稿',
                            cancelButtonText: '放弃草稿',
                            type: 'warning',
                        },
                    )
                        .then(() => {
                            // 恢复正文
                            if (draft.content) {
                                chapterContent.value = draft.content
                            }
                            if (draft.title) {
                                chapterTitle.value = draft.title
                            }
                            // 恢复大纲字段
                            if (draft.chapterOutline !== undefined) {
                                chapterOutline.value = draft.chapterOutline
                            }
                            if (draft.plotPointsText !== undefined) {
                                plotPointsText.value = draft.plotPointsText
                            }
                            if (draft.emotionTone !== undefined) {
                                emotionTone.value = draft.emotionTone
                            }
                            if (draft.sceneSetting !== undefined) {
                                sceneSetting.value = draft.sceneSetting
                            }
                            if (draft.selectedCharacters !== undefined) {
                                selectedCharacters.value = draft.selectedCharacters
                            }
                            draftContent.value = draft.content || ''
                            hasUnsavedChanges.value = true
                            hasOutlineUnsavedChanges.value = true
                            ElMessage.success('草稿已恢复')
                        })
                        .catch(() => {
                            // 用户选择放弃，清除草稿
                            localStorage.removeItem(DRAFT_KEY.value)
                        })
                    return true
                }
            }
        }
    } catch (e) {
        console.error('加载草稿失败:', e)
    }
    return false
}

// 清除本地草稿
const clearDraft = () => {
    if (!currentChapterId.value) return
    try {
        localStorage.removeItem(DRAFT_KEY.value)
        draftContent.value = ''
    } catch (e) {
        console.error('清除草稿失败:', e)
    }
}

// 章节列表
const chapters = ref<Chapter[]>([])
const isLoadingChapters = ref(false)
const chapterListRef = ref<HTMLDivElement>()

// 分页相关
const chapterPageNum = ref(1)
const chapterPageSize = 50
const chapterTotal = ref(0)
const hasMoreChapters = ref(true)
const isLoadingMoreChapters = ref(false)

// 章节筛选（后端筛选）
const chapterSearchKeyword = ref('')
const chapterFilterStatus = ref<number | undefined>(undefined)
const chapterFilterVector = ref<number | undefined>(undefined)

// 监听筛选条件变化，防抖后重新加载第一页
let chapterFilterTimer: ReturnType<typeof setTimeout>
watch([chapterSearchKeyword, chapterFilterStatus, chapterFilterVector], () => {
    clearTimeout(chapterFilterTimer)
    chapterFilterTimer = setTimeout(() => {
        // 重置分页并重新加载
        chapterPageNum.value = 1
        hasMoreChapters.value = true
        chapters.value = []
        loadChapters()
    }, 300)
})

// 编辑章节
const showEditChapter = ref(false)
const editingChapter = ref<Chapter>({
    id: 0,
    novelId: 0,
    chapterNumber: 0,
    title: '',
    content: '',
    wordCount: 0,
    status: 'DRAFT',
    needRepublish: 0,
    vectorStored: 0,
    published: false,
    createTime: '',
    updateTime: '',
})
const isUpdatingChapter = ref(false)

// 重新入库确认弹窗
const showResyncDialog = ref(false)
const resyncChapter = ref<Chapter | null>(null)

// 小说大纲
const novelOutline = ref('')
const isLoadingOutline = ref(false)
const isEditingNovelOutline = ref(false)
const editingNovelOutlineContent = ref('')

/**
 * 将 Markdown 文本渲染为 HTML
 */
const renderedNovelOutline = computed(() => {
    if (!novelOutline.value) return ''
    return marked(novelOutline.value) as string
})

/**
 * 将 Markdown 文本渲染为 HTML（通用方法，用于 AI 问答消息等场景）
 * 支持流式输出中不完整的 Markdown 片段安全渲染
 *
 * @param content Markdown 原始文本
 * @returns 渲染后的 HTML 字符串
 */
const renderMarkdown = (content: string): string => {
    if (!content) return ''
    try {
        return marked(content) as string
    } catch {
        // 流式输出中 Markdown 片段可能不完整，降级为纯文本展示
        return content.replace(/\n/g, '<br>')
    }
}

/**
 * 复制消息内容到剪贴板（复制原始 Markdown 文本，非 HTML）
 *
 * @param content 消息原始文本
 */
const copyMessageContent = async (content: string) => {
    try {
        await navigator.clipboard.writeText(content)
        ElMessage.success('已复制到剪贴板')
    } catch (error) {
        ElMessage.error('复制失败，请手动选择复制')
    }
}

/**
 * 清除正文中的多余空白行（连续多个空行合并为一个）
 */
const cleanBlankLines = () => {
    if (!chapterContent.value.trim()) {
        ElMessage.warning('当前章节暂无内容')
        return
    }
    const originalLength = chapterContent.value.length
    // 视觉对应关系：\n = 段落换行，\n\n = 一个空行，\n\n\n = 两个空行
    // 清除策略：将 3 个及以上连续换行合并为 2 个（保留段落间一个空行）
    const cleaned = chapterContent.value
        .replace(/\n{3,}/g, '\n\n') // 3个及以上连续换行合并为2个（视觉上保留一个空行）
        .replace(/^\n+/, '') // 去除开头空行
        .replace(/\n+$/, '') // 去除结尾空行
    if (cleaned.length === originalLength) {
        ElMessage.info('没有多余的空白行')
        return
    }
    chapterContent.value = cleaned
    hasUnsavedChanges.value = true
    const removedLines = originalLength - cleaned.length
    ElMessage.success(`已清除 ${removedLines} 个多余空白字符`)
}

/**
 * 复制当前章节正文内容到剪贴板
 */
const copyChapterContent = async () => {
    if (!chapterContent.value.trim()) {
        ElMessage.warning('当前章节暂无内容')
        return
    }
    try {
        await navigator.clipboard.writeText(chapterContent.value)
        ElMessage.success('章节内容已复制到剪贴板')
    } catch (error) {
        ElMessage.error('复制失败，请手动选择复制')
    }
}

/**
 * 导出当前章节正文为 TXT 文件
 */
const exportChapterContent = () => {
    if (!chapterContent.value.trim()) {
        ElMessage.warning('当前章节暂无内容')
        return
    }
    const fileName = chapterTitle.value
        ? `${chapterTitle.value}.txt`
        : `第${currentChapterInfo.value?.chapterNumber || ''}章.txt`
    const blob = new Blob([chapterContent.value], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
}

/**
 * 处理撤回消息 - 显示全局确认弹窗
 *
 * @param msg 要撤回的消息
 * @param index 消息在列表中的索引
 */
const handleRevokeMessage = async (msg: ChatMessageLocal, index: number) => {
    if (!msg.id) return

    try {
        await ElMessageBox.confirm('撤回后消息将不参与后续对话', '确认撤回', {
            confirmButtonText: '确认撤回',
            cancelButtonText: '取消',
            type: 'warning',
            customClass: 'revoke-message-box',
            confirmButtonClass: 'revoke-confirm-btn',
            cancelButtonClass: 'revoke-cancel-btn',
        })

        await revokeChatMessage(msg.id)
        chatMessages.value[index].revoked = 1
        persistChatState()
        ElMessage.success('已撤回')
    } catch (error: any) {
        if (error !== 'cancel') {
            ElMessage.error('撤回失败')
        }
    }
}

// 右侧功能面板当前激活的 Tab（从布局缓存恢复）
const layoutStore = useLayoutStore()
const activeFunctionTab = toRef(layoutStore, 'activeFunctionTab')

// 面板标题映射
const functionTabTitle = computed(() => {
    const titleMap: Record<string, string> = {
        outline: '小说大纲',
        character: '人物图谱',
        history: '历史版本',
        chat: 'AI 问答',
        annotations: '批注列表',
        memo: '创作手记',
        settings: '小说设置',
    }
    return activeFunctionTab.value ? titleMap[activeFunctionTab.value] : ''
})

// 创建章节弹窗
const showCreateChapter = ref(false)
const newChapterTitle = ref('')
const newChapterNumber = ref(1)
const isCreatingChapter = ref(false)

// 编辑器背景色相关
const showBgColorPicker = ref(false)
const editorBgColor = ref('#fefefe')
const bgColorOptions = ref([
    { value: '#ffffff', label: '纯白' },
    { value: '#fefefe', label: '浅灰白' },
    { value: '#f5f5f5', label: '浅灰' },
    { value: '#f0f2f8', label: '淡蓝灰' },
    { value: '#fdf6e3', label: '米色' },
    { value: '#f0fff4', label: '淡绿' },
])

const selectBgColor = (color: string) => {
    editorBgColor.value = color
    showBgColorPicker.value = false
}

// 本章大纲
const chapterOutline = ref('')

// 剧情要点
const plotPointsText = ref('')

// 涉及人物
const _involvedCharacters = ref([
    { id: 1, name: '孙悟空', color: '#667eea' },
    { id: 2, name: '唐僧', color: '#f5576c' },
    { id: 3, name: '猪八戒', color: '#4facfe' },
])
const selectedCharacters = ref<number[]>([])

// 情感基调
const emotionTone = ref('')

// 场景设置
const sceneSetting = ref('')

// AI 问答
const chatInput = ref('')
const chatInputRef = ref<InstanceType<typeof ElInput> | null>(null)
const chatMessages = ref<ChatMessageLocal[]>([])
const isChatLoading = ref(false)
const chatMessagesRef = ref<HTMLDivElement>()
const chatStore = useChatStore()
const chatReady = ref(false)

/**
 * 是否还有更早的历史消息可加载
 */
const chatHasMore = ref(true)

/**
 * 是否正在加载更早的历史消息
 */
const chatLoadingMore = ref(false)

/**
 * 每页消息条数
 */
const CHAT_PAGE_SIZE = 30

/**
 * AI 思考状态提示文字（工具执行时动态更新）
 */
const chatThinkingText = ref('AI 正在思考...')

/**
 * 是否显示"一键到底"按钮（用户向上滚动超过阈值时显示）
 */
const showScrollToBottomBtn = ref(false)

/**
 * 判断用户是否不在底部附近的滚动距离阈值
 */
const SCROLL_BOTTOM_THRESHOLD = 200

/**
 * 监听聊天消息区域滚动，控制"一键到底"按钮的显隐，并触发加载更多
 */
const handleChatScroll = () => {
    if (!chatMessagesRef.value) return
    const container = chatMessagesRef.value
    const distanceToBottom = container.scrollHeight - container.scrollTop - container.clientHeight
    showScrollToBottomBtn.value = distanceToBottom > SCROLL_BOTTOM_THRESHOLD

    // 滚动到顶部附近时加载更早的消息
    if (container.scrollTop < 80) {
        loadMoreChatMessages()
    }
}

/**
 * 处理AI问答输入框回车事件
 * Enter发送消息，Shift+Enter换行
 */
const handleChatInputEnter = (e: KeyboardEvent) => {
    if (e.shiftKey) {
        // Shift+Enter 换行，不阻止默认行为
        return
    }
    // Enter 发送消息
    e.preventDefault()
    sendChatMessage()
}

// 问答会话管理
const chatSessionId = ref<number>(0)
const chatSessions = ref<ChatSessionItem[]>([])
let chatAbortController: AbortController | null = null

// 人物图谱
const miniGraphRef = ref<HTMLDivElement>()
let miniChartInstance: echarts.ECharts | null = null

// 历史版本
const historyVersions = ref<ChapterVersion[]>([])
const isLoadingVersions = ref(false)

// 预览版本
const previewVersionId = ref<number | null>(null)
const previewVersionContent = ref('')

// 版本备注弹窗
const showRemarkDialog = ref(false)
const editingVersion = ref<ChapterVersion | null>(null)
const editingRemark = ref('')
const isSavingRemark = ref(false)

// AI 改写相关
const isAiProcessing = ref(false)
const aiProcessingType = ref<'rewrite' | 'audit' | 'regenerate' | null>(null)
// 用户临时需求（对改写结果不满意时，补充具体要求）
const userRequirement = ref('')
const auditResult = ref<AIAuditorResponse | null>(null)
const showAuditResult = ref(false)

// 小说配置（从后端加载，控制自动审核等行为）
const novelSettings = ref<NovelSettings>(createDefaultSettings())

// 小说设置面板相关变量
const settingsActiveTab = ref('agent')
const isLoadingSettings = ref(false)
const isSavingSettings = ref(false)
const hasOwnSettings = ref(false)
const agents = ref<AgentConfig[]>([])
const agentConfig = ref<NovelAgentConfig>({
    novelId: 0,
    authorAgentId: undefined,
    editorAgentId: undefined,
    qaAgentId: undefined,
})
const novelSettingsForm = ref<NovelSettings>(createDefaultSettings())

// 专注模式（从布局缓存恢复）
const isFocusMode = toRef(layoutStore, 'isFocusMode')

// 左侧章节菜单收起状态（从布局缓存恢复）
const isSidebarCollapsed = toRef(layoutStore, 'isSidebarCollapsed')

// 大纲面板宽度（可拖拽调整，从布局缓存恢复）
const outlinePanelWidth = toRef(layoutStore, 'outlinePanelWidth')

/** 大纲面板最小宽度 */
const OUTLINE_PANEL_MIN_WIDTH = 300

/** 大纲面板最大宽度 */
const OUTLINE_PANEL_MAX_WIDTH = 700

// 大纲面板拖拽状态
let isResizingOutline = false
let resizeOutlineStartX = 0
let resizeOutlineStartWidth = 0

/** 开始拖拽大纲面板 */
const startResizeOutline = (e: MouseEvent) => {
    isResizingOutline = true
    resizeOutlineStartX = e.clientX
    resizeOutlineStartWidth = outlinePanelWidth.value
    document.addEventListener('mousemove', handleResizeOutline)
    document.addEventListener('mouseup', stopResizeOutline)
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'
}

/** 拖拽大纲面板中 */
const handleResizeOutline = (e: MouseEvent) => {
    if (!isResizingOutline) return
    const deltaX = e.clientX - resizeOutlineStartX
    const newWidth = resizeOutlineStartWidth + deltaX
    outlinePanelWidth.value = Math.min(OUTLINE_PANEL_MAX_WIDTH, Math.max(OUTLINE_PANEL_MIN_WIDTH, newWidth))
}

/** 停止拖拽大纲面板 */
const stopResizeOutline = () => {
    isResizingOutline = false
    document.removeEventListener('mousemove', handleResizeOutline)
    document.removeEventListener('mouseup', stopResizeOutline)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
}

// 右侧功能面板宽度（可拖拽调整，从布局缓存恢复）
const functionPanelWidth = toRef(layoutStore, 'functionPanelWidth')

/** 功能面板最小宽度 */
const FUNCTION_PANEL_MIN_WIDTH = 240

/** 功能面板最大宽度 */
const FUNCTION_PANEL_MAX_WIDTH = 600

// 计算属性
const wordCount = computed(() => chapterContent.value.replace(/\s/g, '').length)
const lineCount = computed(() => chapterContent.value.split('\n').length)
const paragraphCount = computed(() => chapterContent.value.split('\n').filter((p) => p.trim()).length)

/**
 * 处理编辑器内容统计变化事件
 * 由 TiptapEditor 组件触发，当前使用 computed 兜底，预留扩展
 */
const handleStatsChange = (_stats: { wordCount: number; lineCount: number; paragraphCount: number }) => {
    // 统计数据已通过 computed 从 chapterContent 实时计算，此处预留扩展
}

// ==================== 批注功能 ====================

// 当前章节的批注列表
const chapterAnnotations = ref<AnnotationData[]>([])

/** 按状态排序的批注列表：待处理 → 已采纳 → 已忽略 */
const sortedAnnotations = computed(() => {
    const statusOrder: Record<string, number> = { PENDING: 0, ACCEPTED: 1, RESOLVED: 2 }
    return [...chapterAnnotations.value].sort((a, b) => {
        const orderA = statusOrder[a.status] ?? 3
        const orderB = statusOrder[b.status] ?? 3
        return orderA - orderB
    })
})

/**
 * 加载章节批注列表
 *
 * @param chapterId 章节 ID
 */
const loadChapterAnnotations = async (chapterId: number) => {
    try {
        const res = await getChapterAnnotations(chapterId)
        chapterAnnotations.value = res.data || []
    } catch (error) {
        console.error('加载批注失败:', error)
        chapterAnnotations.value = []
    }
}

/**
 * 处理创建批注事件（由 TiptapEditor 组件触发）
 */
const handleCreateAnnotation = async (data: {
    startOffset: number
    endOffset: number
    originalText: string
    annotationContent: string
}) => {
    if (!currentChapterId.value) return
    try {
        const res = await createAnnotation({
            chapterId: currentChapterId.value,
            novelId: novelId.value,
            startOffset: data.startOffset,
            endOffset: data.endOffset,
            originalText: data.originalText,
            annotationContent: data.annotationContent,
        })
        // 将新批注添加到列表，触发编辑器高亮更新
        chapterAnnotations.value.push(res.data)
        ElMessage.success('批注已添加')
    } catch (error) {
        ElMessage.error('添加批注失败')
        console.error('创建批注失败:', error)
    }
}

/**
 * 处理删除批注事件（由 TiptapEditor 组件触发）
 */
const handleDeleteAnnotation = async (annotationId: string) => {
    try {
        await deleteAnnotationApi(Number(annotationId))
        chapterAnnotations.value = chapterAnnotations.value.filter((a) => String(a.id) !== String(annotationId))
        ElMessage.success('批注已删除')
    } catch (error) {
        ElMessage.error('删除批注失败')
        console.error('删除批注失败:', error)
    }
}

/**
 * 处理批注 AI 改写事件（由 TiptapEditor 组件触发）
 * 将整章内容 + 被批注原文 + 批注意见发送给 AI，流式返回改写结果
 */
const handleAiRewriteAnnotation = (
    data: {
        annotationId: number
        originalText: string
        annotationContent: string
        fullChapterContent: string
    },
    callbacks: {
        onToken: (token: string) => void
        onComplete: (fullText: string) => void
        onError: (error: Error) => void
    },
) => {
    annotationAiRewriteStream(
        {
            annotationId: data.annotationId,
            chapterId: currentChapterId.value,
            fullChapterContent: data.fullChapterContent,
            originalText: data.originalText,
            annotationContent: data.annotationContent,
        },
        {
            onToken: callbacks.onToken,
            onComplete: (fullText: string) => {
                // 改写完成后保存结果到后端
                saveAiRewriteResultApi(data.annotationId, fullText).catch((error) => {
                    console.error('保存 AI 改写结果失败:', error)
                })
                callbacks.onComplete(fullText)
            },
            onError: callbacks.onError,
        },
    )
}

/**
 * 处理采纳 AI 改写结果（替换正文中对应的原文片段）
 */
const handleAcceptRewrite = async (data: { annotationId: string; originalText: string; rewriteResult: string }) => {
    const originalIndex = chapterContent.value.indexOf(data.originalText)
    if (originalIndex === -1) {
        ElMessage.warning('原文已发生变化，无法自动替换，请手动修改')
        return
    }
    chapterContent.value =
        chapterContent.value.substring(0, originalIndex) +
        data.rewriteResult +
        chapterContent.value.substring(originalIndex + data.originalText.length)

    try {
        await acceptAnnotationApi(Number(data.annotationId))
        chapterAnnotations.value = chapterAnnotations.value.filter((a) => String(a.id) !== String(data.annotationId))
        hasUnsavedChanges.value = true
        ElMessage.success('已采纳改写结果')
    } catch (error) {
        ElMessage.error('采纳失败')
        console.error('采纳批注失败:', error)
    }
}

// ==================== 快捷 AI 改写 ====================

/**
 * 处理快捷 AI 改写事件
 */
const handleQuickRewrite = (
    data: { originalText: string; rewritePrompt: string; fullChapterContent: string },
    callbacks: {
        onToken: (token: string) => void
        onComplete: (fullText: string) => void
        onError: (error: Error) => void
    },
) => {
    annotationAiRewriteStream(
        {
            chapterId: currentChapterId.value,
            novelId: novelId.value,
            fullChapterContent: data.fullChapterContent,
            originalText: data.originalText,
            annotationContent: data.rewritePrompt,
        },
        callbacks,
    )
}

/**
 * 处理快捷改写采纳事件（替换正文中对应的原文片段）
 */
const handleAcceptQuickRewrite = (data: { originalText: string; rewriteResult: string }) => {
    const originalIndex = chapterContent.value.indexOf(data.originalText)
    if (originalIndex === -1) {
        ElMessage.warning('原文已发生变化，无法自动替换，请手动修改')
        return
    }
    chapterContent.value =
        chapterContent.value.substring(0, originalIndex) +
        data.rewriteResult +
        chapterContent.value.substring(originalIndex + data.originalText.length)
    hasUnsavedChanges.value = true
    ElMessage.success('已采纳改写结果')
}

// ==================== 批注已读与状态管理 ====================

/** 未读批注数量（基于后端 viewed 字段） */
const unreadAnnotationCount = computed(() => {
    return chapterAnnotations.value.filter((a) => a.viewed === 0).length
})

/**
 * 判断批注是否已读
 */
const isAnnotationRead = (annotationId: number): boolean => {
    const annotation = chapterAnnotations.value.find((a) => a.id === annotationId)
    return annotation ? annotation.viewed === 1 : true
}

/**
 * 处理查看批注事件（由 TiptapEditor 触发，调用后端标记已查看）
 */
const handleViewAnnotation = (annotationId: string) => {
    const index = chapterAnnotations.value.findIndex((a) => String(a.id) === String(annotationId))
    if (index >= 0 && chapterAnnotations.value[index].viewed === 0) {
        chapterAnnotations.value[index] = {
            ...chapterAnnotations.value[index],
            viewed: 1,
        }
        markAnnotationViewed(Number(annotationId)).catch((error) => {
            console.error('标记批注已查看失败:', error)
        })
    }
}

/**
 * 批注列表点击：标记已读并跳转到编辑器中对应位置
 */
const handleAnnotationListClick = (annotation: AnnotationData) => {
    handleViewAnnotation(annotation.id)
    contentEditorRef.value?.scrollToAnnotation(annotation)
}

/**
 * 标记批注为已处理
 */
const handleResolveAnnotation = async (annotationId: string) => {
    try {
        await resolveAnnotationApi(Number(annotationId))
        const annotation = chapterAnnotations.value.find((a) => String(a.id) === String(annotationId))
        if (annotation) {
            annotation.status = 'RESOLVED'
        }
        ElMessage.success('批注已忽略')
    } catch (error) {
        ElMessage.error('操作失败')
        console.error('忽略批注失败:', error)
    }
}

/**
 * 采纳批注：跳转到批注位置并弹出详情弹窗，由用户操作 AI 改写
 */
const handleAdoptAnnotation = (annotation: AnnotationData) => {
    handleViewAnnotation(annotation.id)
    contentEditorRef.value?.scrollToAnnotationAndShowPopup(annotation)
}

// ==================== 创作手记 ====================

const memoContent = ref('')
const editingMemoContent = ref('')
const isEditingMemo = ref(false)
const isLoadingMemo = ref(false)
const isSavingMemo = ref(false)

/**
 * 渲染手记 Markdown 内容
 */
const renderedMemoContent = computed(() => {
    if (!memoContent.value) return ''
    return marked(memoContent.value) as string
})

/**
 * 加载创作手记
 */
const loadMemo = async () => {
    isLoadingMemo.value = true
    try {
        const res = await getNovelMemo(novelId.value)
        memoContent.value = res.data?.content || ''
    } catch (error) {
        console.error('加载创作手记失败:', error)
    } finally {
        isLoadingMemo.value = false
    }
}

const startEditMemo = () => {
    editingMemoContent.value = memoContent.value
    isEditingMemo.value = true
}

const cancelEditMemo = () => {
    isEditingMemo.value = false
}

const saveMemoHandler = async () => {
    isSavingMemo.value = true
    try {
        await saveNovelMemo(novelId.value, editingMemoContent.value)
        memoContent.value = editingMemoContent.value
        isEditingMemo.value = false
        ElMessage.success('手记已保存')
    } catch (error) {
        ElMessage.error('保存手记失败')
    } finally {
        isSavingMemo.value = false
    }
}

/**
 * 格式化批注列表时间
 */
const formatAnnotationListTime = (timeStr: string): string => {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${month}-${day} ${hours}:${minutes}`
}

/**
 * 截断文本
 */
const truncateText = (text: string, maxLength: number): string => {
    if (!text) return ''
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

/**
 * 批注状态标签
 */
const annotationStatusLabel = (status: string): string => {
    const labelMap: Record<string, string> = {
        PENDING: '待处理',
        ACCEPTED: '已采纳',
        RESOLVED: '已忽略',
    }
    return labelMap[status] || status
}

// 当前章节对象（用于收起侧边栏时展示信息）
const currentChapterInfo = computed(() => chapters.value.find((c) => c.id === currentChapterId.value) || null)

// 字数格式化：超过 1000 显示 x.xk
const formatWordCount = (count: number | string) => {
    const num = Number(count) || 0
    if (!num) return '0字'
    if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
    return num + '字'
}

// 人物数据（从后端获取）
const characterData = ref<NovelCharacter[]>([])
const characterRelationData = ref<NovelCharacterRelation[]>([])
const isLoadingCharacterGraph = ref(false)

// 时间格式化函数（精确到秒）
const formatTime = (time: string) => {
    if (!time) return ''
    const date = new Date(time)
    return (
        date.toLocaleDateString() +
        ' ' +
        date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    )
}

/**
 * 获取章节发布状态的样式类名
 */
const getPublishStatusClass = (chapter: Chapter): string => {
    if (chapter.status !== 'PUBLISHED') {
        return 'status-draft'
    }
    if (chapter.needRepublish) {
        return 'status-need-republish'
    }
    return 'status-published'
}

/**
 * 获取章节发布状态的图标组件
 */
const getPublishStatusIcon = (chapter: Chapter) => {
    if (chapter.status !== 'PUBLISHED') {
        return EditPen
    }
    if (chapter.needRepublish) {
        return WarningFilled
    }
    return SuccessFilled
}

/**
 * 获取章节发布状态的文字标签
 */
const getPublishStatusLabel = (chapter: Chapter): string => {
    if (chapter.status !== 'PUBLISHED') {
        return '草稿'
    }
    if (chapter.needRepublish) {
        return '有改动'
    }
    return '已发布'
}

/**
 * 获取章节发布状态的提示文案
 */
const getPublishStatusTooltip = (chapter: Chapter): string => {
    if (chapter.status !== 'PUBLISHED') {
        return '当前章节为草稿状态，尚未发布'
    }
    if (chapter.needRepublish) {
        return '发布后内容有改动，建议重新发布'
    }
    return '章节已发布，内容无改动'
}

/**
 * 手动同步章节到向量数据库
 * 如果已入库，会提示是否重新入库
 */
const syncChapterVector = async (chapter: Chapter) => {
    // 章节列表不返回 content 字段，通过 wordCount 判断是否有内容
    if (!chapter.wordCount || chapter.wordCount === 0) {
        ElMessage.warning('章节内容为空，无法同步到向量数据库')
        return
    }

    // 如果已入库，弹出确认弹窗
    if (chapter.vectorStored) {
        resyncChapter.value = chapter
        showResyncDialog.value = true
        return
    }

    // 未入库，直接同步
    await doSyncChapterVector(chapter)
}

/**
 * 执行向量同步操作
 */
const doSyncChapterVector = async (chapter: Chapter) => {
    try {
        await syncChapterVectorApi(chapter.id)
        ElMessage.success('已触发向量同步，请稍后刷新查看状态')
        chapter.vectorStored = 1
    } catch (error) {
        ElMessage.error('同步向量数据库失败')
    }
}

/**
 * 确认重新入库
 */
const confirmResync = async () => {
    if (!resyncChapter.value) return
    showResyncDialog.value = false
    await doSyncChapterVector(resyncChapter.value)
    resyncChapter.value = null
}

// 方法
const goBack = () => {
    // 主动返回列表页时，清除记录的创作页路径
    novelRouteStore.clearLastNovelPath()
    router.push('/novel').then(() => {
        // 返回首页后刷新页面
        window.location.reload()
    })
}

// 加载章节列表（第一页）
const loadChapters = async () => {
    isLoadingChapters.value = true
    chapterPageNum.value = 1
    hasMoreChapters.value = true
    try {
        const res = await getNovelChaptersPage(
            novelId.value,
            chapterPageNum.value,
            chapterPageSize,
            chapterFilterStatus.value,
            chapterFilterVector.value,
            chapterSearchKeyword.value,
        )
        chapters.value = res.data.list || []
        chapterTotal.value = res.data.total
        hasMoreChapters.value = res.data.hasMore
        // 默认选中第一个章节
        if (chapters.value.length > 0 && !currentChapterId.value) {
            await selectChapter(chapters.value[0].id)
        }
    } catch (error) {
        ElMessage.error('加载章节列表失败')
    } finally {
        isLoadingChapters.value = false
    }
}

// 加载更多章节（下一页）
const loadMoreChapters = async () => {
    if (isLoadingMoreChapters.value || !hasMoreChapters.value) return

    isLoadingMoreChapters.value = true
    try {
        const nextPage = chapterPageNum.value + 1
        const res = await getNovelChaptersPage(
            novelId.value,
            nextPage,
            chapterPageSize,
            chapterFilterStatus.value,
            chapterFilterVector.value,
            chapterSearchKeyword.value,
        )
        const newChapters = res.data.list || []

        if (newChapters.length > 0) {
            chapters.value.push(...newChapters)
            chapterPageNum.value = nextPage
            hasMoreChapters.value = res.data.hasMore
        } else {
            hasMoreChapters.value = false
        }
    } catch (error) {
        console.error('加载更多章节失败', error)
    } finally {
        isLoadingMoreChapters.value = false
    }
}

// 处理章节列表滚动事件
const handleChapterListScroll = () => {
    if (!chapterListRef.value) return

    const container = chapterListRef.value
    const scrollBottom = container.scrollHeight - container.scrollTop - container.clientHeight

    // 距离底部小于 100px 时加载更多
    if (scrollBottom < 100) {
        loadMoreChapters()
    }
}

// 快速定位锚点列表（倒序排列，与章节列表排序一致）
const _QUICK_JUMP_VISIBLE_COUNT = 5
const quickJumpAnchors = computed(() => {
    const total = chapterTotal.value
    if (total <= 10) return []

    // 根据总章节数动态计算间隔
    let interval: number
    if (total <= 100) {
        interval = 10
    } else if (total <= 500) {
        interval = 50
    } else if (total <= 2000) {
        interval = 100
    } else {
        interval = 200
    }

    // 生成锚点并倒序排列（与章节列表排序一致）
    const anchors: number[] = []
    for (let i = interval; i <= total; i += interval) {
        anchors.push(i)
    }
    return anchors.reverse()
})

// 当前选中章节所属的锚点（用于高亮显示）
const activeJumpAnchor = computed(() => {
    if (!currentChapterInfo.value || quickJumpAnchors.value.length === 0) return null
    const chapterNum = currentChapterInfo.value.chapterNumber
    // 锚点是倒序的，找到第一个 <= 当前章节号的锚点（即所属区间）
    // 例如章节 35，锚点 [70,60,50,40,30,20,10]，应高亮 30
    const sortedAsc = [...quickJumpAnchors.value].sort((a, b) => a - b)
    let matched: number | null = null
    for (const anchor of sortedAsc) {
        if (anchor <= chapterNum) {
            matched = anchor
        } else {
            break
        }
    }
    return matched
})

/**
 * 快速定位到指定章节号
 * 章节列表按 sortOrder 倒序排列，第一页是最新章节，越往后页码越小
 * 需要逐页加载直到找到目标章节
 */
const jumpToChapter = async (targetChapterNumber: number) => {
    // 在已加载列表中查找目标章节
    const existingIndex = chapters.value.findIndex((ch) => ch.chapterNumber === targetChapterNumber)

    if (existingIndex >= 0) {
        scrollToChapterIndex(existingIndex)
        return
    }

    // 目标章节未加载，持续加载下一页直到找到或无更多数据
    isLoadingMoreChapters.value = true
    try {
        while (hasMoreChapters.value) {
            const nextPage = chapterPageNum.value + 1
            const res = await getNovelChaptersPage(
                novelId.value,
                nextPage,
                chapterPageSize,
                chapterFilterStatus.value,
                chapterFilterVector.value,
                chapterSearchKeyword.value,
            )
            const newChapters = res.data.list || []
            if (newChapters.length === 0) {
                hasMoreChapters.value = false
                break
            }

            chapters.value.push(...newChapters)
            chapterPageNum.value = nextPage
            hasMoreChapters.value = res.data.hasMore

            // 检查目标章节是否已在新加载的数据中
            const found = newChapters.some((ch) => ch.chapterNumber === targetChapterNumber)
            if (found) break
        }
    } catch (error) {
        ElMessage.error('加载章节数据失败')
        return
    } finally {
        isLoadingMoreChapters.value = false
    }

    // 加载完成后查找并滚动
    await nextTick()
    const loadedIndex = chapters.value.findIndex((ch) => ch.chapterNumber === targetChapterNumber)
    if (loadedIndex >= 0) {
        scrollToChapterIndex(loadedIndex)
    }
}

/**
 * 滚动章节列表到指定索引位置
 */
const scrollToChapterIndex = (index: number) => {
    if (!chapterListRef.value) return
    const chapterItems = chapterListRef.value.querySelectorAll('.chapter-item')
    if (chapterItems[index]) {
        chapterItems[index].scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
}

// 快速定位导航条引用
const quickJumpListRef = ref<HTMLDivElement>()

/**
 * 点击箭头切换到上/下一个锚点（滚动导航条列表）
 * @param direction -1 向上切换，1 向下切换
 */
const scrollJumpRail = (direction: number) => {
    if (!quickJumpListRef.value) return
    const buttons = quickJumpListRef.value.querySelectorAll('.quick-jump-rail-btn')
    if (buttons.length === 0) return

    const container = quickJumpListRef.value
    const containerRect = container.getBoundingClientRect()
    // 找到当前可视区域中间位置最近的按钮索引
    let closestIndex = 0
    let closestDistance = Infinity
    buttons.forEach((btn, index) => {
        const btnRect = btn.getBoundingClientRect()
        const btnCenter = btnRect.top + btnRect.height / 2
        const containerCenter = containerRect.top + containerRect.height / 2
        const distance = Math.abs(btnCenter - containerCenter)
        if (distance < closestDistance) {
            closestDistance = distance
            closestIndex = index
        }
    })

    // 计算目标索引
    const targetIndex = Math.max(0, Math.min(buttons.length - 1, closestIndex + direction))
    buttons[targetIndex].scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// 加载小说大纲
const loadNovelOutline = async () => {
    isLoadingOutline.value = true
    try {
        const res = await getNovelOutline(novelId.value)
        if (res.data) {
            novelOutline.value = res.data.outlineContent || ''
        }
    } catch (error) {
        novelOutline.value = ''
    } finally {
        isLoadingOutline.value = false
    }
}

// 选择章节
const selectChapter = async (id: number) => {
    // 如果有未保存的更改，先保存到本地
    if (hasUnsavedChanges.value && currentChapterId.value) {
        saveDraftToLocal()
    }

    // 停止之前的同步循环
    stopContentSyncLoop()
    stopOutlineSyncLoop()

    currentChapterId.value = id
    const chapter = chapters.value.find((c) => c.id === id)
    if (chapter) {
        chapterTitle.value = `第${chapter.chapterNumber}章 ${chapter.title || '未命名章节'}`
        // 加载章节详情
        try {
            const res = await getChapter(id)
            if (res.data) {
                chapterContent.value = res.data.content || ''
                lastSavedContent.value = chapterContent.value
                // 初始化同步状态
                lastSyncedContent.value = chapterContent.value
                contentLastChangeTime.value = 0
                hasUnsavedChanges.value = false

                // 加载章节大纲
                await loadChapterOutline(id)

                // 加载章节批注
                await loadChapterAnnotations(id)
                // 初始化大纲同步状态
                lastSyncedOutline.value = {
                    chapterOutline: chapterOutline.value,
                    plotPointsText: plotPointsText.value,
                    emotionTone: emotionTone.value,
                    sceneSetting: sceneSetting.value,
                    selectedCharacters: [...selectedCharacters.value],
                }
                outlineLastChangeTime.value = 0

                // 加载完成后检查本地草稿
                nextTick(() => {
                    loadDraftFromLocal()
                })
            }
        } catch (error) {
            ElMessage.error('加载章节内容失败')
        }
    }
}

// 打开创建章节弹窗
const openCreateChapterDialog = () => {
    newChapterTitle.value = ''
    // 默认章节号为当前最大章节号 + 1
    const maxNumber = chapters.value.reduce((max, c) => Math.max(max, c.chapterNumber), 0)
    newChapterNumber.value = maxNumber + 1
    showCreateChapter.value = true
}

// 添加章节
const addChapter = async () => {
    if (!newChapterTitle.value.trim()) {
        ElMessage.warning('请输入章节标题')
        return
    }

    // 校验章节号是否与已有章节重复
    const duplicateChapter = chapters.value.find((c) => c.chapterNumber === newChapterNumber.value)
    if (duplicateChapter) {
        ElMessage.warning(`第 ${newChapterNumber.value} 章已存在（${duplicateChapter.title}），请更换章节号`)
        return
    }

    isCreatingChapter.value = true
    try {
        const res = await createChapter({
            novelId: novelId.value,
            title: newChapterTitle.value.trim(),
        })
        if (res.data) {
            // 如果用户指定的章节号与自动分配的不同，更新章节号
            if (res.data.chapterNumber !== newChapterNumber.value) {
                await updateChapterNumberApi(res.data.id, newChapterNumber.value)
                res.data.chapterNumber = newChapterNumber.value
            }
            // 新创建的章节插入到列表头部（倒序排列）
            chapters.value.unshift(res.data)
            showCreateChapter.value = false
            await selectChapter(res.data.id)
            ElMessage.success('章节创建成功')
        }
    } catch (error) {
        ElMessage.error('创建章节失败')
    } finally {
        isCreatingChapter.value = false
    }
}

// 打开编辑章节弹窗
const openEditChapterDialog = (chapter: Chapter) => {
    editingChapter.value = { ...chapter }
    showEditChapter.value = true
}

// 更新章节
const updateChapter = async () => {
    if (!editingChapter.value.title.trim()) {
        ElMessage.warning('请输入章节标题')
        return
    }

    // 校验章节号是否与其他章节重复（排除自身）
    const duplicateChapter = chapters.value.find(
        (c) => c.id !== editingChapter.value.id && c.chapterNumber === editingChapter.value.chapterNumber,
    )
    if (duplicateChapter) {
        ElMessage.warning(
            `第 ${editingChapter.value.chapterNumber} 章已存在（${duplicateChapter.title}），请更换章节号`,
        )
        return
    }

    isUpdatingChapter.value = true
    try {
        const originalChapter = chapters.value.find((c) => c.id === editingChapter.value.id)
        const chapterNumberChanged =
            originalChapter && editingChapter.value.chapterNumber !== originalChapter.chapterNumber

        // 如果章节号有变更，先更新章节号
        if (chapterNumberChanged) {
            await updateChapterNumberApi(editingChapter.value.id, editingChapter.value.chapterNumber)
        }

        // 更新标题
        const res = await updateChapterApi(editingChapter.value.id, {
            title: editingChapter.value.title.trim(),
        })
        if (res.data) {
            // 合并章节号变更（后端返回的数据已包含最新章节号）
            const index = chapters.value.findIndex((c) => c.id === editingChapter.value.id)
            if (index !== -1) {
                chapters.value[index] = { ...res.data, chapterNumber: editingChapter.value.chapterNumber }
            }
            showEditChapter.value = false
            ElMessage.success('章节更新成功')
        }
    } catch (error) {
        ElMessage.error('更新章节失败')
    } finally {
        isUpdatingChapter.value = false
    }
}

/**
 * 拖拽排序结束后，将新顺序同步到后端
 */
const handleDragEnd = async () => {
    const chapterIds = chapters.value.map((c) => c.id)
    try {
        await batchUpdateChapterSort(novelId.value, chapterIds)
        // 拖拽后重新加载以同步章节号
        await loadChapters()
        ElMessage.success('排序已更新')
    } catch (error) {
        ElMessage.error('排序更新失败')
        await loadChapters()
    }
}

// 一键重排章节号
const handleReorderChapters = async () => {
    try {
        await ElMessageBox.confirm('将按当前排列顺序重新连续编号（1, 2, 3...），确认执行？', '一键重排章节号', {
            confirmButtonText: '确认重排',
            cancelButtonText: '取消',
            type: 'warning',
        })
        await reorderChaptersApi(novelId.value)
        // 重新加载章节列表
        chapters.value = []
        chapterPageNum.value = 1
        hasMoreChapters.value = true
        await loadChapters()
        ElMessage.success('章节号重排完成')
    } catch (error: any) {
        if (error !== 'cancel') {
            ElMessage.error('重排失败')
        }
    }
}

// 确认删除章节
const confirmDeleteChapter = (chapter: Chapter) => {
    ElMessageBox.confirm(`确定要删除第${chapter.chapterNumber}章「${chapter.title || '未命名章节'}」吗？`, '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
    })
        .then(async () => {
            try {
                await deleteChapter(chapter.id)
                const index = chapters.value.findIndex((c) => c.id === chapter.id)
                if (index !== -1) {
                    chapters.value.splice(index, 1)
                }
                if (currentChapterId.value === chapter.id) {
                    // 如果删除的是当前章节，切换到其他章节
                    if (chapters.value.length > 0) {
                        await selectChapter(chapters.value[0].id)
                    } else {
                        currentChapterId.value = 0
                        chapterContent.value = ''
                        chapterOutline.value = ''
                    }
                }
                ElMessage.success('章节删除成功')
            } catch (error) {
                ElMessage.error('删除章节失败')
            }
        })
        .catch(() => {
            // 取消删除
        })
}

// AI 流式改写
// 用于取消正在进行的流式请求
let rewriteAbortController: AbortController | null = null

const aiRewrite = () => {
    if (!chapterOutline.value || !chapterOutline.value.trim()) {
        ElMessage.warning('请先输入本章大纲')
        return
    }

    isAiProcessing.value = true
    aiProcessingType.value = 'rewrite'
    // AI 改写开始前停止自动同步循环，避免中间状态被保存
    stopContentSyncLoop()
    // 清空当前内容，准备接收流式数据
    chapterContent.value = ''

    const request: AIAuthorRequest = {
        chapterId: currentChapterId.value,
        content: chapterContent.value,
        outline: chapterOutline.value,
        plotPoints: plotPointsText.value,
        characters: selectedCharacters.value,
        emotionTone: emotionTone.value,
        sceneSetting: sceneSetting.value,
        userRequirement: userRequirement.value || undefined,
    }

    rewriteAbortController = aiRewriteStream(request, {
        onToken: (token: string) => {
            // 逐 Token 追加到编辑器内容
            chapterContent.value += token
            // 自动滚动到底部
            scrollEditorToBottom()
        },
        onComplete: () => {
            hasUnsavedChanges.value = true
            isAiProcessing.value = false
            aiProcessingType.value = null
            rewriteAbortController = null
            ElMessage.success('AI 改写完成')
            // 改写完成后立即执行一次内容同步（保存到后端并创建版本）
            performContentSync()
            // 根据小说配置决定是否自动审核
            if (novelSettings.value.autoAuditAfterRewrite === 1) {
                performAiAudit()
            }
        },
        onError: (error: Error) => {
            isAiProcessing.value = false
            aiProcessingType.value = null
            rewriteAbortController = null
            ElMessage.error('AI 改写失败：' + error.message)
        },
    })
}

// 取消 AI 改写，终止流式请求
const cancelRewrite = () => {
    if (rewriteAbortController) {
        rewriteAbortController.abort()
        rewriteAbortController = null
    }
    isAiProcessing.value = false
    aiProcessingType.value = null
    ElMessage.info('已取消改写')
}

// AI 审核
const performAiAudit = async () => {
    if (!chapterContent.value.trim()) {
        return
    }

    isAiProcessing.value = true
    aiProcessingType.value = 'audit'

    try {
        const currentChapter = chapters.value.find((c) => c.id === currentChapterId.value)
        const currentChapterNumber = currentChapter?.chapterNumber || 0

        const request: AIAuditorRequest = {
            chapterId: currentChapterId.value,
            content: chapterContent.value,
            novelOutline: novelOutline.value,
            previousChapters: chapters.value
                .filter((ch) => ch.chapterNumber < currentChapterNumber)
                .map((ch) => ({
                    chapterId: ch.id,
                    chapterNumber: ch.chapterNumber,
                    content: '', // 这里需要实际获取章节内容，暂时为空
                })),
        }

        const response = await aiAudit(request)
        auditResult.value = response
        showAuditResult.value = true
    } catch (error) {
        ElMessage.error('AI 审核失败，请稍后重试')
    } finally {
        isAiProcessing.value = false
        aiProcessingType.value = null
    }
}

// 重新生成内容
const regenerateContent = async () => {
    if (!chapterContent.value || !chapterContent.value.trim()) {
        ElMessage.warning('请先在右侧【正文内容】区域输入需要改写的内容')
        return
    }

    isAiProcessing.value = true
    aiProcessingType.value = 'regenerate'

    try {
        const request: AIAuthorRequest = {
            chapterId: currentChapterId.value,
            content: chapterContent.value,
            outline: chapterOutline.value,
            plotPoints: plotPointsText.value,
            characters: selectedCharacters.value,
            emotionTone: emotionTone.value,
            sceneSetting: sceneSetting.value,
        }

        const response = await aiRegenerate(request)
        chapterContent.value = response.content
        hasUnsavedChanges.value = true
        showAuditResult.value = false

        if (novelSettings.value.autoAuditAfterRewrite === 1) {
            await performAiAudit()
        }
    } catch (error) {
        ElMessage.error('重新生成失败，请稍后重试')
    } finally {
        isAiProcessing.value = false
        aiProcessingType.value = null
    }
}

// 批量审核功能（处理大纲过长的情况）
const _performBatchAudit = async () => {
    if (!chapterContent.value.trim()) {
        ElMessage.warning('请先输入需要审核的内容')
        return
    }

    isAiProcessing.value = true
    aiProcessingType.value = 'audit'

    try {
        // 计算章节范围，确保有重叠
        const currentChapter = chapters.value.find((c) => c.id === currentChapterId.value)
        if (!currentChapter) return

        const totalChapters = chapters.value.length
        const chapterRanges = []

        // 生成重叠的章节范围
        const batchSize = 5 // 每个审核负责5个章节
        const overlap = 2 // 重叠2个章节

        for (let i = 1; i <= totalChapters; i += batchSize - overlap) {
            chapterRanges.push({
                startChapter: Math.max(1, i),
                endChapter: Math.min(totalChapters, i + batchSize - 1),
            })
        }

        const request = {
            chapterId: currentChapterId.value,
            content: chapterContent.value,
            novelOutline: novelOutline.value,
            chapterRanges,
        }

        const response = await aiBatchAudit(request)

        // 合并审核结果
        const mergedIssues: AIAuditIssue[] = []
        response.audits.forEach((audit) => {
            audit.issues.forEach((issue) => {
                // 避免重复问题
                if (!mergedIssues.some((existing) => existing.originalText === issue.originalText)) {
                    mergedIssues.push(issue)
                }
            })
        })

        // 创建合并后的审核结果
        auditResult.value = {
            id: response.id,
            overallAssessment: response.overallAssessment,
            issues: mergedIssues,
            summary: response.summary,
            processingTime: response.audits.reduce((sum, audit) => sum + audit.processingTime, 0),
        }

        showAuditResult.value = true
    } catch (error) {
        ElMessage.error('批量审核失败，请稍后重试')
    } finally {
        isAiProcessing.value = false
        aiProcessingType.value = null
    }
}

// 切换专注模式
const toggleFocusMode = () => {
    isFocusMode.value = !isFocusMode.value
    if (isFocusMode.value) {
        ElMessage.success('已进入专注模式，本章大纲已收起')
    } else {
        ElMessage.info('已退出专注模式')
    }
}

// AI 问答（切换到问答 Tab）
const _openAIChat = () => {
    activeFunctionTab.value = 'chat'
    nextTick(() => {
        scrollToBottom()
    })
}

/**
 * 初始化或加载问答会话（切换到问答 Tab 时调用）
 */
const initChatSession = async () => {
    if (!novelId.value) return

    // 优先从 store 恢复缓存的问答状态（避免切换页面后断开）
    const cachedState = chatStore.getChatState(novelId.value)
    if (cachedState && cachedState.sessions.length > 0) {
        chatSessions.value = cachedState.sessions
        chatSessionId.value = cachedState.sessionId
        chatMessages.value = cachedState.messages
        // 立即设置 scrollTop 避免闪烁
        nextTick(() => {
            if (chatMessagesRef.value) {
                chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
            }
            chatReady.value = true
        })
        return
    }

    try {
        // 加载该小说的会话列表
        const res = await listChatSessions(novelId.value)
        chatSessions.value = res.data || []

        if (chatSessions.value.length > 0) {
            // 复用最近的会话
            chatSessionId.value = chatSessions.value[0].id
            await loadChatHistory(chatSessionId.value)
        } else {
            // 创建新会话
            const sessionRes = await createChatSession(novelId.value, '默认对话')
            chatSessionId.value = sessionRes.data.id
            chatSessions.value.unshift(sessionRes.data)
            // 新会话添加欢迎消息
            chatMessages.value = [
                {
                    role: 'assistant',
                    content: '你好，我是你的 AI 助手，可以帮你回顾小说内容、解答疑问。请问有什么可以帮助你的？',
                    time: new Date().toLocaleTimeString(),
                },
            ]
        }
        // 保存到 store
        persistChatState()
    } catch (error) {
        console.error('初始化问答会话失败:', error)
        // 降级为本地模式，不阻塞用户使用
        chatMessages.value = [
            { role: 'assistant', content: '问答服务暂时不可用，请稍后重试。', time: new Date().toLocaleTimeString() },
        ]
    }
}

/**
 * 将当前问答状态持久化到 store（切换页面时不丢失）
 */
const persistChatState = () => {
    if (!novelId.value) return
    chatStore.saveChatState(novelId.value, chatSessionId.value, chatSessions.value, chatMessages.value)
}

/**
 * 加载会话的历史消息（分页，首次加载最近的消息）
 */
const loadChatHistory = async (sessionId: number) => {
    try {
        const res = await listChatMessagesPage(sessionId, undefined, CHAT_PAGE_SIZE)
        const pageResult = res.data
        const messages = pageResult?.messages || []
        chatHasMore.value = pageResult?.hasMore ?? false
        if (messages.length > 0) {
            chatMessages.value = messages.map((msg) => ({
                id: msg.id,
                role: msg.role,
                content: msg.content,
                time: new Date(msg.createTime).toLocaleTimeString(),
                revoked: msg.revoked,
            }))
        } else {
            chatMessages.value = [
                {
                    role: 'assistant',
                    content: '你好，我是你的 AI 助手，可以帮你回顾小说内容、解答疑问。请问有什么可以帮助你的？',
                    time: new Date().toLocaleTimeString(),
                },
            ]
            chatHasMore.value = false
        }
        persistChatState()
        if (chatMessagesRef.value) {
            chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
        }
        scrollToBottom(false)
    } catch (error) {
        console.error('加载聊天历史失败:', error)
    }
}

/**
 * 加载更早的历史消息（用户向上滚动时触发）
 */
const loadMoreChatMessages = async () => {
    if (chatLoadingMore.value || !chatHasMore.value || !chatSessionId.value) return
    if (chatMessages.value.length === 0) return

    // 获取当前最早消息的 ID 作为游标
    const firstMsg = chatMessages.value[0]
    if (!firstMsg.id) return

    chatLoadingMore.value = true
    try {
        const res = await listChatMessagesPage(chatSessionId.value, firstMsg.id, CHAT_PAGE_SIZE)
        const pageResult = res.data
        const olderMessages = pageResult?.messages || []
        chatHasMore.value = pageResult?.hasMore ?? false

        if (olderMessages.length > 0) {
            // 记录当前滚动位置，加载后恢复（避免跳动）
            const el = chatMessagesRef.value
            const prevScrollHeight = el ? el.scrollHeight : 0

            const mapped = olderMessages.map((msg) => ({
                id: msg.id,
                role: msg.role as 'user' | 'assistant',
                content: msg.content,
                time: new Date(msg.createTime).toLocaleTimeString(),
                revoked: msg.revoked,
            }))
            chatMessages.value.unshift(...mapped)

            // 恢复滚动位置（新消息插入到顶部后，保持用户视角不变）
            nextTick(() => {
                if (el) {
                    el.scrollTop = el.scrollHeight - prevScrollHeight
                }
            })
            persistChatState()
        }
    } catch (error) {
        console.error('加载更多消息失败:', error)
    } finally {
        chatLoadingMore.value = false
    }
}

/**
 * 切换会话（下拉选择器触发）
 */
const handleSwitchSession = async (sessionId: number) => {
    if (!sessionId) return
    chatSessionId.value = sessionId
    await loadChatHistory(sessionId)
}

/**
 * 新建会话
 */
const handleCreateSession = async () => {
    if (!novelId.value) return
    try {
        const sessionRes = await createChatSession(novelId.value, '新对话')
        chatSessionId.value = sessionRes.data.id
        chatSessions.value.unshift(sessionRes.data)
        chatMessages.value = [
            {
                role: 'assistant',
                content: '你好，我是你的 AI 助手，可以帮你回顾小说内容、解答疑问。请问有什么可以帮助你的？',
                time: new Date().toLocaleTimeString(),
            },
        ]
        persistChatState()
    } catch (error) {
        ElMessage.error('创建会话失败')
    }
}

/**
 * 删除当前会话
 */
const handleDeleteSession = async () => {
    if (!chatSessionId.value || chatSessions.value.length <= 1) return
    try {
        await ElMessageBox.confirm('确定删除当前会话？历史消息将无法恢复。', '删除会话', {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'warning',
        })
        const deleteId = chatSessionId.value
        await deleteChatSession(deleteId)
        chatSessions.value = chatSessions.value.filter((s) => s.id !== deleteId)
        // 切换到剩余的第一个会话
        if (chatSessions.value.length > 0) {
            chatSessionId.value = chatSessions.value[0].id
            await loadChatHistory(chatSessionId.value)
        }
        persistChatState()
        ElMessage.success('会话已删除')
    } catch {
        // 用户取消
    }
}

/**
 * 重命名当前会话
 */
const handleRenameSession = async () => {
    if (!chatSessionId.value) return
    const currentSession = chatSessions.value.find((s) => s.id === chatSessionId.value)
    if (!currentSession) return

    try {
        const { value: newTitle } = await ElMessageBox.prompt('请输入新的会话标题', '重命名会话', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputValue: currentSession.title,
            inputPattern: /\S+/,
            inputErrorMessage: '标题不能为空',
        })
        if (newTitle && newTitle.trim()) {
            await renameChatSession(chatSessionId.value, newTitle.trim())
            currentSession.title = newTitle.trim()
            ElMessage.success('会话已重命名')
        }
    } catch {
        // 用户取消
    }
}

/**
 * 切换当前会话的收藏状态
 */
const handleToggleFavorite = async () => {
    if (!chatSessionId.value) return
    const currentSession = chatSessions.value.find((s) => s.id === chatSessionId.value)
    if (!currentSession) return

    try {
        const res = await toggleChatSessionFavorite(chatSessionId.value)
        currentSession.favorite = res.data ? 1 : 0
        ElMessage.success(res.data ? '已收藏' : '已取消收藏')
    } catch {
        ElMessage.error('操作失败')
    }
}

/**
 * 当前会话是否已收藏
 */
const currentSessionFavorite = computed(() => {
    const currentSession = chatSessions.value.find((s) => s.id === chatSessionId.value)
    return currentSession?.favorite === 1
})

/**
 * 排序后的会话列表（收藏的排在前面）
 */
const sortedChatSessions = computed(() => {
    return [...chatSessions.value].sort((a, b) => {
        // 收藏的排在前面
        if (a.favorite === 1 && b.favorite !== 1) return -1
        if (a.favorite !== 1 && b.favorite === 1) return 1
        return 0
    })
})

/**
 * 停止当前 AI 问答流式输出
 * 中断 SSE 流并保存已生成的部分回复
 */
const handleStopChat = async () => {
    if (!chatAbortController) return

    // 获取当前 AI 消息的已生成内容
    const lastMessage = chatMessages.value[chatMessages.value.length - 1]
    const partialContent = lastMessage?.role === 'assistant' ? lastMessage.content : ''

    // 中断 SSE 流
    chatAbortController.abort()
    chatAbortController = null
    isChatLoading.value = false

    // 如果有已生成的内容，调用后端保存接口
    if (partialContent && partialContent.trim()) {
        try {
            await savePartialChatResponse(chatSessionId.value, novelId.value, partialContent)
        } catch {
            console.error('保存中断回复失败')
        }
    }

    scrollToBottom()
    ElMessage.info('已停止回答')
}

const sendChatMessage = async () => {
    if (!chatInput.value.trim() || isChatLoading.value) return

    // 确保有会话
    if (!chatSessionId.value) {
        try {
            const sessionRes = await createChatSession(novelId.value, '默认对话')
            chatSessionId.value = sessionRes.data.id
            chatSessions.value.unshift(sessionRes.data)
        } catch (error) {
            ElMessage.error('创建问答会话失败')
            return
        }
    }

    const userMessage = chatInput.value.trim()
    chatMessages.value.push({
        role: 'user',
        content: userMessage,
        time: new Date().toLocaleTimeString(),
    })

    chatInput.value = ''
    isChatLoading.value = true
    chatThinkingText.value = 'AI 正在思考...'
    scrollToBottom()

    // 预先添加一条空的 AI 消息，用于流式追加内容
    const aiMessageIndex = chatMessages.value.length
    chatMessages.value.push({
        role: 'assistant',
        content: '',
        time: new Date().toLocaleTimeString(),
    })

    chatAbortController = aiChatStream(
        {
            novelId: novelId.value,
            sessionId: chatSessionId.value,
            question: userMessage,
        },
        {
            onToken: (token: string) => {
                chatMessages.value[aiMessageIndex].content += token
                // 收到第一个 token 时更新状态提示
                if (chatMessages.value[aiMessageIndex].content.length === token.length) {
                    chatThinkingText.value = '正在生成回答...'
                }
                // 智能滚动：如果用户在底部附近则自动滚动
                ensureScrollToBottom()
            },
            onComplete: () => {
                isChatLoading.value = false
                chatAbortController = null
                chatThinkingText.value = 'AI 正在思考...'
                // 兜底：如果 AI 消息为空，说明后端异常但未正确传递错误事件
                if (!chatMessages.value[aiMessageIndex].content) {
                    chatMessages.value[aiMessageIndex].content = '抱歉，回答生成失败，请稍后重试。'
                    ElMessage.error('AI 问答异常，请检查模型配置或稍后重试')
                }
                scrollToBottom()
                persistChatState()
            },
            onError: (error: Error) => {
                isChatLoading.value = false
                chatAbortController = null
                chatThinkingText.value = 'AI 正在思考...'
                // 如果 AI 消息为空，显示错误提示
                if (!chatMessages.value[aiMessageIndex].content) {
                    chatMessages.value[aiMessageIndex].content = '抱歉，回答生成失败，请稍后重试。'
                }
                ElMessage.error('AI 问答失败：' + error.message)
                scrollToBottom()
            },
            onToolExecuted: (event: ToolExecutedEvent) => {
                chatThinkingText.value = '正在执行工具：' + event.toolName + '...'
                handleToolExecutedEvent(event)
            },
        },
    )
}

// ==================== 工具执行事件相关常量 ====================

/** 章节相关工具名称 */
const CHAPTER_TOOL_NAMES = ['createChapterWithContent', 'updateChapterContent', 'saveChapterOutline']

/** 大纲相关工具名称 */
const OUTLINE_TOOL_NAMES = ['saveNovelOutline']

/** 人物图谱相关工具名称 */
const CHARACTER_TOOL_NAMES = [
    'createCharacter',
    'updateCharacterBasicInfo',
    'updateCharacterProfile',
    'createCharacterRelation',
]

/**
 * 处理 AI 问答中的工具执行事件。
 *
 * <p>当 Agent 执行创作类或人物类工具后，自动刷新前端对应的数据，
 * 让用户无需手动刷新即可看到最新内容。</p>
 *
 * @param event 工具执行事件数据
 */
const handleToolExecutedEvent = async (event: ToolExecutedEvent) => {
    const { toolName } = event

    if (CHAPTER_TOOL_NAMES.includes(toolName)) {
        // 章节相关工具：刷新章节列表
        await loadChapters()
        // 如果当前有选中的章节，重新加载内容（可能被 AI 更新了）
        if (currentChapterId.value) {
            try {
                const res = await getChapter(currentChapterId.value)
                if (res.data) {
                    chapterContent.value = res.data.content || ''
                    lastSavedContent.value = chapterContent.value
                    lastSyncedContent.value = chapterContent.value
                    hasUnsavedChanges.value = false
                }
                // 如果是大纲工具，同时刷新章节大纲
                if (toolName === 'saveChapterOutline') {
                    await loadChapterOutline(currentChapterId.value)
                }
            } catch (error) {
                console.warn('刷新章节内容失败：', error)
            }
        }
        ElMessage.success('AI 已更新章节内容，编辑器已自动刷新')
    } else if (OUTLINE_TOOL_NAMES.includes(toolName)) {
        // 全文大纲工具：刷新大纲
        await loadNovelOutline()
        ElMessage.success('AI 已更新全文大纲')
    } else if (CHARACTER_TOOL_NAMES.includes(toolName)) {
        // 人物图谱工具：刷新人物数据
        await loadCharacterGraphData()
        ElMessage.success('AI 已更新人物图谱')
    }
}

/**
 * 滚动聊天消息区域到底部
 * 使用平滑滚动效果
 */
const scrollToBottom = (smooth = true) => {
    showScrollToBottomBtn.value = false
    nextTick(() => {
        if (chatMessagesRef.value) {
            const el = chatMessagesRef.value
            if (smooth) {
                el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
            } else {
                // 非平滑模式：直接赋值，在下一帧渲染前再补一次确保到底
                el.scrollTop = el.scrollHeight
                requestAnimationFrame(() => {
                    el.scrollTop = el.scrollHeight
                })
            }
            if (!chatReady.value) {
                chatReady.value = true
            }
        }
    })
}

/**
 * 确保滚动到底部（用于流式输出时强制滚动）。
 *
 * <p>流式输出期间始终自动滚动到底部，除非用户主动向上拖拽。
 * 用户手动拖拽后暂停自动滚动，直到用户回到底部附近。</p>
 */
const ensureScrollToBottom = () => {
    if (!chatMessagesRef.value) return
    const el = chatMessagesRef.value
    const distanceToBottom = el.scrollHeight - el.scrollTop - el.clientHeight
    // 用户在底部 300px 范围内，或正在加载中，都自动滚动
    if (isChatLoading.value || distanceToBottom < 300) {
        el.scrollTop = el.scrollHeight
    }
}

// 当前选中的人物
const selectedCharacter = ref<NovelCharacter | null>(null)
const selectedCharacterRelations = ref<NovelCharacterRelation[]>([])
const showCharacterDetail = ref(false)

// 角色类型映射
const roleTypeMap: Record<string, string> = {
    PROTAGONIST: '主角',
    SUPPORTING: '配角',
    ANTAGONIST: '反派',
    OTHER: '其他',
}

// 性别映射
const genderMap: Record<string, string> = {
    MALE: '男',
    FEMALE: '女',
    OTHER: '其他',
}

/**
 * 获取角色类型中文标签
 */
const getRoleLabel = (roleType?: string): string => {
    return roleTypeMap[roleType || ''] || '其他'
}

/**
 * 加载人物图谱数据（从后端获取）
 */
const loadCharacterGraphData = async () => {
    if (!novelId.value) return
    isLoadingCharacterGraph.value = true
    try {
        const res = await getCharacterGraph(novelId.value)
        characterData.value = res.data.characters || []
        characterRelationData.value = res.data.relations || []
    } catch (error) {
        console.error('加载人物图谱失败:', error)
        characterData.value = []
        characterRelationData.value = []
    } finally {
        isLoadingCharacterGraph.value = false
    }
}

/**
 * 获取人物的关系列表
 */
const getCharacterRelations = (characterId: number): NovelCharacterRelation[] => {
    return characterRelationData.value.filter((r) => r.characterId === characterId || r.targetId === characterId)
}

/**
 * 初始化迷你人物图谱
 */
const initMiniGraph = () => {
    if (!miniGraphRef.value) return

    miniChartInstance = echarts.init(miniGraphRef.value)

    const nodes = characterData.value.map((char) => ({
        id: String(char.id),
        name: char.name,
        symbolSize: char.roleType === 'PROTAGONIST' ? 50 : 40,
        itemStyle: { color: char.color || '#6366f1' },
        label: { show: true, fontSize: 12 },
        // 附带完整人物数据，用于点击查看详情
        characterData: char,
    }))

    // echarts 图谱连线数据（需要去重，避免双向关系重复绘制）
    const linkSet = new Set<string>()
    const links: {
        source: string
        target: string
        value: string
        label: { show: boolean; formatter: string; fontSize: number }
    }[] = []
    characterRelationData.value.forEach((rel) => {
        const linkKey = [rel.characterId, rel.targetId].sort().join('-')
        if (!linkSet.has(linkKey)) {
            linkSet.add(linkKey)
            links.push({
                source: String(rel.characterId),
                target: String(rel.targetId),
                value: rel.relationType,
                label: { show: true, formatter: rel.relationType, fontSize: 10 },
            })
        }
    })

    const option: echarts.EChartsOption = {
        tooltip: {
            trigger: 'item',
            formatter: (params: Record<string, unknown>) => {
                const p = params as { dataType: string; data: { characterData: NovelCharacter; value: string } }
                if (p.dataType === 'node') {
                    const char = p.data.characterData
                    return `<div style="padding: 8px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">${char.name}</div>
                        <div style="color: #666; font-size: 12px;">${getRoleLabel(char.roleType)}</div>
                        ${char.identity ? `<div style="color: #888; font-size: 11px;">${char.identity}</div>` : ''}
                        <div style="color: #999; font-size: 11px; margin-top: 4px;">点击查看详情</div>
                    </div>`
                }
                return `${p.data.value}`
            },
        },
        series: [
            {
                type: 'graph',
                layout: 'force',
                data: nodes,
                links: links,
                roam: true,
                label: { position: 'bottom' },
                force: { repulsion: 200, edgeLength: 100 },
                lineStyle: { color: 'source', curveness: 0.2 },
            },
        ],
    }

    miniChartInstance.setOption(option)

    // 点击节点展示人物详情
    miniChartInstance.on('click', (params: Record<string, unknown>) => {
        const p = params as { dataType: string; data: { characterData: NovelCharacter } }
        if (p.dataType === 'node') {
            selectedCharacter.value = p.data.characterData
            selectedCharacterRelations.value = getCharacterRelations(p.data.characterData.id)
            showCharacterDetail.value = true
        }
    })
}

// 监听右侧功能 Tab 切换，按需初始化对应面板
watch(activeFunctionTab, (val) => {
    if (val === 'character') {
        // 先加载后端数据，再初始化图谱
        loadCharacterGraphData().then(() => {
            nextTick(() => {
                initMiniGraph()
            })
        })
    } else if (val === 'history') {
        loadHistoryVersions()
    } else if (val === 'chat') {
        // 切换到问答面板时，先隐藏消息区域，初始化后再显示
        chatReady.value = false
        initChatSession().then(() => {
            nextTick(() => {
                setTimeout(() => scrollToBottom(false), 50)
            })
        })
    } else if (val === 'outline') {
        // 大纲面板切入时，确保大纲数据已加载
        if (!novelOutline.value) {
            loadNovelOutline()
        }
    } else if (val === 'settings') {
        // 切换到设置面板时，加载 Agent 列表和配置
        loadSettingsPanel()
    } else if (val === 'memo') {
        // 切换到创作手记时加载数据
        loadMemo()
    }
})

/**
 * 切换右侧功能面板 Tab（再次点击同一图标则收起面板）
 */
const switchFunctionTab = (tab: 'outline' | 'character' | 'history' | 'chat' | 'annotations' | 'memo' | 'settings') => {
    if (activeFunctionTab.value === tab) {
        activeFunctionTab.value = null
    } else {
        activeFunctionTab.value = tab
    }
}

/**
 * 开始拖拽调整右侧功能面板宽度
 */
const startResizeFunctionPanel = (event: MouseEvent) => {
    event.preventDefault()
    const startX = event.clientX
    const startWidth = functionPanelWidth.value

    const onMouseMove = (moveEvent: MouseEvent) => {
        // 向左拖拽增大宽度，向右拖拽减小宽度
        const deltaX = startX - moveEvent.clientX
        const newWidth = Math.min(FUNCTION_PANEL_MAX_WIDTH, Math.max(FUNCTION_PANEL_MIN_WIDTH, startWidth + deltaX))
        functionPanelWidth.value = newWidth
    }

    const onMouseUp = () => {
        document.removeEventListener('mousemove', onMouseMove)
        document.removeEventListener('mouseup', onMouseUp)
        document.body.style.cursor = ''
        document.body.style.userSelect = ''
    }

    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'
    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
}

const publishChapter = async () => {
    if (!currentChapterId.value) {
        ElMessage.warning('请先选择章节')
        return
    }

    // 根据配置动态生成发布提示信息
    const autoOutline = novelSettings.value.autoOutlineAfterPublish === 1
    const autoVector = novelSettings.value.autoVectorAfterPublish === 1
    let publishTip = '确定要发布本章吗？发布后内容将对读者可见。'
    if (autoOutline && autoVector) {
        publishTip = '确定要发布本章吗？AI 将自动总结章节大纲并存储到向量数据库。'
    } else if (autoOutline) {
        publishTip = '确定要发布本章吗？AI 将自动总结章节大纲。'
    } else if (autoVector) {
        publishTip = '确定要发布本章吗？同时章节内容将存储到向量数据库。'
    }

    ElMessageBox.confirm(publishTip, '发布确认', {
        confirmButtonText: '确认发布',
        cancelButtonText: '取消',
        type: 'info',
    }).then(async () => {
        try {
            const res = await publishChapterApi(currentChapterId.value)
            const chapter = chapters.value.find((c) => c.id === currentChapterId.value)
            if (chapter) {
                chapter.status = 'PUBLISHED'
                chapter.published = true
                chapter.needRepublish = 0
            }
            ElMessage.success(res.data?.message || '章节发布成功')

            // 发布后刷新小说总字数
            loadNovelInfo()

            // 发布后开始轮询 AI 大纲
            startPollingAiOutline(currentChapterId.value)
        } catch (error) {
            ElMessage.error('发布章节失败')
        }
    })
}

// 加载章节大纲
const loadChapterOutline = async (chapterId: number) => {
    try {
        const res = await getChapterOutline(chapterId)
        if (res.data) {
            chapterOutline.value = res.data.outlineContent || ''
            plotPointsText.value = res.data.plotPoints || ''
            emotionTone.value = res.data.emotionTone || ''
            sceneSetting.value = res.data.sceneSetting || ''
            // 解析涉及的人物
            if (res.data.involvedCharacters) {
                try {
                    const characters = JSON.parse(res.data.involvedCharacters)
                    selectedCharacters.value = Array.isArray(characters) ? characters : []
                } catch {
                    selectedCharacters.value = []
                }
            } else {
                selectedCharacters.value = []
            }
            // 记录大纲已保存状态，用于变更检测
            lastSavedOutline.value = {
                chapterOutline: chapterOutline.value,
                plotPointsText: plotPointsText.value,
                emotionTone: emotionTone.value,
                sceneSetting: sceneSetting.value,
                selectedCharacters: [...selectedCharacters.value],
            }
            // 初始化同步状态
            lastSyncedOutline.value = { ...lastSavedOutline.value }
            outlineLastChangeTime.value = 0
            hasOutlineUnsavedChanges.value = false
        } else {
            // 没有大纲数据，重置所有大纲字段
            resetChapterOutline()
        }
    } catch (error) {
        // 大纲可能不存在，不显示错误，重置所有大纲字段
        console.log('章节大纲不存在或加载失败')
        resetChapterOutline()
    }
}

// 重置章节大纲字段
const resetChapterOutline = () => {
    chapterOutline.value = ''
    plotPointsText.value = ''
    emotionTone.value = ''
    sceneSetting.value = ''
    selectedCharacters.value = []
    // 记录大纲已保存状态，用于变更检测
    lastSavedOutline.value = {
        chapterOutline: '',
        plotPointsText: '',
        emotionTone: '',
        sceneSetting: '',
        selectedCharacters: [],
    }
    // 初始化同步状态
    lastSyncedOutline.value = { ...lastSavedOutline.value }
    outlineLastChangeTime.value = 0
    hasOutlineUnsavedChanges.value = false
}

// AI 大纲相关状态
const aiOutlineContent = ref('')
const hasAiOutline = ref(false)
const showAiOutlineDialog = ref(false)
let aiOutlinePollingTimer: ReturnType<typeof setTimeout> | null = null

/** AI 大纲轮询最大次数 */
const AI_OUTLINE_POLL_MAX = 30

/** AI 大纲轮询间隔（毫秒） */
const AI_OUTLINE_POLL_INTERVAL = 3000

/**
 * 发布后开始轮询 AI 大纲生成结果
 */
const startPollingAiOutline = (chapterId: number) => {
    let pollCount = 0
    const poll = async () => {
        if (pollCount >= AI_OUTLINE_POLL_MAX) {
            stopPollingAiOutline()
            return
        }
        pollCount++
        try {
            const res = await getAiOutline(chapterId)
            if (res.data && res.data.hasAiOutline) {
                aiOutlineContent.value = res.data.aiOutlineContent
                hasAiOutline.value = true
                stopPollingAiOutline()
                // 弹出 AI 大纲展示弹窗
                showAiOutlineDialog.value = true
                ElMessage.success('AI 大纲生成完成')
                return
            }
        } catch (error) {
            console.log('轮询 AI 大纲失败，继续重试')
        }
        aiOutlinePollingTimer = setTimeout(poll, AI_OUTLINE_POLL_INTERVAL)
    }
    // 延迟 3 秒后开始首次轮询（给 AI 处理时间）
    aiOutlinePollingTimer = setTimeout(poll, AI_OUTLINE_POLL_INTERVAL)
}

/**
 * 停止轮询 AI 大纲
 */
const stopPollingAiOutline = () => {
    if (aiOutlinePollingTimer) {
        clearTimeout(aiOutlinePollingTimer)
        aiOutlinePollingTimer = null
    }
}

/**
 * 用 AI 大纲替换用户大纲
 */
const handleReplaceOutline = async () => {
    if (!currentChapterId.value) return
    try {
        await replaceOutlineWithAi(currentChapterId.value)
        chapterOutline.value = aiOutlineContent.value
        showAiOutlineDialog.value = false
        ElMessage.success('已用 AI 大纲替换当前大纲')
        // 刷新大纲数据
        await loadChapterOutline(currentChapterId.value)
    } catch (error) {
        ElMessage.error('替换大纲失败')
    }
}

// 加载历史版本
const loadHistoryVersions = async () => {
    if (!currentChapterId.value) {
        ElMessage.warning('请先选择章节')
        return
    }

    isLoadingVersions.value = true
    try {
        const res = await getChapterVersions(currentChapterId.value)
        historyVersions.value = res.data || []
    } catch (error) {
        ElMessage.error('加载历史版本失败')
    } finally {
        isLoadingVersions.value = false
    }
}

// 预览版本
const previewVersion = (version: ChapterVersion) => {
    previewVersionId.value = version.id
    previewVersionContent.value = version.content || ''
}

// 恢复版本
const restoreVersion = async (versionId: number) => {
    if (!currentChapterId.value) return

    ElMessageBox.confirm('恢复此版本将覆盖当前内容，是否继续？', '恢复确认', {
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        type: 'warning',
    }).then(async () => {
        try {
            const res = await restoreChapterVersion(currentChapterId.value, versionId)
            if (res.data) {
                chapterContent.value = res.data.content || ''
                lastSavedContent.value = chapterContent.value
                hasUnsavedChanges.value = false
                ElMessage.success('版本恢复成功')
                previewVersionId.value = null
                previewVersionContent.value = ''
            }
        } catch (error) {
            ElMessage.error('版本恢复失败')
        }
    })
}

// 打开版本备注弹窗
const openRemarkDialog = (version: ChapterVersion) => {
    editingVersion.value = version
    editingRemark.value = version.remark || ''
    showRemarkDialog.value = true
}

// 保存版本备注
const saveVersionRemark = async () => {
    if (!currentChapterId.value || !editingVersion.value) return

    isSavingRemark.value = true
    try {
        await updateVersionRemark(currentChapterId.value, editingVersion.value.id, editingRemark.value)
        // 更新本地数据
        editingVersion.value.remark = editingRemark.value
        ElMessage.success('备注保存成功')
        showRemarkDialog.value = false
    } catch (error) {
        ElMessage.error('备注保存失败')
    } finally {
        isSavingRemark.value = false
    }
}

// 手动保存
const manualSave = async () => {
    if (!currentChapterId.value) {
        ElMessage.warning('请先选择章节')
        return
    }

    isSaving.value = true
    try {
        // 保存章节内容
        await updateChapterApi(currentChapterId.value, {
            title: chapterTitle.value.replace(/^第\d+章\s*/, ''),
            content: chapterContent.value,
        })

        lastSavedContent.value = chapterContent.value
        lastSyncedContent.value = chapterContent.value
        hasUnsavedChanges.value = false
        contentLastChangeTime.value = 0
        clearDraft()
        ElMessage.success('保存成功')

        // 刷新章节列表以更新时间
        await loadChapters()
    } catch (error) {
        ElMessage.error('保存失败')
    } finally {
        isSaving.value = false
    }
}

// 自动保存定时器
let draftSaveTimer: ReturnType<typeof setTimeout>
let backendAutoSaveTimer: ReturnType<typeof setTimeout>
let outlineDraftSaveTimer: ReturnType<typeof setTimeout>
let outlineBackendSaveTimer: ReturnType<typeof setTimeout>

// 智能同步保存相关状态
const lastSyncedContent = ref('') // 上次同步到后端的内容
const lastSyncedOutline = ref({
    chapterOutline: '',
    plotPointsText: '',
    emotionTone: '',
    sceneSetting: '',
    selectedCharacters: [] as number[],
})
const contentSyncInterval = ref<number | null>(null) // 内容同步循环定时器
const outlineSyncInterval = ref<number | null>(null) // 大纲同步循环定时器
const contentLastChangeTime = ref<number>(0) // 内容最后变化时间
const outlineLastChangeTime = ref<number>(0) // 大纲最后变化时间

/** 同步间隔（毫秒）：15秒 */
const SYNC_INTERVAL = 15000
/** 最大无变化时间（毫秒）：1分钟 */
const MAX_NO_CHANGE_TIME = 60000
/** 本地草稿防抖时间（毫秒）：1秒 */
const DRAFT_DEBOUNCE_TIME = 1000

// 保存草稿到本地（高频）
const _performDraftSave = () => {
    if (!hasUnsavedChanges.value || !currentChapterId.value) return
    saveDraftToLocal()
}

/**
 * 检查内容是否有实际变化（与上次同步的内容对比）
 */
const hasContentActuallyChanged = (): boolean => {
    return chapterContent.value !== lastSyncedContent.value
}

/**
 * 检查大纲是否有实际变化（与上次同步的大纲对比）
 */
const hasOutlineActuallyChanged = (): boolean => {
    return (
        chapterOutline.value !== lastSyncedOutline.value.chapterOutline ||
        plotPointsText.value !== lastSyncedOutline.value.plotPointsText ||
        emotionTone.value !== lastSyncedOutline.value.emotionTone ||
        sceneSetting.value !== lastSyncedOutline.value.sceneSetting ||
        JSON.stringify(selectedCharacters.value) !== JSON.stringify(lastSyncedOutline.value.selectedCharacters)
    )
}

/**
 * 执行内容同步到后端
 * 如果内容无变化则跳过，返回是否执行了同步
 */
const performContentSync = async (): Promise<boolean> => {
    if (!currentChapterId.value) return false

    // 检查内容是否有实际变化
    if (!hasContentActuallyChanged()) {
        console.log('内容无变化，跳过同步')
        return false
    }

    isSaving.value = true
    try {
        await updateChapterApi(currentChapterId.value, {
            title: chapterTitle.value.replace(/^第\d+章\s*/, ''),
            content: chapterContent.value,
        })

        lastSavedContent.value = chapterContent.value
        lastSyncedContent.value = chapterContent.value
        hasUnsavedChanges.value = false
        clearDraft() // 保存成功后清除本地草稿
        console.log('内容同步成功:', new Date().toLocaleTimeString())
        return true
    } catch (error) {
        console.error('内容同步失败:', error)
        return false
    } finally {
        isSaving.value = false
    }
}

/**
 * 执行大纲同步到后端
 * 如果大纲无变化则跳过，返回是否执行了同步
 */
const performOutlineSync = async (): Promise<boolean> => {
    if (!currentChapterId.value) return false

    // 检查大纲是否有实际变化
    if (!hasOutlineActuallyChanged()) {
        console.log('大纲无变化，跳过同步')
        return false
    }

    isOutlineSaving.value = true
    try {
        await saveChapterOutline({
            novelId: novelId.value,
            chapterId: currentChapterId.value,
            outlineContent: chapterOutline.value,
            plotPoints: plotPointsText.value,
            emotionTone: emotionTone.value,
            sceneSetting: sceneSetting.value,
            involvedCharacters: JSON.stringify(selectedCharacters.value),
        })

        // 更新已同步状态
        lastSavedOutline.value = {
            chapterOutline: chapterOutline.value,
            plotPointsText: plotPointsText.value,
            emotionTone: emotionTone.value,
            sceneSetting: sceneSetting.value,
            selectedCharacters: [...selectedCharacters.value],
        }
        lastSyncedOutline.value = { ...lastSavedOutline.value }
        hasOutlineUnsavedChanges.value = false
        console.log('大纲同步成功:', new Date().toLocaleTimeString())
        return true
    } catch (error) {
        console.error('大纲同步失败:', error)
        return false
    } finally {
        isOutlineSaving.value = false
    }
}

/**
 * 启动内容同步循环
 * 每15秒检查一次，如果1分钟无变化则停止
 */
const startContentSyncLoop = () => {
    // 自动保存内容关闭时不启动同步循环
    if (novelSettings.value.autoSaveContent === 0) return
    // 如果循环已在运行，不重复启动
    if (contentSyncInterval.value) return

    console.log('启动内容同步循环')
    contentSyncInterval.value = window.setInterval(async () => {
        const now = Date.now()
        const timeSinceLastChange = now - contentLastChangeTime.value

        // 如果超过1分钟无变化，停止循环
        if (timeSinceLastChange >= MAX_NO_CHANGE_TIME) {
            console.log('超过1分钟无变化，停止内容同步循环')
            stopContentSyncLoop()
            return
        }

        // 执行同步
        await performContentSync()
    }, SYNC_INTERVAL)
}

/**
 * 停止内容同步循环
 */
const stopContentSyncLoop = () => {
    if (contentSyncInterval.value) {
        clearInterval(contentSyncInterval.value)
        contentSyncInterval.value = null
        console.log('内容同步循环已停止')
    }
}

/**
 * 启动大纲同步循环
 * 每15秒检查一次，如果1分钟无变化则停止
 */
const startOutlineSyncLoop = () => {
    // 如果循环已在运行，不重复启动
    if (outlineSyncInterval.value) return

    console.log('启动大纲同步循环')
    outlineSyncInterval.value = window.setInterval(async () => {
        const now = Date.now()
        const timeSinceLastChange = now - outlineLastChangeTime.value

        // 如果超过1分钟无变化，停止循环
        if (timeSinceLastChange >= MAX_NO_CHANGE_TIME) {
            console.log('超过1分钟无变化，停止大纲同步循环')
            stopOutlineSyncLoop()
            return
        }

        // 执行同步
        await performOutlineSync()
    }, SYNC_INTERVAL)
}

/**
 * 停止大纲同步循环
 */
const stopOutlineSyncLoop = () => {
    if (outlineSyncInterval.value) {
        clearInterval(outlineSyncInterval.value)
        outlineSyncInterval.value = null
        console.log('大纲同步循环已停止')
    }
}

// 自动保存到后端（低频）- 保留用于兼容，实际逻辑已迁移到 performContentSync
const _performAutoSave = async () => {
    await performContentSync()
}

// 大纲草稿保存到本地（高频，复用 saveDraftToLocal）
const _performOutlineDraftSave = () => {
    if (!hasOutlineUnsavedChanges.value || !currentChapterId.value) return
    saveDraftToLocal()
}

// 大纲自动保存到后端（低频）- 保留用于兼容，实际逻辑已迁移到 performOutlineSync
const _performOutlineAutoSave = async () => {
    await performOutlineSync()
}

/**
 * 检测大纲字段是否有变更
 */
const checkOutlineChanged = (): boolean => {
    return (
        chapterOutline.value !== lastSavedOutline.value.chapterOutline ||
        plotPointsText.value !== lastSavedOutline.value.plotPointsText ||
        emotionTone.value !== lastSavedOutline.value.emotionTone ||
        sceneSetting.value !== lastSavedOutline.value.sceneSetting ||
        JSON.stringify(selectedCharacters.value) !== JSON.stringify(lastSavedOutline.value.selectedCharacters)
    )
}

/**
 * 大纲字段变更时触发智能同步
 * 如果大纲有变化，立即启动同步循环
 */
const handleOutlineChange = () => {
    hasOutlineUnsavedChanges.value = checkOutlineChanged()

    // 清除之前的大纲保存定时器
    clearTimeout(outlineDraftSaveTimer)

    if (hasOutlineUnsavedChanges.value) {
        // 记录最后变化时间
        outlineLastChangeTime.value = Date.now()

        // 防抖保存到本地草稿（1秒后保存，避免频繁写入）
        outlineDraftSaveTimer = setTimeout(() => {
            saveDraftToLocal()
        }, DRAFT_DEBOUNCE_TIME)

        // 启动同步循环（如果未运行）
        startOutlineSyncLoop()
    }
}

// 保存小说大纲
const saveNovelOutlineHandler = async () => {
    try {
        // 将编辑内容同步到 novelOutline
        novelOutline.value = editingNovelOutlineContent.value
        await saveNovelOutline(novelId.value, novelOutline.value)
        isEditingNovelOutline.value = false
        ElMessage.success('大纲保存成功')
    } catch (error) {
        ElMessage.error('大纲保存失败')
    }
}

/**
 * 进入大纲编辑模式
 */
const startEditNovelOutline = () => {
    editingNovelOutlineContent.value = novelOutline.value
    isEditingNovelOutline.value = true
}

/**
 * 取消大纲编辑
 */
const cancelEditNovelOutline = () => {
    isEditingNovelOutline.value = false
    editingNovelOutlineContent.value = ''
}

// 监听正文内容变化，智能同步
watch(chapterContent, (newVal) => {
    hasUnsavedChanges.value = newVal !== lastSavedContent.value

    // 清除之前的定时器
    clearTimeout(draftSaveTimer)

    if (hasUnsavedChanges.value) {
        // 记录最后变化时间
        contentLastChangeTime.value = Date.now()

        // 防抖保存到本地草稿（1秒后保存，避免频繁写入）
        draftSaveTimer = setTimeout(() => {
            saveDraftToLocal()
        }, DRAFT_DEBOUNCE_TIME)

        // AI 改写过程中不启动自动同步，等改写完成后统一保存
        if (!isAiProcessing.value) {
            startContentSyncLoop()
        }
    }
})

// 监听大纲相关字段变化，分层自动保存
watch(chapterOutline, handleOutlineChange)
watch(plotPointsText, handleOutlineChange)
watch(emotionTone, handleOutlineChange)
watch(sceneSetting, handleOutlineChange)
watch(selectedCharacters, handleOutlineChange, { deep: true })

/**
 * 页面刷新/关闭前的处理
 * 如果正在流式输出，中断请求（后端 doOnCancel 会自动保存已生成内容）
 */
const handleBeforeUnload = () => {
    if (chatAbortController) {
        chatAbortController.abort()
        chatAbortController = null
    }
}

// 页面加载时恢复草稿
onMounted(() => {
    loadNovelInfo()
    loadChapters()
    loadNovelOutline()
    loadNovelSettings()
    window.addEventListener('beforeunload', handleBeforeUnload)

    // 根据缓存的工具栏 Tab 初始化对应面板数据
    const tab = activeFunctionTab.value
    if (tab === 'character') {
        loadCharacterGraphData().then(() => nextTick(() => initMiniGraph()))
    } else if (tab === 'history') {
        loadHistoryVersions()
    } else if (tab === 'chat') {
        chatReady.value = false
        initChatSession().then(() => {
            nextTick(() => setTimeout(() => scrollToBottom(false), 50))
        })
    } else if (tab === 'settings') {
        loadSettingsPanel()
    }
})

// 组件卸载时清理定时器
onUnmounted(() => {
    stopContentSyncLoop()
    stopOutlineSyncLoop()
    clearTimeout(draftSaveTimer)
    clearTimeout(backendAutoSaveTimer)
    clearTimeout(outlineDraftSaveTimer)
    clearTimeout(outlineBackendSaveTimer)
    window.removeEventListener('beforeunload', handleBeforeUnload)
    // 组件卸载时也中断正在进行的流式请求
    if (chatAbortController) {
        chatAbortController.abort()
        chatAbortController = null
    }
})

// 从 keep-alive 缓存恢复时，重新初始化 echarts 图表尺寸
onActivated(() => {
    if (miniChartInstance) {
        nextTick(() => {
            miniChartInstance?.resize()
        })
    }
})

/**
 * 加载小说有效配置（用于控制自动审核等行为）
 */
const loadNovelSettings = async () => {
    try {
        const res = await getEffectiveSettings(novelId.value)
        if (res.data) {
            novelSettings.value = res.data
        }
    } catch (error) {
        console.error('加载小说配置失败，使用默认配置', error)
    }
}

/**
 * 判断是否为内置 Agent
 */
const isBuiltinAgent = (agent: AgentConfig): boolean => {
    return agent.isBuiltin === 1
}

/**
 * 用户自建的 Agent 列表（过滤掉内置模板，仅用户自建 Agent 可被绑定使用）
 */
const userAgentList = computed(() => {
    return agents.value.filter((a) => !isBuiltinAgent(a))
})

/**
 * 加载设置面板数据（Agent 列表 + Agent 配置 + 小说设置）
 */
const loadSettingsPanel = async () => {
    isLoadingSettings.value = true
    try {
        // 并行加载 Agent 列表、Agent 配置和小说级别配置
        const [agentList, agentRes, settingsRes] = await Promise.all([
            getAvailableAgentList(),
            getNovelAgentConfig(novelId.value),
            getNovelLevelSettings(novelId.value),
        ])
        agents.value = agentList
        if (agentRes.data) {
            agentConfig.value = agentRes.data
        }
        if (settingsRes.data) {
            // 小说有自身配置，直接使用
            novelSettingsForm.value = settingsRes.data
            hasOwnSettings.value = true
        } else {
            // 小说没有自身配置，加载有效配置（合并后的结果）展示
            hasOwnSettings.value = false
            try {
                const effectiveRes = await getEffectiveSettings(novelId.value)
                novelSettingsForm.value = effectiveRes.data
            } catch {
                novelSettingsForm.value = createDefaultSettings()
            }
        }
    } catch (error) {
        console.error('加载设置面板数据失败:', error)
        ElMessage.error('加载设置失败')
    } finally {
        isLoadingSettings.value = false
    }
}

/**
 * 保存设置面板数据（Agent 配置 + AI 功能配置）
 */
const saveSettingsPanel = async () => {
    isSavingSettings.value = true
    try {
        const agentRequest: NovelAgentConfigRequest = {
            authorAgentId: agentConfig.value.authorAgentId,
            editorAgentId: agentConfig.value.editorAgentId,
            qaAgentId: agentConfig.value.qaAgentId,
        }
        // 并行保存 Agent 配置和小说配置
        await Promise.all([
            saveNovelAgentConfig(novelId.value, agentRequest),
            saveNovelSettings(novelId.value, novelSettingsForm.value),
        ])
        // 保存成功后同步更新运行时配置
        novelSettings.value = { ...novelSettingsForm.value }
        hasOwnSettings.value = true
        ElMessage.success('设置保存成功')
    } catch (error) {
        console.error('保存设置失败:', error)
        ElMessage.error('保存设置失败')
    } finally {
        isSavingSettings.value = false
    }
}

// 页面关闭前保存草稿（含正文和大纲）
window.addEventListener('beforeunload', () => {
    if (currentChapterId.value && (hasUnsavedChanges.value || hasOutlineUnsavedChanges.value)) {
        saveDraftToLocal()
    }
})

/**
 * 监听编辑器失焦事件，失焦时立即保存草稿
 */
const handleEditorBlur = () => {
    if (currentChapterId.value && (hasUnsavedChanges.value || hasOutlineUnsavedChanges.value)) {
        // 清除防抖定时器，立即保存
        clearTimeout(draftSaveTimer)
        clearTimeout(outlineDraftSaveTimer)
        saveDraftToLocal()
        console.log('编辑器失焦，立即保存草稿')
    }
}
</script>

<style scoped>
.novel-editor-page {
    height: calc(100vh - 64px);
    display: flex;
    flex-direction: column;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
    position: fixed;
    top: 64px;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    font-size: 15px;
    overflow: hidden;
}

/* 顶部标题栏 */
.editor-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 24px;
    background: #fff;
    border-bottom: 1px solid #e8eaed;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    gap: 16px;
}

/* 左侧：返回 + 章节标题 */
.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    min-width: 0;
    overflow: hidden;
}

/* 中间：小说名称 + 统计徽章 */
.header-center {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
}

.novel-title-badge {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    max-width: 260px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.novel-word-count-badge,
.novel-chapter-count-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 14px;
    border-radius: 14px;
    font-size: 13px;
    font-weight: 500;
    white-space: nowrap;
}

.novel-word-count-badge {
    background: rgba(26, 115, 232, 0.08);
    color: #1a73e8;
}

.novel-chapter-count-badge {
    background: rgba(102, 126, 234, 0.08);
    color: #667eea;
}

/* 右侧：操作按钮 */
.header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
}

.back-btn {
    font-size: 16px;
    color: #1a73e8;
    font-weight: 600;
    background: transparent !important;
    border: none !important;
    padding: 8px 12px;
}

.back-btn:hover {
    color: #1557b0;
    background: transparent !important;
}

.chapter-title-input {
    width: 320px;
    max-width: 30vw;
    min-width: 140px;
}

.chapter-title-input :deep(.el-input__wrapper) {
    box-shadow: none;
    background: #f8f9fa;
    border-radius: 8px;
    padding: 8px 16px;
}

.chapter-title-input :deep(.el-input__inner) {
    font-size: 14px;
    font-weight: 600;
    color: #202124;
}

.focus-mode-btn {
    font-weight: 500;
    transition: all 0.3s ease;
}

.focus-mode-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 编辑器主体 */
.editor-body {
    flex: 1;
    display: flex;
    overflow: hidden;
    padding: 20px 24px 24px;
    gap: 4px;
    min-width: 0;
}

/* 左侧边栏 */
.editor-sidebar {
    width: 280px;
    min-width: 48px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 12px;
    display: flex;
    flex-direction: row;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8eaed;
    transition: width 0.3s ease;
}

/* 侧边栏主内容区（包裹 header + content） */
.sidebar-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;
}

/* 左侧边栏收起状态 */
.editor-sidebar.collapsed {
    width: 52px;
}

.sidebar-header-actions {
    display: flex;
    align-items: center;
    gap: 2px;
}

/* 收起状态下的展开按钮 */
.sidebar-expand-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    cursor: pointer;
    color: #9aa0a6;
    transition: all 0.2s;
    margin: 0 auto;
}

.sidebar-expand-btn:hover {
    color: #1a73e8;
    background: rgba(26, 115, 232, 0.08);
}

/* 收起状态下的整体容器 */
.sidebar-collapsed-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    height: 100%;
    cursor: pointer;
    padding: 4px 0 12px;
    gap: 0;
}

.sidebar-collapsed-content:hover .sidebar-expand-btn {
    color: #1a73e8;
    background: rgba(26, 115, 232, 0.08);
}

/* 收起状态下的章节信息区 */
.collapsed-chapter-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 10px 0 8px;
    width: 100%;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 8px;
}

/* 发布状态指示点 */
.collapsed-status-dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    flex-shrink: 0;
}

.collapsed-status-dot.dot-published {
    background: #34a853;
}

.collapsed-status-dot.dot-need-republish {
    background: #fbbc04;
}

.collapsed-status-dot.dot-draft {
    background: #9aa0a6;
}

/* 竖排章节序号 */
.collapsed-chapter-number {
    writing-mode: vertical-rl;
    text-orientation: mixed;
    font-size: 11px;
    font-weight: 600;
    color: #1a73e8;
    letter-spacing: 1px;
    line-height: 1.2;
    max-height: 60px;
    overflow: hidden;
}

/* 竖排字数 */
.collapsed-word-count {
    writing-mode: vertical-rl;
    text-orientation: mixed;
    font-size: 10px;
    color: #9aa0a6;
    letter-spacing: 0.5px;
    line-height: 1.2;
}

/* 收起状态下的总章节数 */
.collapsed-total {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1px;
    margin-top: auto;
    padding-bottom: 4px;
}

.collapsed-total-num {
    font-size: 13px;
    font-weight: 700;
    color: #5f6368;
    line-height: 1;
}

.collapsed-total-label {
    font-size: 10px;
    color: #9aa0a6;
    line-height: 1;
}

.sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    height: 56px;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(90deg, #fafbfc 0%, #f5f7fa 100%);
}

/* 收起状态下 header 撑满整个侧边栏，作为点击区域 */
.editor-sidebar.collapsed .sidebar-header {
    height: 100%;
    padding: 8px 0;
    border-bottom: none;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
    overflow: hidden;
}

.sidebar-title {
    font-size: 15px;
    font-weight: 600;
    color: #202124;
    display: flex;
    align-items: center;
    gap: 8px;
}

.sidebar-title .el-icon {
    color: #1a73e8;
    font-size: 18px;
}

.tab-item:hover {
    color: #1a73e8;
}

.tab-item.active {
    color: #1a73e8;
    border-bottom: 2px solid #1a73e8;
}

.sidebar-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px 0;
}

.sidebar-content::-webkit-scrollbar {
    width: 6px;
}

.sidebar-content::-webkit-scrollbar-track {
    background: transparent;
}

.sidebar-content::-webkit-scrollbar-thumb {
    background: #dadce0;
    border-radius: 3px;
}

/* 章节工具栏 */
.chapter-toolbar {
    padding: 12px 16px;
    border-bottom: 1px solid #e8eaed;
    background: #fafbfc;
}

.toolbar-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
}

.toolbar-row:last-child {
    margin-bottom: 0;
}

.search-input {
    flex: 1;
}

.filter-select {
    flex: 1;
}

.list-summary {
    padding: 10px 16px;
    font-size: 13px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.quick-jump-rail {
    width: 30px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-left: 1px solid #ebeef5;
    background: linear-gradient(180deg, #fafbfc 0%, #f5f7fa 100%);
    padding: 8px 0;
    overflow: hidden;
    gap: 4px;
}

.quick-jump-arrow {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 22px;
    color: #606266;
    cursor: pointer;
    flex-shrink: 0;
    border-radius: 4px;
    transition: all 0.2s;
    background: #e8eaed;
}

.quick-jump-arrow:hover {
    color: #fff;
    background: #409eff;
}

.quick-jump-list {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1px;
    max-height: 110px;
    overflow-y: auto;
    scrollbar-width: none;
    padding: 2px 0;
}

.quick-jump-list::-webkit-scrollbar {
    display: none;
}

.quick-jump-rail-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 20px;
    font-size: 10px;
    color: #909399;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
    user-select: none;
    flex-shrink: 0;
    font-weight: 500;
}

.quick-jump-rail-btn:hover {
    color: #fff;
    background: #409eff;
    box-shadow: 0 1px 4px rgba(64, 158, 255, 0.3);
}

.quick-jump-rail-btn.active {
    color: #fff;
    background: #409eff;
    box-shadow: 0 1px 4px rgba(64, 158, 255, 0.3);
}

.loading-more {
    font-size: 12px;
    color: #909399;
    color: #9aa0a6;
    font-weight: 500;
}

.chapter-item {
    cursor: pointer;
    transition: all 0.2s;
    border-left: 3px solid transparent;
    margin: 8px 12px;
    border-radius: 12px;
    background: linear-gradient(135deg, #f8f9fa 0%, #f0f4f8 100%);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8eaed;
    overflow: hidden;
}

.chapter-item:hover {
    background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%);
    transform: translateX(3px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: #d0d7de;
}

.chapter-item.active {
    background: linear-gradient(135deg, #e8f0fe 0%, #d4e4ff 100%);
    border-left-color: #1a73e8;
    border-color: #aecbfa;
    box-shadow: 0 4px 12px rgba(26, 115, 232, 0.2);
}

/* 拖拽排序样式 */
.drag-handle {
    cursor: grab;
    color: #c0c4cc;
    margin-right: 6px;
    transition: color 0.2s;
}

.drag-handle:hover {
    color: #1a73e8;
}

.drag-handle:active {
    cursor: grabbing;
}

.chapter-drag-ghost {
    opacity: 0.4;
    background: #e8f0fe;
}

.chapter-drag-chosen {
    box-shadow: 0 6px 20px rgba(26, 115, 232, 0.3);
    border-color: #1a73e8;
}

.chapter-content {
    display: flex;
    flex-direction: column;
    padding: 12px 16px;
}

.chapter-header-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
}

.chapter-title-section {
    display: flex;
    align-items: center;
    gap: 6px;
    flex: 1;
    min-width: 0;
}

.chapter-number {
    font-size: 13px;
    color: #5f6368;
    font-weight: 600;
    background: rgba(26, 115, 232, 0.1);
    padding: 2px 8px;
    border-radius: 4px;
    white-space: nowrap;
    flex-shrink: 0;
}

.chapter-name {
    font-size: 15px;
    color: #1a73e8;
    font-weight: 600;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    letter-spacing: 0.3px;
    margin-bottom: 8px;
}

.published-icon {
    color: #34a853;
    flex-shrink: 0;
}

/* 章节状态标志行 */
.chapter-status-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
    flex-wrap: wrap;
}

.status-badge {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 500;
    line-height: 1;
    cursor: default;
    transition: all 0.2s;
    white-space: nowrap;
}

.status-text {
    line-height: 1.2;
}

/* 草稿状态 */
.status-draft {
    background: rgba(158, 158, 158, 0.12);
    color: #9e9e9e;
}

/* 已发布状态 */
.status-published {
    background: rgba(52, 168, 83, 0.12);
    color: #34a853;
}

/* 发布后有改动，需要重新发布 */
.status-need-republish {
    background: rgba(251, 188, 4, 0.15);
    color: #e8a000;
}

/* 向量已入库 */
.status-vector-done {
    background: rgba(26, 115, 232, 0.12);
    color: #1a73e8;
}

/* 向量未入库 */
.status-vector-none {
    background: rgba(158, 158, 158, 0.08);
    color: #bdbdbd;
}

/* 可点击的状态标签 */
.status-clickable {
    cursor: pointer;
    transition: all 0.2s;
}

.status-clickable:hover {
    background: rgba(26, 115, 232, 0.15);
    color: #1a73e8;
    transform: scale(1.05);
}

/* 章节操作按钮 */
.chapter-actions {
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.2s;
    flex-shrink: 0;
}

.chapter-item:hover .chapter-actions {
    opacity: 1;
}

.chapter-actions .el-button {
    padding: 4px;
    height: 24px;
    width: 24px;
}

/* 章节元信息 */
.chapter-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding-top: 8px;
    border-top: 1px dashed #e8eaed;
}

.meta-row {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
}

.meta-row .el-icon {
    color: #9aa0a6;
    flex-shrink: 0;
}

.meta-label {
    color: #9aa0a6;
    font-weight: 500;
    min-width: 24px;
    flex-shrink: 0;
}

.meta-time {
    color: #5f6368;
    font-weight: 400;
    font-family: 'Courier New', monospace;
}

.outline-list {
    padding: 16px;
}

.outline-text {
    font-size: 14px;
    color: #5f6368;
    line-height: 1.8;
}

/* 中间大纲面板 */
.outline-panel {
    min-width: 300px;
    max-width: 700px;
    flex-shrink: 1;
    background: #fff;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8eaed;
    position: relative;
}

.outline-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0 20px;
    height: 56px;
    border-bottom: 1px solid #f0f0f0;
    font-size: 15px;
    font-weight: 600;
    color: #202124;
    background: linear-gradient(90deg, #fafbfc 0%, #f5f7fa 100%);
}

.outline-header .el-icon {
    color: #1a73e8;
    font-size: 18px;
}

.outline-body {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.outline-body::-webkit-scrollbar {
    width: 6px;
}

.outline-body::-webkit-scrollbar-track {
    background: transparent;
}

.outline-body::-webkit-scrollbar-thumb {
    background: #dadce0;
    border-radius: 3px;
}

.panel-section {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.panel-row {
    display: flex;
    gap: 12px;
}

.panel-section.half {
    flex: 1;
}

.section-label {
    font-size: 15px;
    font-weight: 600;
    color: #5f6368;
    display: flex;
    align-items: center;
    gap: 6px;
}

.section-label .el-icon {
    color: #1a73e8;
    font-size: 16px;
}

.outline-textarea :deep(.el-textarea__inner) {
    background: #f8f9fa;
    border: 1px solid #dadce0;
    border-radius: 8px;
    font-size: 14px;
    resize: vertical;
    padding: 14px 16px;
    transition: all 0.2s;
    min-height: 120px;
}

.outline-textarea.resizable-textarea :deep(.el-textarea__inner) {
    resize: vertical;
    min-height: 160px;
}

.outline-textarea.large-textarea :deep(.el-textarea__inner) {
    min-height: 240px;
    font-size: 15px;
}

.outline-textarea :deep(.el-textarea__inner:focus) {
    border-color: #1a73e8;
    background: #fff;
    box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.1);
}

.full-width {
    width: 100%;
}

.enhanced-select :deep(.el-input__wrapper) {
    padding: 8px 12px;
    min-height: 42px;
}

.enhanced-select :deep(.el-input__inner) {
    font-size: 14px;
}

.enhanced-input :deep(.el-input__wrapper) {
    padding: 8px 12px;
}

.enhanced-input :deep(.el-input__inner) {
    font-size: 14px;
    height: 42px;
}

.ai-section {
    margin-top: auto;
    padding-top: 8px;
}

/* AI 改写按钮固定底部区域 */
.ai-section-fixed {
    padding: 12px 20px;
    border-top: 1px solid #f0f0f0;
    background: #fff;
    flex-shrink: 0;
}

/* 折叠面板样式（融入大纲面板风格） */
.outline-collapse {
    border: none;
}

.outline-collapse :deep(.el-collapse-item__header) {
    background: transparent;
    border-bottom: none;
    padding: 0;
    height: 36px;
    line-height: 36px;
    font-size: 14px;
    color: #5f6368;
}

.outline-collapse :deep(.el-collapse-item__wrap) {
    border-bottom: none;
    background: transparent;
}

.outline-collapse :deep(.el-collapse-item__content) {
    padding-bottom: 0;
}

.collapse-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    font-weight: 600;
    color: #5f6368;
}

.collapse-title .el-icon {
    color: #1a73e8;
    font-size: 16px;
}

.collapse-content {
    padding: 12px 0 0;
}

.ai-options {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    padding: 10px 12px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e8eaed;
}

.ai-options .el-checkbox {
    font-size: 14px;
}

.option-tip {
    color: #9aa0a6;
    cursor: help;
    font-size: 14px;
}

.ai-continue-btn {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    height: 40px;
    border-radius: 8px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    transition: all 0.3s;
}

.ai-continue-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.ai-icon {
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.5;
    }
}

/* 右侧内容编辑区 */
.content-panel {
    flex: 1;
    min-width: 0;
    background: #fff;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8eaed;
}

.content-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    height: 56px;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(90deg, #fafbfc 0%, #f5f7fa 100%);
}

.content-title {
    font-size: 16px;
    font-weight: 600;
    color: #202124;
    display: flex;
    align-items: center;
    gap: 10px;
}

.content-title .el-icon {
    color: #1a73e8;
    font-size: 18px;
}

.content-actions {
    display: flex;
    align-items: center;
    gap: 20px;
}

.word-count {
    font-size: 14px;
    color: #5f6368;
    font-weight: 500;
}

/* 清除空行按钮 */
.clean-blank-btn {
    font-size: 12px;
    color: #9aa0a6;
    padding: 4px 8px;
    border-radius: 6px;
    transition: all 0.2s;
}

.clean-blank-btn:hover {
    color: #667eea;
    background: rgba(102, 126, 234, 0.06);
}

/* 背景色选择器 */
.bg-color-picker {
    position: relative;
    display: inline-block;
}

.bg-color-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 6px;
    border: 1px solid #dadce0;
    cursor: pointer;
    transition: all 0.2s;
    color: #5f6368;
}

.bg-color-btn:hover {
    border-color: #1a73e8;
    box-shadow: 0 2px 8px rgba(26, 115, 232, 0.2);
}

.bg-color-options {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 8px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
    padding: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    min-width: 140px;
    z-index: 100;
}

.bg-color-option {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    border: 2px solid transparent;
    display: flex;
    align-items: center;
    justify-content: center;
}

.bg-color-option:hover {
    transform: scale(1.1);
}

.bg-color-option.active {
    border-color: #1a73e8;
}

.check-icon {
    font-size: 12px;
    color: #1a73e8;
    font-weight: bold;
}

/* 保存按钮样式 */
.save-btn {
    border: none;
    font-weight: 500;
    transition: all 0.3s;
}

.save-btn-saved {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.save-btn-saved:hover {
    background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.save-btn-unsaved {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: #fff;
    box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
}

.save-btn-unsaved:hover {
    background: linear-gradient(135deg, #e082e0 0%, #e04660 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

/* 保存按钮脉冲动画 */
.save-btn-pulse {
    animation: btn-pulse 2s infinite;
}

@keyframes btn-pulse {
    0%,
    100% {
        box-shadow: 0 0 0 0 rgba(245, 87, 108, 0.4);
    }
    50% {
        box-shadow: 0 0 0 8px rgba(245, 87, 108, 0);
    }
}

.content-body {
    flex: 1;
    overflow: hidden;
    padding: 15px 5px;
}

.content-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 24px;
    border-top: 1px solid #f0f0f0;
    background: linear-gradient(90deg, #fafbfc 0%, #f5f7fa 100%);
}

.footer-left {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;
    color: #5f6368;
}

.footer-left .el-icon {
    color: #9aa0a6;
}

.footer-right {
    display: flex;
    align-items: center;
    gap: 8px;
}

/* 底部复制/导出操作按钮 */
.footer-action-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    color: #5f6368;
    border: 1px solid #e8eaed;
    background: #fff;
    transition: all 0.2s;
}

.footer-action-btn:hover {
    color: #667eea;
    border-color: #667eea;
    background: rgba(102, 126, 234, 0.04);
}

/* 右侧功能区（活动栏 + 内容面板） */
.function-area {
    display: flex;
    flex-shrink: 0;
    min-width: 48px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(102, 126, 234, 0.06);
    border: 1px solid #eef0f8;
    overflow: hidden;
}

/* 活动栏（竖排图标，融入整体面板） */
.activity-bar {
    width: 48px;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 12px 0;
    gap: 4px;
    border-left: 1px solid #f0f0f5;
    background: linear-gradient(180deg, #fafbff 0%, #f5f6fc 100%);
    flex-shrink: 0;
}

.activity-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    cursor: pointer;
    color: #a0a3b1;
    transition: all 0.25s ease;
}

.activity-icon:hover {
    color: #667eea;
    background: rgba(102, 126, 234, 0.08);
}

.activity-icon.active {
    color: #667eea;
    background: rgba(102, 126, 234, 0.12);
}

/* 内容面板（去掉独立边框，融入 function-area） */
.function-panel {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    position: relative;
}

/* 面板标题栏 */
.panel-title-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px 0 16px;
    height: 44px;
    border-bottom: 1px solid #f0f0f5;
    background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
    flex-shrink: 0;
}

.panel-title-text {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    letter-spacing: 0.3px;
}

/* Tab 内容区通用样式 */
.function-tab-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.tab-panel-body {
    flex: 1;
    overflow-y: auto;
    padding: 14px;
}

.tab-panel-body::-webkit-scrollbar {
    width: 5px;
}

.tab-panel-body::-webkit-scrollbar-track {
    background: transparent;
}

.tab-panel-body::-webkit-scrollbar-thumb {
    background: #e0e0e8;
    border-radius: 3px;

    &:hover {
        background: #c8c8d4;
    }
}

.tab-panel-footer {
    padding: 10px 14px;
    border-top: 1px solid #f0f0f5;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    flex-shrink: 0;
    background: #fff;
}

.inline-graph-container {
    width: 100%;
    height: 100%;
    min-height: 400px;
}

/* 历史版本内嵌面板 */
.history-panel-body {
    display: flex;
    flex-direction: column;
    gap: 10px;
    overflow: hidden;
}

.inline-history-list {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.empty-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
}

.inline-history-item {
    padding: 10px 12px;
    border: 1px solid #eef0f8;
    border-radius: 10px;
    background: #fff;
    cursor: pointer;
    transition: all 0.25s ease;
}

.inline-history-item:hover {
    border-color: #d8ddf5;
    background: #f8f9fe;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.06);
}

.inline-history-item.active {
    border-color: rgba(102, 126, 234, 0.3);
    background: rgba(102, 126, 234, 0.06);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

/* 发布标记 */
.published-tag {
    margin-left: 6px;
    font-size: 10px;
    height: 18px;
    padding: 0 6px;
}

/* 版本备注 */
.version-remark {
    font-size: 12px;
    color: #8c8fa3;
    margin: 4px 0;
    padding: 4px 8px;
    background: #f8f9fe;
    border-radius: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.inline-version-preview {
    margin-top: 10px;
    border: 1px solid #eef0f8;
    border-radius: 10px;
    overflow: hidden;
}

.inline-version-preview .preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    border-bottom: 1px solid #f0f0f5;
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
}

.inline-version-preview .preview-content {
    padding: 12px 14px;
    max-height: 400px;
    overflow-y: auto;
    background: #fff;
}

.inline-version-preview .preview-content pre {
    margin: 0;
    white-space: pre-wrap;
    word-wrap: break-word;
    font-family: inherit;
    font-size: 13px;
    line-height: 1.6;
    color: #303133;
}

/* AI 问答内嵌面板 */
.chat-session-bar {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 10px 14px;
    border-bottom: 1px solid #f0f0f5;
    background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
    flex-shrink: 0;
    min-height: 48px;
}

.session-select {
    flex: 1;
}

/* 会话选择器标题字号增大 */
.session-select :deep(.el-input__inner) {
    font-size: 14px;
    font-weight: 500;
}

.chat-panel-body {
    padding: 0;
    overflow-y: hidden;
    display: flex;
    flex-direction: column;
}

/* 聊天消息区域包裹容器（用于定位浮动按钮） */
.chat-messages-wrapper {
    position: relative;
    flex: 1;
    min-height: 0;
    overflow: hidden;
}

.inline-chat-messages {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    overflow-y: auto;
    padding: 14px 14px 36px;
    background: #f8f9fe;
}

.inline-chat-messages::-webkit-scrollbar {
    width: 5px;
}

.inline-chat-messages::-webkit-scrollbar-track {
    background: transparent;
}

.inline-chat-messages::-webkit-scrollbar-thumb {
    background: #e0e0e8;
    border-radius: 3px;

    &:hover {
        background: #c8c8d4;
    }
}

/* 一键到底浮动按钮 */
.scroll-to-bottom-btn {
    position: absolute;
    bottom: 12px;
    right: 16px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    color: #667eea;
    transition: all 0.2s ease;
    border: 1px solid #e8e8f0;

    &:hover {
        background: #667eea;
        color: #fff;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
    }
}

/* 一键到底按钮出入动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.25s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
    opacity: 0;
    transform: translateX(-50%) translateY(10px);
}

/* 聊天消息样式 */
.chat-message {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
}

.chat-message.user {
    flex-direction: row-reverse;
}

.chat-message.user .message-content {
    align-items: flex-end;
}

.chat-message.user .message-text {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 12px 12px 2px 12px;
}

.message-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 80%;
}

.message-text {
    padding: 10px 14px;
    background: #fff;
    border-radius: 12px 12px 12px 2px;
    font-size: 13px;
    line-height: 1.6;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
    color: #303133;
}

/* AI 回复 Markdown 渲染样式 */
.ai-markdown-content {
    word-break: break-word;
}

.ai-markdown-content :deep(h1) {
    font-size: 18px;
    font-weight: 600;
    margin: 10px 0 6px;
    color: #1f2937;
}

.ai-markdown-content :deep(h2) {
    font-size: 16px;
    font-weight: 600;
    margin: 8px 0 4px;
    color: #1f2937;
}

.ai-markdown-content :deep(h3) {
    font-size: 14px;
    font-weight: 600;
    margin: 6px 0 4px;
    color: #1f2937;
}

.ai-markdown-content :deep(p) {
    margin: 4px 0;
}

.ai-markdown-content :deep(ul),
.ai-markdown-content :deep(ol) {
    padding-left: 18px;
    margin: 4px 0;
}

.ai-markdown-content :deep(li) {
    margin: 2px 0;
}

.ai-markdown-content :deep(strong) {
    font-weight: 600;
    color: #1f2937;
}

.ai-markdown-content :deep(em) {
    font-style: italic;
    color: #6b7280;
}

.ai-markdown-content :deep(code) {
    background: #f3f4f6;
    padding: 1px 5px;
    border-radius: 3px;
    font-size: 12px;
    font-family: 'Consolas', 'Monaco', monospace;
    color: #e74c3c;
}

.ai-markdown-content :deep(pre) {
    background: #1e1e2e;
    color: #cdd6f4;
    padding: 10px 14px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 6px 0;
    font-size: 12px;
    line-height: 1.5;
}

.ai-markdown-content :deep(pre code) {
    background: transparent;
    color: inherit;
    padding: 0;
    font-size: inherit;
}

.ai-markdown-content :deep(blockquote) {
    border-left: 3px solid #667eea;
    padding: 4px 12px;
    margin: 6px 0;
    color: #6b7280;
    background: #f8f9ff;
    border-radius: 0 6px 6px 0;
}

.ai-markdown-content :deep(hr) {
    border: none;
    border-top: 1px solid #e8eaed;
    margin: 8px 0;
}

.ai-markdown-content :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 6px 0;
    font-size: 12px;
}

.ai-markdown-content :deep(th),
.ai-markdown-content :deep(td) {
    border: 1px solid #e8eaed;
    padding: 4px 8px;
    text-align: left;
}

.ai-markdown-content :deep(th) {
    background: #f5f7fa;
    font-weight: 600;
}

/* 消息操作栏（复制按钮） */
.message-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 2px;
}

.copy-message-btn {
    color: #a0a3b1;
    font-size: 12px;
    padding: 2px 6px;
    border-radius: 4px;
    transition: all 0.2s ease;
}

.copy-message-btn:hover {
    color: #667eea;
    background: #f0f1ff;
}

.copy-btn-text {
    margin-left: 3px;
}

.revoke-message-btn {
    color: #9aa0a6;
    font-size: 12px;
    padding: 3px 8px;
    border-radius: 6px;
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    gap: 3px;
}

.revoke-message-btn:hover {
    color: #667eea;
    background: rgba(102, 126, 234, 0.08);
}

/* 微信风格撤回提示（居中灰色小字） */
.chat-message.is-revoked {
    justify-content: center;
    padding: 2px 0;
}

.revoked-notice {
    font-size: 11px;
    color: #b0b3bf;
    background: #f0f0f5;
    padding: 2px 10px;
    border-radius: 4px;
    text-align: center;
}

/* 加载更多历史消息提示 */
.chat-loading-more,
.chat-no-more {
    text-align: center;
    font-size: 12px;
    color: #b0b3bf;
    padding: 8px 0;
}

/* AI 思考/工具执行状态提示条（固定在输入框上方） */
.chat-thinking-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 14px;
    background: #f8f7ff;
    border-top: 1px solid #ede9fe;
    font-size: 12px;
    color: #7c3aed;
    flex-shrink: 0;
}

.thinking-text {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* 三点跳动动画 */
.thinking-dot-anim {
    display: inline-flex;
    gap: 3px;
    align-items: center;
}

.thinking-dot-anim span {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: #7c3aed;
    animation: dotBounce 1.2s infinite ease-in-out;
}

.thinking-dot-anim span:nth-child(2) {
    animation-delay: 0.2s;
}

.thinking-dot-anim span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes dotBounce {
    0%,
    80%,
    100% {
        opacity: 0.3;
        transform: scale(0.8);
    }
    40% {
        opacity: 1;
        transform: scale(1);
    }
}

.chat-input-area {
    padding: 10px 14px;
    border-top: 1px solid #f0f0f5;
    display: flex;
    gap: 8px;
    background: #fff;
    flex-shrink: 0;
}

.chat-input {
    flex: 1;
}

.send-btn {
    align-self: flex-end;
    border-radius: 8px;
}

/* 会话选项自定义样式 */
.session-option-item {
    display: flex;
    align-items: center;
    gap: 4px;
    overflow: hidden;
}

.session-star-icon {
    flex-shrink: 0;
}

.session-option-title {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 收藏按钮激活态 */
.is-favorited {
    color: #f59e0b !important;
}

/* 聊天操作按钮区域 */
.chat-action-buttons {
    display: flex;
    align-items: flex-end;
}

.stop-btn {
    border-radius: 8px;
}

/* 版本信息样式 */
.version-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
}

.version-left {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #8c8fa3;
}

.version-time {
    font-size: 12px;
    font-weight: 500;
}

.version-word {
    font-size: 11px;
    color: #a0a3b1;
    font-weight: 500;
}

.version-actions {
    display: flex;
    justify-content: flex-end;
}

.restore-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border: none;
    font-size: 12px;
    border-radius: 6px;
    padding: 4px 12px;
    transition:
        opacity 0.2s ease,
        transform 0.15s ease;
}

.restore-btn:hover {
    opacity: 0.85;
    transform: translateY(-1px);
}

/* 章节弹窗（创建/编辑统一风格） */
.chapter-dialog-body {
    padding: 20px 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.chapter-number-badge {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    font-size: 18px;
    font-weight: 600;
    color: #1a73e8;
    background: linear-gradient(135deg, #e8f0fe 0%, #d4e4ff 100%);
    padding: 10px 28px;
    border-radius: 10px;
    border: 1px solid #aecbfa;
}

.chapter-number-badge .el-input-number {
    width: 110px;
}

.chapter-number-badge .el-input-number :deep(.el-input__wrapper) {
    border-radius: 6px;
    background: rgba(255, 255, 255, 0.85);
}

.chapter-number-label {
    white-space: nowrap;
    letter-spacing: 1px;
}

.chapter-dialog-body .el-input {
    max-width: 320px;
}

.chapter-dialog-body .el-input__wrapper {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    padding: 4px 16px;
}

.chapter-dialog-body .el-input__inner {
    font-size: 15px;
    height: 44px;
}

/* 人物内联详情卡片 */
.character-graph-body {
    padding: 0;
    position: relative;
}

.character-inline-detail {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    z-index: 5;
    background: #fff;
    border-bottom: 1px solid #eef0f8;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.1);
    max-height: 50%;
    overflow-y: auto;
}

.inline-detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border-bottom: 1px solid #f0f0f5;
    background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
}

.inline-detail-avatar {
    display: flex;
    align-items: center;
    gap: 12px;
}

.inline-detail-name {
    display: flex;
    align-items: center;
    gap: 8px;
}

.detail-name-text {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.inline-detail-body {
    padding: 12px 14px;
}

.inline-relations-title {
    font-size: 13px;
    color: #8c8fa3;
    margin-bottom: 10px;
    font-weight: 500;
}

.inline-relations-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.inline-relation-chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    background: #f8f9fe;
    border-radius: 16px;
    font-size: 13px;
    border: 1px solid #eef0f8;
    transition: background 0.2s ease;

    &:hover {
        background: rgba(102, 126, 234, 0.06);
    }
}

.inline-relation-name {
    color: #303133;
    font-weight: 500;
}

.inline-no-relation {
    color: #a0a3b1;
    font-size: 13px;
    text-align: center;
    padding: 12px 0;
}

/* 前往编辑按钮 */
.inline-detail-footer {
    display: flex;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #f0f0f5;
    margin-top: 4px;
}

.goto-edit-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 12px;
    padding: 6px 14px;
    transition:
        opacity 0.2s,
        transform 0.15s;

    &:hover,
    &:focus {
        opacity: 0.85;
        color: #fff;
        transform: translateY(-1px);
    }

    &:active {
        transform: translateY(0);
    }
}

/* 基本信息网格 */
.inline-info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    margin-bottom: 10px;
}

.inline-info-item {
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 6px 10px;
    background: #f8f9fe;
    border-radius: 8px;
    border: 1px solid #eef0f8;
}

.inline-info-label {
    font-size: 11px;
    color: #a0a3b1;
}

.inline-info-value {
    font-size: 13px;
    font-weight: 500;
    color: #303133;
}

/* 详情分区 */
.inline-section {
    margin-bottom: 10px;
}

.inline-section-title {
    font-size: 12px;
    color: #8c8fa3;
    font-weight: 500;
    margin-bottom: 4px;
}

.inline-section-text {
    font-size: 13px;
    color: #303133;
    line-height: 1.6;
    padding: 6px 10px;
    background: #f8f9fe;
    border-radius: 8px;
    border: 1px solid #eef0f8;
    word-break: break-all;
}

/* 滑入动画 */
.slide-up-enter-active,
.slide-up-leave-active {
    transition: all 0.25s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
    opacity: 0;
    transform: translateY(-12px);
}

/* AI 大纲弹窗 */
.ai-outline-dialog :deep(.el-dialog__body) {
    padding: 24px;
}

.ai-outline-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.ai-outline-tip {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 12px 16px;
    background: #f0f7ff;
    border-radius: 8px;
    font-size: 13px;
    color: #1a73e8;
    line-height: 1.5;
}

.ai-outline-tip .el-icon {
    margin-top: 2px;
    flex-shrink: 0;
}

.ai-outline-textarea :deep(.el-textarea__inner) {
    background: #f8f9fa;
    border: 1px solid #e8eaed;
    border-radius: 8px;
    font-size: 14px;
    line-height: 1.8;
    color: #202124;
}

/* AI 审核结果弹窗 */
.audit-result-dialog :deep(.el-dialog__body) {
    padding: 24px;
    max-height: 600px;
    overflow-y: auto;
}

.audit-result-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.audit-summary {
    display: flex;
    gap: 20px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;
    flex-wrap: wrap;
}

.summary-item {
    display: flex;
    align-items: center;
    gap: 8px;
}

.summary-label {
    font-weight: 600;
    color: #5f6368;
    min-width: 80px;
}

.issues-title {
    margin: 0 0 16px 0;
    font-size: 16px;
    color: #202124;
    font-weight: 600;
}

.issue-item {
    padding: 16px;
    border: 1px solid #e8eaed;
    border-radius: 8px;
    margin-bottom: 12px;
    background: #fff;
    transition: all 0.2s;
}

.issue-item:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border-color: #d0d7de;
}

/* Agent 配置弹窗样式 */
.agent-config-dialog :deep(.el-dialog__body) {
    padding: 24px;
    max-height: 600px;
    overflow-y: auto;
}

.agent-config-content {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.config-section {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.config-section .section-label {
    font-size: 16px;
    font-weight: 600;
    color: #5f6368;
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e8eaed;
}

.config-section .section-label .el-icon {
    color: #1a73e8;
    font-size: 18px;
}

.agent-config-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.agent-config-form .el-form-item {
    margin-bottom: 0;
}

.agent-config-form .el-form-item__label {
    font-weight: 500;
    color: #5f6368;
}

.enhanced-input-number {
    width: 100%;
}

.enhanced-slider {
    width: 100%;
    margin: 10px 0;
}

.slider-value {
    text-align: center;
    font-size: 14px;
    color: #1a73e8;
    font-weight: 500;
    margin-top: 4px;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .agent-config-dialog {
        width: 90% !important;
    }
}

/* 问题严重程度样式 */
.issue-severity-high {
    border-left: 4px solid #f56c6c;
}

.issue-severity-medium {
    border-left: 4px solid #e6a23c;
}

.issue-severity-low {
    border-left: 4px solid #67c23a;
}

.issue-header {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
}

.issue-content {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.original-text,
.issue-reason,
.issue-suggestion {
    display: flex;
    gap: 8px;
    align-items: flex-start;
}

.text-label,
.reason-label,
.suggestion-label {
    font-weight: 600;
    color: #5f6368;
    min-width: 60px;
    flex-shrink: 0;
}

.text-content {
    flex: 1;
    padding: 8px 12px;
    background: #f8f9fa;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
}

.reason-content,
.suggestion-content {
    flex: 1;
    line-height: 1.6;
    color: #202124;
}

.audit-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid #e8eaed;
    margin-top: 8px;
}

/* 重新入库确认弹窗 */
.resync-confirm-body {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 24px;
    gap: 12px;
}

.resync-icon-wrapper {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
    display: flex;
    align-items: center;
    justify-content: center;
}

.resync-chapter-title {
    font-size: 17px;
    font-weight: 600;
    color: #303133;
}

.resync-desc {
    font-size: 14px;
    color: #606266;
}

.resync-warning-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: #fdf6ec;
    border-radius: 8px;
    font-size: 13px;
    color: #e6a23c;
    width: 100%;
    justify-content: center;
}

/* 小说大纲展示区域 */
.novel-outline-display {
    height: 100%;
    overflow-y: auto;
    padding: 4px;
}

.outline-empty-hint {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    height: 200px;
    color: #a0a3b1;
    font-size: 13px;
}

.outline-empty-hint .el-icon {
    font-size: 36px;
    color: #d8ddf5;
}

/* Markdown 渲染内容样式（使用 :deep 穿透 v-html 生成的子元素） */
.markdown-rendered-content {
    font-size: 13px;
    line-height: 1.8;
    color: #374151;
    word-break: break-word;
}

.markdown-rendered-content :deep(h1) {
    font-size: 20px;
    font-weight: 600;
    margin: 16px 0 8px;
    padding-bottom: 6px;
    border-bottom: 1px solid #e8eaed;
    color: #1f2937;
}

.markdown-rendered-content :deep(h2) {
    font-size: 17px;
    font-weight: 600;
    margin: 14px 0 6px;
    color: #1f2937;
}

.markdown-rendered-content :deep(h3) {
    font-size: 15px;
    font-weight: 600;
    margin: 12px 0 4px;
    color: #374151;
}

.markdown-rendered-content :deep(p) {
    margin: 6px 0;
}

.markdown-rendered-content :deep(ul),
.markdown-rendered-content :deep(ol) {
    padding-left: 20px;
    margin: 6px 0;
}

.markdown-rendered-content :deep(li) {
    margin: 3px 0;
}

.markdown-rendered-content :deep(strong) {
    font-weight: 600;
    color: #1f2937;
}

.markdown-rendered-content :deep(em) {
    font-style: italic;
    color: #6b7280;
}

.markdown-rendered-content :deep(code) {
    background: #f3f4f6;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    color: #e11d48;
}

.markdown-rendered-content :deep(blockquote) {
    border-left: 3px solid #667eea;
    padding: 4px 12px;
    margin: 8px 0;
    background: #f8f9fe;
    color: #4b5563;
}

.markdown-rendered-content :deep(hr) {
    border: none;
    border-top: 1px solid #e8eaed;
    margin: 12px 0;
}

/* 面板间拖拽手柄：双竖线 + 中间菱形装饰 */
.panel-divider-handle {
    flex-shrink: 0;
    width: 12px;
    margin: 0;
    cursor: col-resize;
    user-select: none;
    position: relative;

    /* 左竖线 */

    &::before,
    &::after {
        content: '';
        position: absolute;
        top: 50%;
        width: 1.5px;
        height: 28px;
        margin-top: -14px;
        border-radius: 1px;
        background-color: #d1d5db;
        transition: all 0.25s ease;
    }

    &::before {
        left: 4px;
    }

    &::after {
        right: 4px;
    }

    &:hover::before,
    &:hover::after {
        background-color: #818cf8;
        height: 36px;
        margin-top: -18px;
        box-shadow: 0 0 4px rgba(99, 102, 241, 0.3);
    }

    &:active::before,
    &:active::after {
        background-color: #6366f1;
        height: 40px;
        margin-top: -20px;
        box-shadow: 0 0 6px rgba(99, 102, 241, 0.5);
    }
}

/* 小说设置面板样式 */
.settings-panel-body {
    padding: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

/* 自定义 Tab 栏 */
.settings-tab-bar {
    display: flex;
    gap: 6px;
    padding: 10px 14px;
    border-bottom: 1px solid #f0f0f5;
    background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
    flex-shrink: 0;
}

.settings-tab-item {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 5px 14px;
    font-size: 12px;
    font-weight: 500;
    color: #8c8fa3;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.25s ease;
    background: transparent;
    border: 1px solid transparent;
    user-select: none;

    &:hover {
        color: #667eea;
        background: rgba(102, 126, 234, 0.06);
    }

    &.active {
        color: #667eea;
        background: rgba(102, 126, 234, 0.1);
        border-color: rgba(102, 126, 234, 0.2);
    }
}

.settings-scroll-area {
    padding: 14px;
    overflow-y: auto;
    flex: 1;
}

.settings-scroll-area::-webkit-scrollbar {
    width: 5px;
}

.settings-scroll-area::-webkit-scrollbar-track {
    background: transparent;
}

.settings-scroll-area::-webkit-scrollbar-thumb {
    background: #e0e0e8;
    border-radius: 3px;

    &:hover {
        background: #c8c8d4;
    }
}

/* Agent 配置卡片 */
.settings-card {
    background: #fff;
    border: 1px solid #eef0f8;
    border-radius: 10px;
    padding: 14px;
    margin-bottom: 10px;
    transition:
        border-color 0.2s ease,
        box-shadow 0.2s ease;

    &:hover {
        border-color: #d8ddf5;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.06);
    }

    &:last-child {
        margin-bottom: 0;
    }
}

.settings-card-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
}

.settings-card-hint {
    font-size: 11px;
    color: #a0a3b1;
    margin-top: 6px;
    line-height: 1.4;
}

.agent-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
}

.agent-option-name {
    font-size: 13px;
}

/* 提示条 */
.settings-alert {
    margin-bottom: 12px;
    border-radius: 8px;
}

/* 设置分组 */
.settings-group {
    margin-bottom: 16px;

    &:last-child {
        margin-bottom: 0;
    }
}

.settings-group-title {
    display: flex;
    align-items: center;
    gap: 7px;
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f5;
}

.settings-group-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
}

/* 开关列表 */
.settings-switch-list {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.settings-switch-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 10px;
    border-radius: 8px;
    transition: background 0.2s ease;

    &:hover {
        background: #f8f9fe;
    }
}

.setting-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    min-width: 0;
}

.setting-label {
    font-size: 13px;
    color: #303133;
    font-weight: 500;
}

.setting-desc {
    font-size: 11px;
    color: #a0a3b1;
    line-height: 1.4;
}

/* 数字输入行 */
.settings-number-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 10px;
    border-radius: 8px;
    background: #f8f9fe;
}

.settings-number-hint {
    font-size: 11px;
    color: #a0a3b1;
    margin-top: 4px;
    padding-left: 10px;
    line-height: 1.4;
}

/* 底部保存栏 */
.settings-footer {
    padding: 10px 14px;
    border-top: 1px solid #f0f0f5;
    display: flex;
    justify-content: flex-end;
    background: #fff;
    flex-shrink: 0;
}

.settings-save-btn {
    border-radius: 8px;
    padding: 8px 20px;
    font-size: 13px;
    font-weight: 500;
}

/* 活动栏未读角标 */
.activity-icon {
    position: relative;
}

.activity-badge {
    position: absolute;
    top: 2px;
    right: 2px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    border-radius: 8px;
    background: #667eea;
    color: #fff;
    font-size: 10px;
    font-weight: 600;
    line-height: 16px;
    text-align: center;
}

/* 批注列表面板 */
.annotation-list-body {
    padding: 0;
}

.annotation-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 10px;
}

.annotation-list-item {
    padding: 10px 12px;
    border: 1px solid #eef0f8;
    border-radius: 10px;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
    border-left: 3px solid #f59e0b;
}

.annotation-list-item:hover {
    border-color: #d8ddf5;
    border-left-color: #f59e0b;
    background: #f8f9fe;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.06);
}

/* 已采纳卡片：绿色竖条 */
.annotation-list-item.is-accepted {
    border-left-color: #10b981;
}

.annotation-list-item.is-accepted:hover {
    border-left-color: #10b981;
}

/* 已忽略卡片：灰色竖条 + 文字变浅 */
.annotation-list-item.is-ignored {
    border-left-color: #d0d0d0;
    background: #fafafa;
}

.annotation-list-item.is-ignored:hover {
    border-left-color: #d0d0d0;
}

.annotation-list-item.is-ignored .annotation-list-author {
    color: #bbb;
}

.annotation-list-item.is-ignored .annotation-list-content {
    color: #999;
}

.annotation-list-item.is-ignored .annotation-list-original {
    color: #c0c0c0;
}

/* 未读卡片：紫蓝色竖条覆盖 */
.annotation-list-item.is-unread {
    border-left-color: #667eea;
    background: #f8f9fe;
}

.annotation-list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
}

.annotation-list-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
}

.annotation-list-author {
    font-size: 12px;
    font-weight: 600;
    color: #667eea;
}

.annotation-list-time {
    font-size: 11px;
    color: #9aa0a6;
}

/* 右上角状态标签（纯文字 + 小圆点，与按钮区分） */
.annotation-status-tag {
    font-size: 11px;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 4px;
    color: #9aa0a6;
}

.annotation-status-tag::before {
    content: '';
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
}

.annotation-status-tag.status-pending {
    color: #f59e0b;
}

.annotation-status-tag.status-pending::before {
    background: #f59e0b;
}

.annotation-status-tag.status-resolved {
    color: #d0d0d0;
}

.annotation-status-tag.status-resolved::before {
    background: #d0d0d0;
}

.annotation-status-tag.status-accepted {
    color: #10b981;
}

.annotation-status-tag.status-accepted::before {
    background: #10b981;
}

.unread-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #ef4444;
    flex-shrink: 0;
}

.annotation-list-original {
    font-size: 11px;
    color: #9aa0a6;
    font-style: italic;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.annotation-list-content {
    font-size: 12px;
    color: #303133;
    line-height: 1.5;
    margin-bottom: 6px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.annotation-list-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 6px;
    padding-top: 6px;
    border-top: 1px solid #f5f5f8;
}

.resolve-btn {
    padding: 2px 8px;
    border: 1px solid #d8ddf5;
    border-radius: 4px;
    background: #eef0ff;
    color: #667eea;
    font-size: 11px;
    cursor: pointer;
    transition: all 0.15s;
}

.resolve-btn:hover {
    background: #dde1ff;
    border-color: #c0c8f0;
}

.adopt-btn {
    padding: 2px 8px;
    border: 1px solid #d4edda;
    border-radius: 4px;
    background: #d4edda;
    color: #155724;
    font-size: 11px;
    cursor: pointer;
    transition: all 0.15s;
}

.adopt-btn:hover {
    background: #c3e6cb;
    border-color: #b1dfbb;
}

.ignore-btn {
    padding: 2px 8px;
    border: 1px solid #e8eaed;
    border-radius: 4px;
    background: #f5f5f5;
    color: #9aa0a6;
    font-size: 11px;
    cursor: pointer;
    transition: all 0.15s;
}

.ignore-btn:hover {
    background: #e8e8e8;
    border-color: #d0d0d0;
}
</style>

<style>
.revoke-message-box {
    border-radius: 8px !important;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12) !important;
    padding: 24px !important;
}

.revoke-message-box .el-message-box__header {
    padding: 0;
}

.revoke-message-box .el-message-box__title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
}

.revoke-message-box .el-message-box__content {
    padding: 12px 0 24px;
}

.revoke-message-box .el-message-box__status {
    display: none !important;
}

.revoke-message-box .el-message-box__message {
    font-size: 14px;
    color: #4b5563;
    line-height: 1.6;
    padding-left: 0 !important;
}

.revoke-message-box .el-message-box__btns {
    padding: 0;
    gap: 12px;
}

.revoke-message-box .el-message-box__btns .el-button {
    border-radius: 6px !important;
    font-size: 14px !important;
    padding: 8px 20px !important;
    font-weight: 500 !important;
    height: auto !important;
}

.revoke-message-box .revoke-confirm-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
    border: none !important;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.revoke-message-box .revoke-confirm-btn:hover {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    transform: translateY(-1px);
}

.revoke-message-box .revoke-cancel-btn {
    color: #5f6368 !important;
}

.revoke-message-box .revoke-cancel-btn:hover {
    color: #1a73e8 !important;
}
</style>
