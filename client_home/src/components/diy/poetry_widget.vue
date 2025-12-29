<template>
  <div class="poetry-widget">
    <div class="poetry-content">
      <div v-if="loading" class="poetry-loading">
        <b-spinner small variant="primary" label="Loading..."></b-spinner>
        <span class="ml-2">加载中...</span>
      </div>
      <div v-else-if="poetryData && poetryData.success" class="poetry-text">
        <div class="poetry-quote">
          <b-icon icon="quote" class="quote-icon"></b-icon>
          <span class="poetry-content-text">{{ poetryData.data.content }}</span>
        </div>
        <div class="poetry-actions">
          <b-button 
            @click="refreshPoetry" 
            variant="outline-primary" 
            size="sm"
            class="refresh-btn"
          >
            <b-icon icon="arrow-clockwise"></b-icon>
            换一句
          </b-button>
        </div>
      </div>
      <div v-else class="poetry-error">
        <b-alert show variant="warning" class="mb-0 p-2">
          <p class="mb-1 small">无法加载古诗词</p>
          <b-button 
            @click="refreshPoetry" 
            variant="outline-warning" 
            size="sm"
          >
            重试
          </b-button>
        </b-alert>
      </div>
    </div>
  </div>
</template>

<script>
import { poetryService } from '@/services/poetryService'

export default {
  name: 'PoetryWidget',
  data() {
    return {
      poetryData: null,
      loading: true
    }
  },
  mounted() {
    this.loadPoetry()
  },
  methods: {
    async loadPoetry() {
      this.loading = true
      try {
        const result = await poetryService.getDailyPoetry()
        if (result.success) {
          this.poetryData = result
        } else {
          // 如果API失败，使用备用数据
          this.poetryData = poetryService.getBackupPoetry()
        }
      } catch (error) {
        console.error('加载古诗词失败:', error)
        this.poetryData = poetryService.getBackupPoetry()
      } finally {
        this.loading = false
      }
    },

    async refreshPoetry() {
      await this.loadPoetry()
    }
  }
}
</script>

<style scoped>
.poetry-widget {
  display: flex;
  align-items: center;
  min-height: 60px;
}

.poetry-content {
  flex: 1;
}

.poetry-loading {
  display: flex;
  align-items: center;
  color: #666;
  font-size: var(--font_base);
}

.poetry-text {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.poetry-quote {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.quote-icon {
  color: #007bff;
  font-size: var(--font_base);
  margin-top: 0.2rem;
  flex-shrink: 0;
}

.poetry-content-text {
  font-size: var(--font_base);
  color: #333;
  line-height: 1.4;
  font-style: italic;
}

.poetry-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.refresh-btn {
  font-size: var(--font_small);
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
}

.poetry-error {
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .poetry-text {
    flex-direction: column;
    align-items: stretch;
    gap: 0.8rem;
  }

  .poetry-content-text {
    font-size: 0.85rem;
  }

  .poetry-actions {
    justify-content: center;
  }
}
</style>