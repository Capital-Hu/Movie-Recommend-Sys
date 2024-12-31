import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import login from '../pages/login/login'
import register from '../pages/register/register'
import home from '../pages/home/home'
import moviedetail from "@/pages/moviedetail/moviedetail";
import genremovie from "@/pages/genremovie/genremovie"
import searchresult from "@/pages/searchresult/searchresult"

const routes = [
  {
    path: '/',
    name: 'main',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: login
  },
  {
    path: '/register',
    name: 'register',
    component: register
  },
  {
    path: '/home',
    name: 'home',
    component: home
  },
  {
    path: '/moviedetail',
    name: 'moviedetail',
    component: moviedetail
  },
  {
    path: '/genremovie',
    name: 'genremovie',
    component: genremovie
  },
  {
    path: '/searchresult',
    name: 'searchresult',
    component: searchresult
  },
  {
    path: '/about',
    name: 'about',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
