<template>
	<div class="zhihu-news-list">
		<div class="news-header">
			<h3 class="news-title">
				<b-icon icon="newspaper" class="mr-2"></b-icon>
				知乎每日新闻
			</h3>
			<div class="news-date" v-if="newsDate">
				{{ newsDate }}
			</div>
		</div>

		<!-- 头条新闻 -->
		<div v-if="topStories.length > 0" class="top-stories mb-4">
			<h5 class="section-title">头条新闻</h5>
			<div class="row">
				<div class="col-md-6" v-for="(story, index) in topStories.slice(0, 2)" :key="index">
					<div class="top-story-card" @click="openUrl(story.url)">
						<div class="story-image">
							<img :src="story.image" :alt="story.title" @error="handleImageError" />
							<div class="story-overlay">
								<span class="story-hint">{{ story.hint }}</span>
							</div>
						</div>
						<div class="story-content">
							<h6 class="story-title">{{ story.title }}</h6>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 普通新闻列表 -->
		<div class="news-list">
			<h5 class="section-title">最新资讯</h5>
			<div class="row">
				<div class="col-md-6 col-lg-4 mb-3" v-for="(news, index) in newsList" :key="index">
					<div class="news-card" @click="openUrl(news.url)">
						<div class="news-image">
							<img :src="news.thumbnail || news.image" :alt="news.title" @error="handleImageError" />
							<div class="news-overlay">
								<span class="news-hint">{{ news.hint }}</span>
							</div>
						</div>
						<div class="news-content">
							<h6 class="news-card-title">{{ news.title }}</h6>
							<div class="news-meta">
								<span class="news-id">#{{ news.id }}</span>
								<span class="news-time">{{ news.ga_prefix }}</span>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 加载状态 -->
		<div v-if="loading" class="loading-state">
			<b-spinner variant="primary" label="Loading..."></b-spinner>
			<p class="mt-2">正在加载新闻...</p>
		</div>

		<!-- 错误状态 -->
		<div v-if="error" class="error-state">
			<b-alert show variant="danger">
				<h6>加载失败</h6>
				<p>{{ error }}</p>
				<b-button @click="retryLoad" variant="outline-danger" size="sm">重试</b-button>
			</b-alert>
		</div>
	</div>
</template>

<script>
import { zhihuNewsService } from '@/services/zhihuNews'

export default {
	name: 'ZhihuNewsList',
	data() {
		return {
			newsList: [],
			topStories: [],
			newsDate: '',
			loading: false,
			error: null
		}
	},
	mounted() {
		this.loadNews()
	},
	methods: {
		async loadNews() {
			this.loading = true
			this.error = null

			try {
				const data = await zhihuNewsService.getDailyNews()
				
				// 设置新闻日期
				if (data.date) {
					this.newsDate = zhihuNewsService.formatDate(data.date)
				}

				// 设置头条新闻
				this.topStories = await zhihuNewsService.getTopStories()
				
				// 设置普通新闻列表
				this.newsList = await zhihuNewsService.getNewsList()

			} catch (error) {
				console.error('加载知乎新闻失败:', error)
				this.error = '无法加载新闻，请检查网络连接后重试'
			} finally {
				this.loading = false
			}
		},

		async retryLoad() {
			await this.loadNews()
		},

		openUrl(url) {
			if (url) {
				window.open(url, '_blank')
			}
		},

		handleImageError(event) {
			// 图片加载失败时显示默认图片
			event.target.src = '/img/default.png'
		}
	}
}
</script>

<style scoped>
.zhihu-news-list {
	padding: 1rem;
}

.news-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1.5rem;
	padding-bottom: 0.5rem;
	border-bottom: 2px solid #e9ecef;
}

.news-title {
	color: #2c3e50;
	margin: 0;
	display: flex;
	align-items: center;
}

.news-date {
	color: #6c757d;
	font-size: var(--font_base);
}

.section-title {
	color: #495057;
	margin-bottom: 1rem;
	padding-left: 0.5rem;
	border-left: 3px solid #007bff;
}

.top-stories {
	background: #f8f9fa;
	padding: 1rem;
	border-radius: 8px;
}

.top-story-card {
	background: white;
	border-radius: 8px;
	overflow: hidden;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
	cursor: pointer;
	transition: transform 0.2s, box-shadow 0.2s;
}

.top-story-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.story-image {
	position: relative;
	height: 200px;
	overflow: hidden;
}

.story-image img {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.story-overlay {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	background: linear-gradient(transparent, rgba(0,0,0,0.7));
	color: white;
	padding: 1rem;
}

.story-hint {
	font-size: var(--font_small);
	opacity: 0.9;
}

.story-content {
	padding: 1rem;
}

.story-title {
	color: #2c3e50;
	font-weight: 600;
	line-height: 1.4;
	margin: 0;
}

.news-card {
	background: white;
	border-radius: 8px;
	overflow: hidden;
	box-shadow: 0 2px 6px rgba(0,0,0,0.1);
	cursor: pointer;
	transition: transform 0.2s, box-shadow 0.2s;
	height: 100%;
	display: flex;
	flex-direction: column;
}

.news-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.news-image {
	position: relative;
	height: 180px;
	overflow: hidden;
}

.news-image img {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.news-overlay {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	background: linear-gradient(transparent, rgba(0,0,0,0.8));
	color: white;
	padding: 0.75rem;
}

.news-hint {
	font-size: var(--font_small);
	opacity: 0.9;
}

.news-content {
	padding: 1rem;
	flex: 1;
	display: flex;
	flex-direction: column;
}

.news-card-title {
	color: #2c3e50;
	font-weight: 600;
	line-height: 1.4;
	margin: 0 0 1rem 0;
	flex: 1;
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.news-meta {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: var(--font_small);
	color: #6c757d;
}

.loading-state, .error-state {
	text-align: center;
	padding: 2rem;
}

@media (max-width: 768px) {
	.news-header {
		flex-direction: column;
		align-items: flex-start;
		gap: 0.5rem;
	}
	
	.top-stories {
		padding: 0.75rem;
	}
	
	.news-image {
		height: 160px;
	}
}
</style>