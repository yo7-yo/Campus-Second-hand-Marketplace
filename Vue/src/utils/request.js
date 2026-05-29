import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.response.use(
  res => res,
  err => {
    const data = err.response?.data
    let msg = '请求失败'
    if (data && typeof data === 'object') {
      msg = data.error || data.message || msg
    } else if (typeof data === 'string' && data.length < 200) {
      msg = data
    } else if (err.message) {
      msg = err.message
    }
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
