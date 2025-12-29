import axios from 'axios'

const API_BASE_URL = 'https://v2.xxapi.cn/api/yiyan'

export const poetryService = {
  /**
   * 获取每日古诗词
   * @returns {Promise<Object>} 返回古诗词信息
   */
  async getDailyPoetry() {
    try {
      const response = await axios.get(API_BASE_URL, {
        params: {
          type: 'poetry'
        }
      })
      
      if (response.data.code === 200) {
        return {
          success: true,
          data: {
            content: response.data.data,
            requestId: response.data.request_id
          },
          message: '获取成功',
          requestId: response.data.request_id
        }
      } else {
        console.error('古诗词API返回错误:', response.data.msg)
        return {
          success: false,
          data: null,
          message: response.data.msg || '获取古诗词失败',
          requestId: response.data.request_id
        }
      }
    } catch (error) {
      console.error('获取古诗词失败:', error)
      return {
        success: false,
        data: null,
        message: '网络错误，请稍后重试',
        requestId: null
      }
    }
  },

  /**
   * 获取备用古诗词（当API失败时使用）
   * @returns {Object} 备用古诗词
   */
  getBackupPoetry() {
    const backupPoems = [
      {
        content: "漫向寒炉醉玉瓶，唤君同赏小窗明。《浣溪沙·和无咎韵》 — 陆游",
        author: "陆游",
        title: "浣溪沙·和无咎韵"
      },
      {
        content: "静夜思 — 床前明月光，疑是地上霜。举头望明月，低头思故乡。",
        author: "李白",
        title: "静夜思"
      },
      {
        content: "春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。",
        author: "孟浩然",
        title: "春晓"
      },
      {
        content: "锄禾日当午，汗滴禾下土。谁知盘中餐，粒粒皆辛苦。",
        author: "李绅",
        title: "悯农"
      },
      {
        content: "白日依山尽，黄河入海流。欲穷千里目，更上一层楼。",
        author: "王之涣",
        title: "登鹳雀楼"
      }
    ]
    
    const randomIndex = Math.floor(Math.random() * backupPoems.length)
    return {
      success: true,
      data: {
        content: backupPoems[randomIndex].content,
        requestId: 'backup-' + Date.now()
      },
      message: '使用备用数据',
      requestId: 'backup'
    }
  }
}