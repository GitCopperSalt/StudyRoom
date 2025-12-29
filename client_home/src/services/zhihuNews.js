import axios from 'axios'

const API_BASE_URL = 'https://v.api.aa1.cn/api/zhihu-news'

export const zhihuNewsService = {
  // 获取知乎每日新闻
  async getDailyNews() {
    try {
      const response = await axios.get(`${API_BASE_URL}/index.php?aa1=xiarou`)
      return response.data
    } catch (error) {
      console.error('获取知乎新闻失败:', error)
      // 返回模拟数据作为降级方案
      return this.getMockData()
    }
  },

  // 获取新闻列表（处理后的数据格式）
  async getNewsList() {
    try {
      const data = await this.getDailyNews()
      if (data.news) {
        return data.news.map(item => ({
          id: item.id,
          title: item.title,
          image: item.image,
          thumbnail: item.thumbnail,
          hint: item.hint,
          url: item.url,
          share_url: item.share_url,
          image_hue: item.image_hue,
          ga_prefix: item.ga_prefix
        }))
      }
      return []
    } catch (error) {
      console.error('获取新闻列表失败:', error)
      return []
    }
  },

  // 获取头条新闻
  async getTopStories() {
    try {
      const data = await this.getDailyNews()
      if (data.top_stories) {
        return data.top_stories.map(item => ({
          id: item.id,
          title: item.title,
          image: item.image,
          hint: item.hint,
          url: item.url,
          share_url: item.share_url,
          image_hue: item.image_hue,
          ga_prefix: item.ga_prefix,
          image_source: item.image_source
        }))
      }
      return []
    } catch (error) {
      console.error('获取头条新闻失败:', error)
      return []
    }
  },

  // 格式化日期
  formatDate(dateString) {
    if (!dateString) return ''
    const year = dateString.substring(0, 4)
    const month = dateString.substring(4, 6)
    const day = dateString.substring(6, 8)
    return `${year}年${month}月${day}日`
  },

  // 获取模拟数据作为降级方案
  getMockData() {
    return {
      date: "20211224",
      news: [
        {
          "image_hue": "0xb39e7d",
          "title": "「东北大米」和「南方大米」有哪些区别？",
          "url": "https://daily.zhihu.com/story/9743658",
          "image": "https://pica.zhimg.com/v2-fa83f855d3492183c4957bac445662bb.jpg?source=8673f162",
          "hint": "朴朴超市 · 1 分钟阅读",
          "share_url": "http://daily.zhihu.com/story/9743658",
          "thumbnail": "https://pic2.zhimg.com/v2-a5ed468c9b6b115485c1b2295a061eb9.jpg?source=8673f162",
          "ga_prefix": "122407",
          "id": 9743658
        },
        {
          "image_hue": "0x7b5658",
          "title": "装置艺术和建筑学有什么关系？",
          "url": "https://daily.zhihu.com/story/9743655",
          "image": "https://pic2.zhimg.com/v2-811786b1f919bb0979d9ad5b4f19a88e.jpg?source=8673f162",
          "hint": "知乎用户 · 2 分钟阅读",
          "share_url": "http://daily.zhihu.com/story/9743655",
          "thumbnail": "https://pic1.zhimg.com/v2-0fe10dfb0f7e83c94d02f0278a605150.jpg?source=8673f162",
          "ga_prefix": "122407",
          "id": 9743655
        },
        {
          "image_hue": "0x202c2e",
          "title": "杠铃杆分为哪几种类型？不同类型所用的滚花也是不同的吗？",
          "url": "https://daily.zhihu.com/story/9743661",
          "image": "https://pic1.zhimg.com/v2-4f432e109b20e20aba6e0f84003b0112.jpg?source=8673f162",
          "hint": "kmlover · 4 分钟阅读",
          "share_url": "http://daily.zhihu.com/story/9743661",
          "thumbnail": "https://pica.zhimg.com/v2-4ab03c403d6eb0f3f0cfd350c3e80862.jpg?source=8673f162",
          "ga_prefix": "122407",
          "id": 9743661
        },
        {
          "image_hue": "0xb3957d",
          "title": "弹钢琴不都是按固定按键吗，为什么有技艺高低之分？",
          "url": "https://daily.zhihu.com/story/9743649",
          "image": "https://pic1.zhimg.com/v2-2f6cd7299d04aecf35dd487b59bd6248.jpg?source=8673f162",
          "hint": "圭多达莱佐 · 1 分钟阅读",
          "share_url": "http://daily.zhihu.com/story/9743649",
          "thumbnail": "https://pica.zhimg.com/v2-9020ca4fb321261a63ff915f67c66cb9.jpg?source=8673f162",
          "ga_prefix": "122407",
          "id": 9743649
        },
        {
          "image_hue": "0xb3867d",
          "title": "大误 · 用什么睡姿抱女友睡觉最舒服？",
          "url": "https://daily.zhihu.com/story/9743630",
          "image": "https://pica.zhimg.com/v2-07b3126ba5a9e662825b77bf51cb8a09.jpg?source=8673f162",
          "hint": "丁香医生 · 2 分钟阅读",
          "share_url": "http://daily.zhihu.com/story/9743630",
          "thumbnail": "https://pic2.zhimg.com/v2-98514a5075515728b31aa44b623ab66c.jpg?source=8673f162",
          "ga_prefix": "122407",
          "id": 9743630
        },
        {
          "image_hue": "0x413045",
          "title": "瞎扯 · 如何正确地吐槽",
          "url": "https://daily.zhihu.com/story/9743687",
          "image": "https://pic3.zhimg.com/v2-d9b3363931476f6b9abab5ee55c9f30e.jpg?source=8673f162",
          "hint": "VOL.2818",
          "share_url": "http://daily.zhihu.com/story/9743687",
          "thumbnail": "https://pic1.zhimg.com/v2-3f5cdf192c17cb4ab0160a56c48a56ad.jpg?source=8673f162",
          "ga_prefix": "122406",
          "id": 9743687
        }
      ],
      "is_today": true,
      "top_stories": [
        {
          "image_hue": "0x806e52",
          "image_source": "",
          "hint": "作者 / 博物",
          "url": "https://daily.zhihu.com/story/9743263",
          "image": "https://pic1.zhimg.com/v2-1a619e3eea42084dad9ef118f6c72c87.jpg?source=8673f162",
          "title": "用臭鼬的臭臭臭鼬，臭鼬会被臭死吗？",
          "share_url": "http://daily.zhihu.com/story/9743263",
          "ga_prefix": "121507",
          "id": 9743263
        },
        {
          "image_hue": "0x363625",
          "image_source": "",
          "hint": "作者 / 了不起的苏小姐",
          "url": "https://daily.zhihu.com/story/9743204",
          "image": "https://pic3.zhimg.com/v2-3b9ef2df261d1e28fbdd702bf10b2819.jpg?source=8673f162",
          "title": "有哪些隐含暗喻或细节、情节的名画？",
          "share_url": "http://daily.zhihu.com/story/9743204",
          "ga_prefix": "121207",
          "id": 9743204
        },
        {
          "image_hue": "0x2b323d",
          "image_source": "",
          "hint": "作者 / 中国数字科技馆",
          "url": "https://daily.zhihu.com/story/9743101",
          "image": "https://pic3.zhimg.com/v2-3c8a6ba4b4c6b6e4c6b0b0e0f0e0e0f0.jpg?source=8673f162",
          "title": "为什么有时候会认不出熟人？",
          "share_url": "http://daily.zhihu.com/story/9743101",
          "ga_prefix": "120907",
          "id": 9743101
        }
      ]
    }
  }
}