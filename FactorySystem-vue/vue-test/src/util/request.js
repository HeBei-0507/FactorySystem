import axios from "axios"

const baseURL = '/api'


const instance = axios.create({
  //基地址+超时时间
  baseURL,
  timeout: 10000
})

//响应拦截器
instance.interceptors.response.use(
  (res) => {
    // 检查响应数据格式
    // if(res.data && res.data.code === 200) {
    //   return res
    // }
    // // 如果没有code字段，可能是直接返回数据，也允许通过
    // if(res.status === 200 && res.data) {
    //   return res
    // }
    // //处理业务失败
    // return Promise.reject(res.data || res)
    console.log("响应拦截器输出：\n", res)
    return res.data
  },
  (err) => {
    //处理出现401
    // if(err.response?.status === 401) {
    //   const tokenStore = useTokenStore()
    //   // 清除token
    //   tokenStore.setToken('')
    //   showFailToast('请先登录')
    //   // 确保router存在再跳转
    //   if(router && typeof router.push === 'function') {
    //     // 避免重复跳转到登录页
    //     if(window.location.hash !== '#/login') {
    //       router.push('/login')
    //     }
    //   }
    // }
    //错误的默认情况
    return Promise.reject(err)
  }
)

export default instance
export {baseURL}