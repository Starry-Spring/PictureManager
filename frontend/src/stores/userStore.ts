// stores/userStore.ts
import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null as any,
        isAuthenticated: false,
        token: localStorage.getItem('token') || '',
        initialized: false
    }),

    actions: {
        async register(userData: any) {
            try {
                const response = await axios.post('/api/auth/register', userData)
                this.user = response.data
                this.isAuthenticated = true
                return { success: true, data: response.data }
            } catch (error: any) {
                console.error('注册错误:', error)
                return {
                    success: false,
                    message: error.response?.data?.message || error.message
                }
            }
        },

        async login(usernameOrEmail: string, password: string) {
            try {
                const response = await axios.post('/api/auth/login', {
                    usernameOrEmail,
                    password
                })
                this.user = response.data
                this.token = response.data.token
                this.isAuthenticated = true
                this.initialized = true
                
                // 将token存储到localStorage中
                localStorage.setItem('token', response.data.token)
                
                // 设置axios默认请求头
                axios.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`
                
                return { success: true, data: response.data }
            } catch (error: any) {
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
            // 注意：不重置 initialized，避免重新触发 initializeAuth
            
            // 清除localStorage中的token
            localStorage.removeItem('token')
            
            // 清除axios请求头
            delete axios.defaults.headers.common['Authorization']
        },
        
        async initializeAuth() {
            // 如果已经初始化过，不再重复执行
            if (this.initialized) {
                return
            }
            
            const token = localStorage.getItem('token')
            
            if (!token) {
                // 没有token，直接标记为已初始化
                this.isAuthenticated = false
                this.initialized = true
                return
            }
            
            // 设置token和axios请求头
            this.token = token
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
            
            try {
                // 尝试验证token
                const response = await axios.get('/api/auth/verify')
                this.user = response.data
                this.isAuthenticated = true
            } catch (error: any) {
                console.error('Token验证失败:', error)
                // token无效，清除登录状态但不影响 initialized
                this.user = null
                this.token = ''
                this.isAuthenticated = false
                localStorage.removeItem('token')
                delete axios.defaults.headers.common['Authorization']
            } finally {
                // 无论成功还是失败，都标记为已初始化
                this.initialized = true
            }
        }
    }
})