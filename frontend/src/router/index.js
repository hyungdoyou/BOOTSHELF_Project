import { createRouter, createWebHistory } from "vue-router";

import MainPage from "@/pages/MainPage.vue";
import AuthSignupPage from "@/pages/AuthSignupPage.vue";
import SignupPage from '@/pages/SignupPage.vue';
import SearchResultPage from '@/pages/SearchResultPage.vue';
import BoardWritePage from '@/pages/BoardWritePage.vue';
import StudyBoardPage from '@/pages/StudyBoardPage.vue'

const routes = [
  { path: "/", component: MainPage },
  { path: '/authsignup', component: AuthSignupPage },
  { path: '/signup', component: SignupPage},
  { path: '/result', component: SearchResultPage},
  { path: '/boardwrite', component: BoardWritePage},
  { path: '/study', component: StudyBoardPage},
];

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
});


export default router;