<template>
	<div class="history-widget">
		<div class="history-header">
			<h5 class="history-title">
				<b-icon icon="calendar3" class="mr-2"></b-icon>
				历史上的今天
			</h5>
			<div class="history-date">
				{{ todayDateInfo.fullDateString }}
			</div>
		</div>

		<!-- 历史文字事件 -->
		<div class="history-text-section">
			<div v-if="loading.text" class="loading-text">
				<b-spinner small variant="primary" label="Loading..."></b-spinner>
				<span class="ml-2">加载历史事件...</span>
			</div>
			<div v-else-if="historyTextData" class="history-event">
				<div class="event-year">{{ historyTextData.year }}年</div>
				<div class="event-title">{{ historyTextData.title }}</div>
				<b-button 
					v-if="historyTextData.url" 
					@click="openHistoryUrl" 
					variant="outline-primary" 
					size="sm" 
					class="mt-2"
				>
					<b-icon icon="link45deg" class="mr-1"></b-icon>
					查看详情
				</b-button>
			</div>
			<div v-else class="error-text">
				<b-alert show variant="warning" class="mb-0 p-2">
					<p class="mb-1 small">无法加载历史事件</p>
				</b-alert>
			</div>
		</div>

		<!-- 历史图片 -->
		<div class="history-image-section">
			<div class="section-title">历史图片</div>
			<div v-if="loading.image" class="loading-image">
				<b-spinner small variant="primary" label="Loading..."></b-spinner>
				<span class="ml-2">加载历史图片...</span>
			</div>
			<div v-else-if="historyImageData && historyImageData.success" class="history-image-container">
				<div class="history-image-wrapper" @click="viewImage">
					<img 
						:src="historyImageData.imageUrl || mockImageUrl" 
						:alt="`历史上的今天 - ${todayDateInfo.dateString}`"
						@error="handleImageError"
						class="history-image"
					/>
					<div class="image-overlay">
						<b-icon icon="eye" class="overlay-icon"></b-icon>
						<span class="overlay-text">点击查看大图</span>
					</div>
				</div>
			</div>
			<div v-else class="error-image">
				<b-alert show variant="warning" class="mb-0 p-2">
					<p class="mb-1 small">无法加载历史图片</p>
				</b-alert>
			</div>
		</div>

		<!-- 图片查看模态框 -->
		<b-modal 
			:id="modalId" 
			title="历史上的今天" 
			hide-footer
			size="lg"
			centered
		>
			<div class="text-center">
				<img 
					:src="historyImageData?.imageUrl || mockImageUrl" 
					:alt="`历史上的今天 - ${todayDateInfo.dateString}`"
					class="modal-image"
				/>
				<p class="mt-3 text-muted">
					{{ todayDateInfo.fullDateString }} • 历史图片
				</p>
			</div>
		</b-modal>
	</div>
</template>

<script>
import { historyService } from '@/services/historyService'
import { historyTextService } from '@/services/historyTextService'

export default {
	name: 'HistoryWidget',
	data() {
		return {
			historyImageData: null,
			historyTextData: null,
			todayDateInfo: {},
			mockImageUrl: 'https://cdn.xxhzm.cn/v2api/cache/history/2025-12-26.jpg',
			modalId: 'history-modal-' + Date.now(),
			loading: {
				image: true,
				text: true
			}
		}
	},
	mounted() {
		this.initData()
		this.loadHistoryData()
	},
	methods: {
		initData() {
			this.todayDateInfo = historyTextService.getTodayDateInfo()
		},

		async loadHistoryData() {
			await Promise.all([
				this.loadHistoryImage(),
				this.loadHistoryText()
			])
		},

		async loadHistoryImage() {
			try {
				const result = await historyService.getTodayInHistory()
				if (result.success) {
					this.historyImageData = result
				}
			} catch (error) {
				console.error('加载历史图片失败:', error)
			} finally {
				this.loading.image = false
			}
		},

		async loadHistoryText() {
			try {
				const result = await historyTextService.getTodayInHistoryText()
				if (result.success) {
					this.historyTextData = result.data
				}
			} catch (error) {
				console.error('加载历史文字失败:', error)
			} finally {
				this.loading.text = false
			}
		},

		viewImage() {
			this.$bvModal.show(this.modalId)
		},

		handleImageError(event) {
			console.warn('历史图片加载失败:', event.target.src)
			event.target.src = this.mockImageUrl
		},

		openHistoryUrl() {
			if (this.historyTextData && this.historyTextData.url) {
				window.open(this.historyTextData.url, '_blank')
			}
		}
	}
}
</script>

<style scoped>
.history-widget {
	background-color: #fff;
	border-radius: 8px;
	padding: 1rem;
	color: #333;
	box-shadow: 0 2px 8px rgba(0,0,0,0.08);
	margin-top: 1rem;
}

.history-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1rem;
	padding-bottom: 0.5rem;
	border-bottom: 1px solid #e9ecef;
}

.history-title {
	margin: 0;
	font-weight: 600;
	color: #333;
	display: flex;
	align-items: center;
}

.history-date {
	font-size: 0.85rem;
	color: #666;
	font-weight: 500;
}

/* 历史文字事件部分 */
.history-text-section {
	margin-bottom: 1rem;
}

.history-event {
	padding: 1rem;
	background: #f8f9fa;
	border-radius: 8px;
	border-left: 3px solid #007bff;
}

.event-year {
	font-size: 0.85rem;
	color: #007bff;
	font-weight: 600;
	margin-bottom: 0.5rem;
}

.event-title {
	font-size: 0.95rem;
	color: #333;
	line-height: 1.4;
	font-weight: 500;
}

/* 历史图片部分 */
.history-image-section {
	margin-top: 1rem;
}

.section-title {
	font-size: 0.85rem;
	color: #666;
	margin-bottom: 0.6rem;
	font-weight: 600;
}

.history-image-container {
	position: relative;
	border-radius: 8px;
	overflow: hidden;
	box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.history-image-wrapper {
	position: relative;
	cursor: pointer;
	transition: transform 0.3s ease;
}

.history-image-wrapper:hover {
	transform: scale(1.02);
}

.history-image {
	width: 100%;
	height: 180px;
	object-fit: cover;
	display: block;
}

.image-overlay {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.4);
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	opacity: 0;
	transition: opacity 0.3s ease;
	color: white;
}

.history-image-wrapper:hover .image-overlay {
	opacity: 1;
}

.overlay-icon {
	font-size: 1.8rem;
	margin-bottom: 0.5rem;
}

.overlay-text {
	font-size: 0.85rem;
	font-weight: 500;
}

/* 加载和错误状态 */
.loading-text, .loading-image, .error-text, .error-image {
	text-align: center;
	padding: 1rem;
	color: #666;
}

.loading-text, .loading-image {
	display: flex;
	align-items: center;
	justify-content: center;
}

.modal-image {
	width: 100%;
	max-height: 70vh;
	object-fit: contain;
	border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
	.history-widget {
		padding: 0.8rem;
	}

	.history-header {
		flex-direction: column;
		align-items: flex-start;
		gap: 0.5rem;
	}

	.history-event {
		padding: 0.8rem;
	}

	.event-title {
		font-size: 0.95rem;
	}

	.history-image {
		height: 150px;
	}
}
</style>