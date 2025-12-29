import axios from 'axios'

const API_BASE_URL = 'https://cn.apihz.cn/api/zici/today.php'
// 使用公共ID和KEY，您可以在 http://www.apihz.cn 注册获取自己的ID和KEY
const PUBLIC_ID = '88888888'
const PUBLIC_KEY = '88888888'

export const historyTextService = {
  /**
   * 获取历史上的今天文字事件
   * @param {number} month - 月份 (1-12)，可选
   * @param {number} day - 日期 (1-31)，可选
   * @returns {Promise<Object>} 返回历史事件信息
   */
  async getTodayInHistoryText(month = null, day = null) {
    try {
      const params = {
        id: PUBLIC_ID,
        key: PUBLIC_KEY
      }
      
      // 如果提供了月和日参数，添加到请求中
      if (month && day) {
        params.m = month
        params.d = day
      }
      
      const response = await axios.get(API_BASE_URL, {
        params: params
      })
      
      if (response.data.code === 200) {
        return {
          success: true,
          data: {
            title: response.data.title,
            year: response.data.y,
            month: response.data.m,
            day: response.data.d,
            url: response.data.url,
            keywords: response.data.words,
            fullDate: `${response.data.y}年${response.data.m}月${response.data.d}日`
          },
          message: '获取成功',
          requestId: Date.now().toString()
        }
      } else {
        console.error('历史上的今天文字API返回错误:', response.data.msg)
        return {
          success: false,
          data: null,
          message: response.data.msg || '获取历史上的今天文字信息失败',
          requestId: Date.now().toString()
        }
      }
    } catch (error) {
      console.error('获取历史上的今天文字信息失败:', error)
      return {
        success: false,
        data: null,
        message: '网络错误，请稍后重试',
        requestId: null
      }
    }
  },

  /**
   * 获取指定日期的历史事件
   * @param {number} month - 月份 (1-12)
   * @param {number} day - 日期 (1-31)
   * @returns {Promise<Object>} 返回历史事件信息
   */
  async getHistoryByDate(month, day) {
    return this.getTodayInHistoryText(month, day)
  },

  /**
   * 获取今天的日期信息
   * @returns {Object} 日期信息
   */
  getTodayDateInfo() {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    const day = now.getDate()
    
    const monthNames = [
      '一月', '二月', '三月', '四月', '五月', '六月',
      '七月', '八月', '九月', '十月', '十一月', '十二月'
    ]
    
    const weekNames = [
      '星期日', '星期一', '星期二', '星期三', 
      '星期四', '星期五', '星期六'
    ]
    
    return {
      year,
      month,
      day,
      week: weekNames[now.getDay()],
      monthName: monthNames[month - 1],
      dateString: `${month}月${day}日`,
      fullDateString: `${year}年${month}月${day}日 ${weekNames[now.getDay()]}`,
      monthDayString: `${month}/${day}`
    }
  }
}