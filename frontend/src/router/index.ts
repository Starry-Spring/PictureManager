import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import Layout from '../components/Layout.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'Login',
            component: () => import('../views/LoginView.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/register',
            name: 'Register',
            component: () => import('../views/RegisterView.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/',
            component: Layout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    redirect: '/gallery'
                },
                {
                    path: 'gallery',
                    name: 'Gallery',
                    component: () => import('../views/GalleryView.vue')
                },
                {
                    path: 'dashboard',
                    name: 'Dashboard',
                    component: () => import('../views/DashboardView.vue')
                },
                {
                    path: 'user',
                    name: 'User',
                    component: () => import('../views/UserView.vue')
                },
                {
                    path: 'image/:id',
                    name: 'ImageDetail',
                    component: () => import('../views/ImageDetailView.vue')
                },
                {
                    path: 'ai',
                    name: 'AI',
                    component: () => import('../views/AIView.vue')
                }
            ]
        }
    ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    
    // 确保认证状态已初始化（只会执行一次）
    if (!userStore.initialized) {
        await userStore.initializeAuth()
    }

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
    
    if (requiresAuth && !userStore.isAuthenticated) {
        // 需要认证但未登录，跳转到登录页
        next('/login')
    } else if ((to.name === 'Login' || to.name === 'Register') && userStore.isAuthenticated) {
        // 已登录用户访问登录/注册页，跳转到图片库
        next('/gallery')
    } else {
        // 正常放行
        next()
    }
})

export default router