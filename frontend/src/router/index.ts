import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/userStore'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/login'
        },
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
            path: '/dashboard',
            name: 'Dashboard',
            component: () => import('../views/DashboardView.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/gallery',
            name: 'Gallery',
            component: () => import('../views/GalleryView.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/user',
            name: 'User',
            component: () => import('../views/UserView.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/image/:id',
            name: 'ImageDetail',
            component: () => import('../views/ImageDetailView.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/ai',
            name: 'AI',
            component: () => import('../views/AIView.vue'),
            meta: { requiresAuth: true }
        }
    ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.isAuthenticated) {
        next('/login')
    } else if ((to.name === 'Login' || to.name === 'Register') && userStore.isAuthenticated) {
        next('/gallery')
    } else {
        next()
    }
})

export default router