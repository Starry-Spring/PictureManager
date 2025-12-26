import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import Layout from '@/components/Layout.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/LoginView.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/register',
            name: 'Register',
            component: () => import('@/views/RegisterView.vue'),
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
                    component: () => import('@/views/GalleryView.vue')
                },
                {
                    path: 'dashboard',
                    name: 'Dashboard',
                    component: () => import('@/views/DashboardView.vue')
                },
                {
                    path: 'user',
                    name: 'User',
                    component: () => import('@/views/UserView.vue')
                },
                {
                    path: 'image/:id',
                    name: 'ImageDetail',
                    component: () => import('@/views/ImageDetailView.vue')
                },
                {
                    path: 'ai',
                    name: 'AI',
                    component: () => import('@/views/AIView.vue')
                }
            ]
        }
    ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    
    // 如果尚未初始化认证状态，尝试初始化
    if (!userStore.isAuthenticated && !userStore.initialized) {
        await userStore.initializeAuth()
        userStore.initialized = true
    }

    if (to.meta.requiresAuth && !userStore.isAuthenticated) {
        next('/login')
    } else if ((to.name === 'Login' || to.name === 'Register') && userStore.isAuthenticated) {
        next('/gallery')
    } else {
        next()
    }
})

export default router