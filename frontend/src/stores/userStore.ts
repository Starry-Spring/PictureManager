// stores/userStore.ts
import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null,
        isAuthenticated: false
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
                console.log('===== ====')
                const response = await axios.post('/api/auth/login', {
                    usernameOrEmail,
                    password
                })
                this.user = response.data
                this.isAuthenticated = true
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
            this.isAuthenticated = false
        }
    }
})