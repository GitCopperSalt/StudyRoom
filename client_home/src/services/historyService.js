import axios from 'axios'

const API_BASE_URL = 'https://v2.xxapi.cn/api/historypic'

export const historyService = {
  // 获取历史上的今天图片
  async getTodayInHistory() {
    try {
      const response = await axios.get(API_BASE_URL)
      
      if (response.data.code === 200) {
        return {
          success: true,
          imageUrl: response.data.data,
          message: response.data.msg,
          requestId: response.data.request_id
        }
      } else {
        console.error('历史上的今天API返回错误:', response.data.msg)
        return {
          success: false,
          imageUrl: null,
          message: response.data.msg || '获取历史上的今天失败',
          requestId: response.data.request_id
        }
      }
    } catch (error) {
      console.error('获取历史上的今天失败:', error)
      return {
        success: false,
        imageUrl: null,
        message: '网络错误，请稍后重试',
        requestId: null
      }
    }
  },

  // 获取历史上的今天图片（直接跳转模式）
  async getTodayInHistoryRedirect() {
    try {
      const response = await axios.get(`${API_BASE_URL}?return=302`, {
        maxRedirects: 0,
        validateStatus: function (status) {
          return status === 302
        }
      })
      
      if (response.status === 302 && response.headers.location) {
        return {
          success: true,
          imageUrl: response.headers.location,
          message: '获取成功',
          requestId: null
        }
      } else {
        throw new Error('未获取到重定向地址')
      }
    } catch (error) {
      console.error('获取历史上的今天重定向失败:', error)
      return {
        success: false,
        imageUrl: null,
        message: '获取历史上的今天失败',
        requestId: null
      }
    }
  },

  // 获取今日日期信息
  getTodayDateInfo() {
    const today = new Date()
    const year = today.getFullYear()
    const month = today.getMonth() + 1
    const day = today.getDate()
    const weekDay = today.toLocaleDateString('zh-CN', { weekday: 'long' })
    
    return {
      year,
      month,
      day,
      weekDay,
      dateString: `${month}月${day}日`,
      fullDateString: `${year}年${month}月${day}日 ${weekDay}`
    }
  },

  // 获取历史趣闻（模拟数据）
  getHistoryFacts() {
    const facts = [
      '今天是计算机科学的重要日子',
      '文学史上诞生了伟大的作品',
      '科技领域取得了重大突破',
      '艺术领域留下了珍贵遗产',
      '历史事件改变了世界进程'
    ]
    
    const randomFact = facts[Math.floor(Math.random() * facts.length)]
    return randomFact
  },

  // 获取模拟历史数据（降级方案）
  getMockData() {
    return {
      success: true,
      imageUrl: 'https://cdn.xxhzm.cn/v2api/cache/history/2024-12-26.jpg',
      message: '历史上的今天',
      requestId: 'mock_' + Date.now()
    }
  }
}