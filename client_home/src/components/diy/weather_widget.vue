<template>
	<div class="weather-widget">
		<!-- 当前天气 -->
		<div v-if="currentWeather" class="current-weather">
			<div class="weather-header">
				<h5 class="weather-title">
					<b-icon icon="cloud-sun" class="mr-2"></b-icon>
					{{ currentWeather.city }} 实时天气
				</h5>
				<div class="weather-update-time">
					更新于 {{ updateTime }}
				</div>
			</div>

			<div class="current-weather-card">
				<div class="weather-main">
					<div class="weather-temp">
						<span class="temp-value">{{ weatherService.formatTemperature(currentWeather.current_temp) }}</span>
						<span class="temp-range">
							{{ weatherService.formatTemperature(currentWeather.low_temp) }} / {{ weatherService.formatTemperature(currentWeather.high_temp) }}
						</span>
					</div>
					<div class="weather-desc">
						<div class="weather-condition">{{ currentWeather.current_weather }}</div>
						<div class="weather-details">
							<span class="detail-item">
								<b-icon icon="thermometer" class="mr-1"></b-icon>
								体感温度 {{ weatherService.formatTemperature(currentWeather.current_temp) }}
							</span>
							<span class="detail-item">
								<b-icon icon="droplet" class="mr-1"></b-icon>
								湿度 {{ currentWeather.humidity }}%
							</span>
							<span class="detail-item">
								<b-icon icon="arrow-up" class="mr-1"></b-icon>
								{{ weatherService.formatWindDirection(currentWeather.wind_dir) }} {{ currentWeather.wind_speed }}级
							</span>
							<span class="detail-item">
								<b-icon icon="speedometer2" class="mr-1"></b-icon>
								气压 {{ currentWeather.pressure }}hPa
							</span>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 今日实时天气 -->
		<div v-if="realTimeWeather.length > 0" class="realtime-weather mt-4">
			<h6 class="section-title">今日实时天气</h6>
			<div class="realtime-timeline">
				<div 
					v-for="(weather, index) in realTimeWeather" 
					:key="index"
					class="realtime-item"
					:class="{ active: isCurrentTime(weather.time) }"
				>
					<div class="time">{{ weather.time }}</div>
					<div class="weather-icon">
						<b-icon :icon="getWeatherIcon(weather.weather)" class="weather-icon-svg"></b-icon>
					</div>
					<div class="temp">{{ weatherService.formatTemperature(weather.temperature) }}</div>
					<div class="humidity">
						<b-icon icon="droplet" class="mr-1"></b-icon>
						{{ weather.humidity }}%
					</div>
				</div>
			</div>
		</div>

		<!-- 未来天气预报 -->
		<div v-if="forecast.length > 0" class="forecast-weather mt-4">
			<h6 class="section-title">未来天气</h6>
			<div class="forecast-list">
				<div 
					v-for="(day, index) in forecast.slice(0, 3)" 
					:key="index"
					class="forecast-item"
					:class="{ today: index === 0 }"
				>
					<div class="forecast-date">
						<div class="day-name">{{ getDayName(index) }}</div>
						<div class="date-text">{{ formatDate(day.date) }}</div>
					</div>
					<div class="forecast-weather">
						<div class="weather-icon">
							<b-icon :icon="getWeatherIcon(day.weather_from)" class="weather-icon-svg"></b-icon>
						</div>
						<div class="weather-condition">{{ day.weather_from }}</div>
					</div>
					<div class="forecast-temps">
						<span class="high-temp">{{ weatherService.formatTemperature(day.high_temp) }}</span>
						<span class="temp-divider">/</span>
						<span class="low-temp">{{ weatherService.formatTemperature(day.low_temp) }}</span>
					</div>
					<div class="forecast-wind">
						{{ day.wind_from }}
					</div>
				</div>
			</div>
		</div>

		<!-- 加载状态 -->
		<div v-if="loading" class="loading-state">
			<b-spinner variant="primary" label="Loading..."></b-spinner>
			<p class="mt-2">正在加载天气信息...</p>
		</div>

		<!-- 错误状态 -->
		<div v-if="error" class="error-state">
			<b-alert show variant="warning">
				<h6>天气信息加载失败</h6>
				<p>{{ error }}</p>
				<b-button @click="retryLoad" variant="outline-warning" size="sm">重试</b-button>
			</b-alert>
		</div>
	</div>
</template>

<script>
import { weatherService } from '@/services/weatherService'

