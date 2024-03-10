import { createRouter, createWebHistory } from "vue-router";

import MainPage from "@/pages/MainPage.vue";
import AuthSignupPage from "@/pages/AuthSignupPage.vue";
import SignupPage from "@/pages/SignupPage.vue";
import MyActivePage from "@/pages/MyActivePage.vue";
import SearchResultPage from "@/pages/SearchResultPage.vue";
import BoardWritePage from "@/pages/BoardWritePage.vue";
import ReviewWritePage from "@/pages/ReviewWritePage.vue";
import StudyBoardPage from "@/pages/StudyBoardPage.vue";
import MainSignupPage from "@/pages/MainSignupPage.vue";
import BoardDetailsPage from "@/pages/BoardDetailsPage.vue";
import BoardListPage from "@/pages/BoardListPage.vue";
import EmailValidationPage from "@/pages/EmailValidationPage.vue";
import AdminMainPage from "@/pages/AdminMainPage.vue";

import AdminWithdrawPage from "@/pages/AdminWithdrawPage.vue";

const routes = [
  { path: "/", component: MainPage },
  { path: "/auth/signup", component: AuthSignupPage },
  { path: "/signup", component: SignupPage },
  { path: "/active", component: MyActivePage },
  { path: "/result", component: SearchResultPage },
  { path: "/board/new", component: BoardWritePage },
  { path: "/review/new", component: ReviewWritePage },
  { path: "/study", component: StudyBoardPage },
  { path: "/board/detail", component: BoardDetailsPage },
  { path: "/board", component: BoardListPage },
  { path: "/select/signup", component: MainSignupPage },
  { path: "/email/verify", component: EmailValidationPage },
  { path: "/admin", component: AdminMainPage },

  { path: "/admin/withdraw", component: AdminWithdrawPage },
];

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
});

export default router;
