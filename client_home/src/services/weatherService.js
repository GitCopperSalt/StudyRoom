import axios from 'axios'

const API_BASE_URL = 'https://v2.xxapi.cn/api/weatherDetails'
const API_KEY = 'f7016be2271ae751'
const DEFAULT_CITY = '郑州'

export const weatherService = {
  // 获取城市天气详情
  async getWeatherDetails(city = DEFAULT_CITY) {
    try {
      const response = await axios.get(API_BASE_URL, {
        params: {
          city: city,
          key: API_KEY
        }
      })
      
      if (response.data.code === 200) {
        return response.data.data
      } else {
        console.error('天气API返回错误:', response.data.msg)
        return this.getMockData()
      }
    } catch (error) {
      console.error('获取天气信息失败:', error)
      return this.getMockData()
    }
  },

  // 获取当前天气信息
  async getCurrentWeather(city = DEFAULT_CITY) {
    try {
      const data = await this.getWeatherDetails(city)
      if (data && data.data && data.data.length > 0) {
        const today = data.data[0]
        const currentHour = new Date().getHours()
        
        // 获取当前时间的天气数据
        let currentWeather = today.real_time_weather[0]
        
        // 找到最接近当前时间的天气数据
        if (today.real_time_weather && today.real_time_weather.length > 0) {
          for (let i = 0; i < today.real_time_weather.length; i++) {
            const time = parseInt(today.real_time_weather[i].time.split(':')[0])
            if (time <= currentHour) {
              currentWeather = today.real_time_weather[i]
            } else {
              break
            }
          }
        }

        return {
          city: data.city,
          date: today.date,
          day: today.day,
          high_temp: today.high_temp,
          low_temp: today.low_temp,
          current_temp: currentWeather.temperature,
          current_weather: currentWeather.weather,
          humidity: currentWeather.humidity,
          wind_speed: currentWeather.wind_speed,
          wind_dir: currentWeather.wind_dir,
          pressure: currentWeather.pressure,
          description: currentWeather.description
        }
      }
      return null
    } catch (error) {
      console.error('获取当前天气失败:', error)
      return null
    }
  },

  // 获取未来几天天气
  async getForecast(city = DEFAULT_CITY) {
    try {
      const data = await this.getWeatherDetails(city)
      if (data && data.data) {
        return data.data.map(day => ({
          date: day.date,
          day: day.day,
          high_temp: day.high_temp,
          low_temp: day.low_temp,
          weather_from: day.weather_from,
          weather_to: day.weather_to,
          wind_from: day.wind_from,
          wind_to: day.wind_to
        }))
      }
      return []
    } catch (error) {
      console.error('获取天气预报失败:', error)
      return []
    }
  },

  // 获取实时天气数据
  async getRealTimeWeather(city = DEFAULT_CITY) {
    try {
      const data = await this.getWeatherDetails(city)
      if (data && data.data && data.data.length > 0) {
        const today = data.data[0]
        return today.real_time_weather || []
      }
      return []
    } catch (error) {
      console.error('获取实时天气失败:', error)
      return []
    }
  },

  // 格式化温度
  formatTemperature(temp) {
    if (temp === undefined || temp === null) return '--'
    return `${temp}°C`
  },

  // 格式化风向
  formatWindDirection(dir) {
    const directions = {
      'N': '北风',
      'NE': '东北风',
      'E': '东风',
      'SE': '东南风',
      'S': '南风',
      'SW': '西南风',
      'W': '西风',
      'NW': '西北风'
    }
    return directions[dir] || dir
  },

  // 获取模拟数据作为降级方案
  getMockData() {
    return {
      city: "郑州",
      data: [
        {
          date: "2025-12-26",
          day: "星期四",
          high_temp: 8,
          low_temp: 2,
          weather_from: "多云",
          weather_to: "晴",
          wind_from: "北风",
          wind_to: "微风",
          real_time_weather: [
            {
              time: "08:00",
              temperature: 3,
              weather: "多云",
              humidity: 65,
              wind_speed: 3,
              wind_dir: "N",
              pressure: 1013,
              precipitation: 0,
              cloud_cover: 40,
              description: "多云，气温较低"
            },
            {
              time: "14:00",
              temperature: 7,
              weather: "晴",
              humidity: 45,
              wind_speed: 2,
              wind_dir: "N",
              pressure: 1015,
              precipitation: 0,
              cloud_cover: 20,
              description: "晴间多云，气温适宜"
            },
            {
              time: "20:00",
              temperature: 4,
              weather: "晴",
              humidity: 55,
              wind_speed: 2,
              wind_dir: "N",
              pressure: 1016,
              precipitation: 0,
              cloud_cover: 10,
              description: "晴朗，夜间较冷"
            }
          ]
        },
        {
          date: "2025-12-27",
          day: "星期五",
          high_temp: 6,
          low_temp: 0,
          weather_from: "晴",
          weather_to: "多云",
          wind_from: "北风",
          wind_to: "微风",
          real_time_weather: []
        },
        {
          date: "2025-12-28",
          day: "星期六",
          high_temp: 9,
          low_temp: 1,
          weather_from: "晴",
          weather_to: "多云",
          wind_from: "南风",
          wind_to: "微风",
          real_time_weather: []
        }
      ]
    }
  }
}