export default {
	name: 'WeatherWidget',
	props: {
		city: {
			type: String,
			default: '郑州'
		}
	},
	data() {
		return {
			currentWeather: null,
			forecast: [],
			realTimeWeather: [],
			updateTime: '',
			loading: false,
			error: null,
			weatherService: weatherService
		}
	},
	mounted() {
		this.loadWeatherData()
	},
	methods: {
		async loadWeatherData() {
			this.loading = true
			this.error = null

			try {
				// 并行加载所有天气数据
				const [currentWeather, forecast, realTimeWeather] = await Promise.all([
					weatherService.getCurrentWeather(this.city),
					weatherService.getForecast(this.city),
					weatherService.getRealTimeWeather(this.city)
				])

				this.currentWeather = currentWeather
				this.forecast = forecast
				this.realTimeWeather = realTimeWeather
				this.updateTime = this.getCurrentTime()

			} catch (error) {
				console.error('加载天气数据失败:', error)
				this.error = '无法加载天气信息，请稍后重试'
			} finally {
				this.loading = false
			}
		},

		async retryLoad() {
			await this.loadWeatherData()
		},

		getCurrentTime() {
			const now = new Date()
			return now.toLocaleTimeString('zh-CN', { 
				hour: '2-digit', 
				minute: '2-digit' 
			})
		},

		getDayName(index) {
			const days = ['今天', '明天', '后天']
			return days[index] || '未知'
		},

		formatDate(dateString) {
			if (!dateString) return ''
			const date = new Date(dateString)
			const month = date.getMonth() + 1
			const day = date.getDate()
			return `${month}/${day}`
		},

		isCurrentTime(timeString) {
			const now = new Date()
			const currentHour = now.getHours()
			const timeHour = parseInt(timeString.split(':')[0])
			return Math.abs(currentHour - timeHour) <= 1
		},

		getWeatherIcon(weather) {
			const weatherMap = {
				'晴': 'sun',
				'多云': 'cloud-sun',
				'阴': 'cloud',
				'小雨': 'cloud-rain',
				'中雨': 'cloud-rain-heavy',
				'大雨': 'cloud-rain-heavy',
				'雪': 'cloud-snow',
				'雾': 'cloud-fog',
				'霾': 'cloud'
			}
			return weatherMap[weather] || 'cloud'
		}
	}
}
</script>

<style scoped>
.weather-widget {
	background-color: #fff;
	border-radius: 8px;
	padding: 1.5rem;
	color: #333;
	box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.weather-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1rem;
}

.weather-title {
	margin: 0;
	font-weight: 600;
	color: #333;
}

.weather-update-time {
	font-size: 0.85rem;
	color: #666;
}

.current-weather-card {
	background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
	border-radius: 8px;
	padding: 1.5rem;
}

.weather-main {
	display: flex;
	align-items: center;
	gap: 1.5rem;
}

.weather-temp {
	text-align: center;
}

.temp-value {
	font-size: 3rem;
	font-weight: 300;
	display: block;
	line-height: 1;
}

.temp-range {
	font-size: 0.9rem;
	opacity: 0.9;
}

.weather-desc {
	flex: 1;
}

.weather-condition {
	font-size: 1.2rem;
	font-weight: 500;
	margin-bottom: 0.5rem;
}

.weather-details {
	display: flex;
	flex-wrap: wrap;
	gap: 1rem;
	font-size: 0.85rem;
	opacity: 0.9;
}

.detail-item {
	display: flex;
	align-items: center;
}

.section-title {
	color: #333;
	font-weight: 600;
	margin-bottom: 1rem;
	padding-left: 0.5rem;
	border-left: 3px solid #007bff;
}

.realtime-timeline {
	display: flex;
	overflow-x: auto;
	gap: 1rem;
	padding-bottom: 0.5rem;
}

.realtime-item {
	min-width: 80px;
	text-align: center;
	padding: 0.75rem;
	background: #f8f9fa;
	border-radius: 8px;
	transition: all 0.3s ease;
}

.realtime-item.active {
	background: #e9ecef;
	transform: scale(1.05);
}

.realtime-item .time {
	font-size: 0.8rem;
	margin-bottom: 0.25rem;
}

.weather-icon-svg {
	font-size: 1.5rem;
	margin: 0.25rem 0;
}

.realtime-item .temp {
	font-weight: 600;
	font-size: 0.9rem;
}

.realtime-item .humidity {
	font-size: 0.75rem;
	color: #666;
}

.forecast-list {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.forecast-item {
	display: flex;
	align-items: center;
	gap: 1rem;
	padding: 0.75rem;
	background: #f8f9fa;
	border-radius: 8px;
	transition: all 0.3s ease;
}

.forecast-item.today {
	background: #e9ecef;
}

.forecast-item:hover {
	background: #dee2e6;
}

.forecast-date {
	min-width: 80px;
}

.day-name {
	font-weight: 600;
	font-size: 0.9rem;
}

.date-text {
	font-size: 0.75rem;
	opacity: 0.8;
}

.forecast-weather {
	min-width: 100px;
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

.weather-condition {
	font-size: 0.85rem;
}

.forecast-temps {
	min-width: 80px;
	text-align: center;
}

.high-temp {
	font-weight: 600;
}

.temp-divider {
	margin: 0 0.25rem;
	opacity: 0.7;
}

.low-temp {
	opacity: 0.8;
}

.forecast-wind {
	min-width: 60px;
	font-size: 0.75rem;
	color: #666;
}

.loading-state, .error-state {
	text-align: center;
	padding: 2rem;
	color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
	.weather-widget {
		padding: 1rem;
	}

	.weather-main {
		flex-direction: column;
		text-align: center;
		gap: 1rem;
	}

	.weather-details {
		justify-content: center;
	}

	.realtime-timeline {
		justify-content: flex-start;
	}

	.forecast-item {
		flex-wrap: wrap;
		text-align: center;
	}

	.forecast-weather, .forecast-temps, .forecast-wind {
		min-width: auto;
		flex: 1;
	}
}
</style>