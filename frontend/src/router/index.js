import { createRouter, createWebHistory } from "vue-router";

import MainPage from "@/pages/MainPage.vue";
import AuthSignupPage from "@/pages/AuthSignupPage.vue";
import SignupPage from "@/pages/SignupPage.vue";
import MyActivePage from "@/pages/MyActivePage.vue";
import MyProfilePage from "@/pages/MyProfilePage.vue";
import SearchResultPage from "@/pages/SearchResultPage.vue";
import BoardWritePage from "@/pages/BoardWritePage.vue";
import ReviewWritePage from "@/pages/ReviewWritePage.vue";
import StudyBoardPage from "@/pages/StudyBoardPage.vue";
import SelectSignupPage from "@/pages/SelectSignupPage.vue";
import BoardDetailsPage from "@/pages/BoardDetailsPage.vue";
import ReviewDetailsPage from "@/pages/ReviewDetailsPage.vue";
import ReviewListPage from "@/pages/ReviewListPage.vue";
import EmailValidationPage from "@/pages/EmailValidationPage.vue";
import NoticeBoardPage from "@/pages/NoticeBoardPage.vue";
import KakaoLogIn from "@/pages/KakaoLogIn.vue";

import AdminMainPage from "@/pages/AdminMainPage.vue";
import AdminWithdrawPage from "@/pages/AdminWithdrawPage.vue";
import AdminCategoryRegisterPage from "@/pages/AdminCategoryRegisterPage.vue";
import AdminTagRegisterPage from "@/pages/AdminTagRegisterPage.vue";
import KnowledgeBoardListPage from "@/pages/KnowledgeBoardListPage.vue";
import QnABoardListPage from "@/pages/QnABoardListPage.vue";

const requireAuth = () => (from, to, next) => {
  const token = localStorage.getItem("token");
  if (token) {
    return next();
  }
  next("/login");
}

/*
const requireAdminAuth = () => (from, to, next) => {
  const token = localStorage.getItem("token");
  if (token) {
    return next();
  }
  next("/admin");
}
*/

const routes = [
  { path: "/", component: MainPage },
  {
    path: "/KakaoLogIn/:token",
    name: "KakaoLogIn",
    component: () => import("@/pages/KakaoLogIn.vue"),
    props: true,
  },
  { path: "/auth/signup", component: AuthSignupPage },
  { path: "/KakaoLogIn", component: KakaoLogIn },
  { path: "/signup", component: SignupPage },
  { path: "/profile", component: MyProfilePage, beforeEnter: requireAuth() },
  { path: "/mypage", component: MyActivePage, beforeEnter: requireAuth() },
  { path: "/result", component: SearchResultPage },
  { path: "/board/new", component: BoardWritePage, beforeEnter: requireAuth() },
  { path: "/review/new", component: ReviewWritePage, beforeEnter: requireAuth() },
  { path: "/study", component: StudyBoardPage },
  { path: "/board/:boardIdx", component: BoardDetailsPage },
  { path: "/review/:idx", component: ReviewDetailsPage },
  { path: "/board/knowledge", component: KnowledgeBoardListPage },
  { path: "/board/qna", component: QnABoardListPage },
  { path: "/review", component: ReviewListPage },
  { path: "/select/signup", component: SelectSignupPage },
  { path: "/email/verify", component: EmailValidationPage },
  { path: "/notice", component: NoticeBoardPage },
  { path: "/admin", component: AdminMainPage, },
  { path: "/admin/withdraw", component: AdminWithdrawPage },
  { path: "/admin/category/register", component: AdminCategoryRegisterPage },
  { path: "/admin/tag/register", component: AdminTagRegisterPage },
];

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
});

export default router;
