<template>
    <main class="auth-page">
        <div class="auth-spread">
            <div class="frontispiece">
                <div class="imprint">
                    <el-icon>
                        <Reading />
                    </el-icon>
                    HiNovel
                </div>
                <div class="book-title">小说创作室</div>
                <img alt="" class="bookplate" src="@/assets/writing-room.webp" />
            </div>
            <section class="form-page">
                <div class="form-content">
                    <h1>{{ title }}</h1>
                    <slot />
                </div>
            </section>
        </div>
    </main>
</template>

<script lang="ts" setup>
import { Reading } from '@element-plus/icons-vue'

defineProps<{ title: string }>()
</script>

<style scoped>
.auth-page {
    min-height: 100vh;
    min-height: 100dvh;
    display: grid;
    place-items: center;
    padding: clamp(14px, 4vw, 64px);
}

.auth-spread {
    display: grid;
    grid-template-columns: 1.08fr 1fr;
    width: min(100%, 1040px);
    background: var(--surface);
    border: 1px solid var(--rule);
    border-radius: 5px 14px 14px 5px;
    overflow: hidden;
    box-shadow: 0 16px 50px #22372e0d;
}

.frontispiece {
    display: flex;
    flex-direction: column;
    min-width: 0;
    padding: clamp(26px, 4vw, 56px);
    border-left: 9px solid var(--pine-deep);
    color: #f7f5df;
    background: var(--pine);
}

.imprint {
    display: flex;
    gap: 9px;
    align-items: center;
    font:
        bold 24px/1 Georgia,
        serif;
    letter-spacing: -0.8px;
}

.book-title {
    font: 600 clamp(36px, 4.4vw, 64px)/1.3 var(--display-font);
    margin: auto 0 32px;
    padding-top: 46px;
}

.bookplate {
    width: 100%;
    aspect-ratio: 1.4;
    object-fit: cover;
    outline: 1px solid #e6ddba88;
    outline-offset: 5px;
    margin-block: 8px;
}

.form-page {
    display: grid;
    place-items: center;
    padding: clamp(28px, 4vw, 64px);
    min-height: 554px;
}

.form-content {
    width: min(100%, 350px);
}

h1 {
    margin: 0 0 30px;
    font: 600 32px/1.25 var(--display-font);
}

:slotted(.el-form) {
    width: 100%;
}

.form-page :deep(.el-form-item__label) {
    font-weight: 600;
    color: var(--ink);
}

.form-page :deep(.submit-btn) {
    width: 100%;
    margin-top: 8px;
}

.form-page :deep(.form-footer) {
    margin-top: 24px;
    font-size: 14px;
    color: var(--muted);
}

.form-page :deep(.form-footer a) {
    margin-left: 8px;
    font-weight: 600;
    text-underline-offset: 3px;
}

@media (max-width: 700px) {
    .auth-spread {
        grid-template-columns: 1fr;
        max-width: 460px;
    }

    .frontispiece {
        display: grid;
        grid-template-columns: 1fr 38%;
        align-items: center;
        column-gap: 20px;
        padding: 20px 24px;
    }

    .imprint {
        font-size: 20px;
    }

    .book-title {
        font-size: 26px;
        margin: 6px 0 0;
        padding: 0;
    }

    .bookplate {
        grid-area: 1 / 2 / 3 / 3;
        margin: 0;
        aspect-ratio: 1.7;
        outline-offset: 2px;
    }

    .form-page {
        min-height: auto;
        padding: 30px 24px;
    }
}
</style>
