// stores/userStore.ts
import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null,
        isAuthenticated: false,
        token: localStorage.getItem('token') || ''
    }),

    actions: {
        async register(userData) {
            try {
                const response = await axios.post('/api/auth/register', userData)
                this.user = response.data
                this.isAuthenticated = true
                return { success: true, data: response.data }
            } catch (error) {
                console.error('注册错误:', error)
                return {
                    success: false,
                    message: error.response?.data?.message || error.message
                }
            }
        },

        async login(usernameOrEmail, password) {
            try {
                const response = await axios.post('/api/auth/login', {
                    usernameOrEmail,
                    password
                })
                this.user = response.data
                this.token = response.data.token
                this.isAuthenticated = true
                
                // 将token存储到localStorage中
                localStorage.setItem('token', response.data.token)
                
                // 设置axios默认请求头
                axios.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`
                
                return { success: true, data: response.data }
            } catch (error) {
                console.error('登录错误:', error)
                return {
                    success: false,
                    message: error.response?.data?.message || error.message
                }
            }
        },

        logout() {
            this.user = null
            this.token = ''
            this.isAuthenticated = false
            
            // 清除localStorage中的token
            localStorage.removeItem('token')
            
            // 清除axios请求头
            delete axios.defaults.headers.common['Authorization']
        },
        
        async initializeAuth() {
            const token = localStorage.getItem('token')
            if (token) {
                this.token = token
                // 设置axios默认请求头
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
                
                // 尝试获取用户信息以验证令牌是否有效
                try {
                    const response = await axios.get('/api/auth/verify')
                    this.user = response.data
                    this.isAuthenticated = true
                } catch (error) {
                    // 如果令牌无效，清除本地存储
                    console.error('令牌验证失败:', error)
                    this.logout()
                }
            }
        }
    }
})