import { defineStore } from "pinia";
import axios from "axios";
import VueJwtDecode from "vue-jwt-decode";

const backend = "http://192.168.0.61/api";
// const backend = "http://localhost:9999";

const accessToken = localStorage.getItem("accessToken");
const refreshToken = localStorage.getItem("refreshToken");

export const useBoardStore = defineStore("board", {
  state: () => ({
    boardList: [],
    currentPage: 0,
    totalPages: 0,
    totalCnt: 0,
    boardDetail: [],
    tagList: [],
    tagIdx: 0,
    tagName: "",
    previousPath: "",
    isBoardExist: true,
    isLoading: false,
    fromEdit: false,
    isPageExist: true,
    isTokenExpired: false,
  }),

  actions: {

    validateToken() {
      const decodedAccessToken = VueJwtDecode.decode(accessToken);
      const expirationTime = decodedAccessToken.exp;
      const currentTime = Math.floor(Date.now() / 1000);

      if (expirationTime - currentTime < 30) {
        this.isTokenExpired = true;
      } else {
        this.isTokenExpired = false;
      }
    },

    async createBoard(board) {

      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };


        let response = await axios.post(backend + `/main/board/create`, board, {
          headers
        });

        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }
        if (response.data.isSuccess === true) {
          alert("게시글이 등록되었습니다.");
          window.location.href = "/board/" + response.data.result.boardIdx;
          this.fromEdit = true;
        }
      } catch (e) {
        if (e.response && e.response.data) {
          console.log(e.response.data);
          if (e.response.data.code === "BOARD-002") {
            alert("이미 사용중인 제목입니다. 제목을 변경해주세요.");
          } else if (e.response.data.code === "COMMON-001") {
            alert("카테고리, 제목은 필수로 입력하셔야 합니다.")
          }
        }
      }
    },
    async getBoardListByQuery(query, option, page = 1) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
          "/main/board/search?query=" +
          query +
          "&searchType=" +
          option +
          "&page=" +
          (page - 1)
        );
        this.boardList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalCnt;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },

    /* es 검색!!! */
    async getCategoryBoardListByQuery(
      boardCategoryIdx,
      title,
      option,
      page = 1
    ) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
          "/search/board/list/scroll" +
          "?categoryIdx=" +
          boardCategoryIdx +
          "&title=" +
          title +
          "&sortType=" +
          option
        );
        this.boardList = response.data.result.list;

        this.lastSearchAfter = response.data.result.lastSearchAfter;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalHits;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },

    /* es 더보기 */
    async getBoardListByQueryNext(categoryIdx, sortType, title, searchAfterStr) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          `${backend}/search/board/list/scroll?categoryIdx=${categoryIdx}&sortType=${sortType}&title=${title}&searchAfterStr=${searchAfterStr}`
        );

        if (response.data.result.list.length === 0) {
          this.noMoreData = true; // 데이터가 더 이상 없음을 표시
          this.isLoading = false;  // 로딩 상태 해제
          setTimeout(() => alert("마지막 게시글입니다."), 250); // alert 창을 약간 지연시켜 띄움
        } else {
          // 새로운 검색 결과를 기존 목록에 추가
          this.boardList = [...this.boardList, ...response.data.result.list];

          this.totalCnt = response.data.result.totalHits;
          this.searchAfterStr = `${response.data.result.lastSearchAfter[0]}, ${response.data.result.lastSearchAfter[1]}`;

          this.noMoreData = false; // 더 불러올 데이터가 있으므로 메시지 숨김

          if (this.boardList.length === 0) {
            this.isBoardExist = false;
            this.isPageExist = false;
          } else {
            this.isBoardExist = true;
            this.isPageExist = true;
          }
        }
      } catch (error) {
        console.error(error);
        this.isBoardExist = false;
        this.isPageExist = false;
      } finally {
        this.isLoading = false;
      }
    },

    async findListByCategory(boardCategoryIdx, sortType, page = 1) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
          "/main/board/category/" +
          boardCategoryIdx +
          "/" +
          sortType +
          "?page=" +
          (page - 1)
        );

        this.boardList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalCnt;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    async getSearchBoardList(title, sortType, categoryIdx) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
          `/search/board/list?categoryIdx=${categoryIdx}&sortType=${sortType}&title=${encodeURIComponent(
            title
          )}`,
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );

        this.boardList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.totalCnt = response.data.result.totalCnt;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (e) {
        console.log(e);
      } finally {
        this.isLoading = false;
      }
    },
    async findBoard(boardIdx) {
      try {
        let response = await axios.get(backend + "/main/board/" + boardIdx);
        this.boardDetail = response.data.result;

        return this.boardDetail;
      } catch (e) {
        console.log(e);
      }
    },
    async createBoardUp(accessToken, requestBody) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };

        let response = await axios.post(
          backend + "/main/boardup/create",
          requestBody,
          {
            headers
          }
        );
        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }

        return response;
      } catch (e) {
        console.error("게시글 추천 실패", e);
        throw e;
      }
    },
    async createBoardScrap(accessToken, requestBody) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };


        let response = await axios.post(
          backend + "/main/boardscrap/create",
          requestBody,
          {
            headers
          }
        );
        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }

        return response;
      } catch (e) {
        console.error("게시글 스크랩 실패", e);
        throw e;
      }
    },
    async checkBoardUp(accessToken, boardIdx) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };

        let response = await axios.get(`${backend}/main/boardup/check/${boardIdx}`, {
          headers
        });
        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }

        this.isRecommended = response.data.result.status;

        return response;
      } catch (e) {
        console.error(e);
        throw e;
      }
    },
    async checkBoardScrap(accessToken, boardIdx) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };
        let response = await axios.get(
          `${backend}/main/boardscrap/check/${boardIdx}`,
          {
            headers
          }
        );
        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }



        this.isScrapped = response.data.result.status;

        return response;
      } catch (e) {
        console.error(e);
        throw e;
      }
    },
    async cancelBoardUp(accessToken, boardUpIdx) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };

        await axios.patch(
          `${backend}/main/boardup/delete/${boardUpIdx}`,
          {},
          {
            headers
          }
        );

      } catch (e) {
        console.error(e);
        throw e;
      }
    },
    async cancelBoardScrap(accessToken, boardScrapIdx) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };



        await axios.patch(
          `${backend}/main/boardscrap/delete/${boardScrapIdx}`,
          {},
          {
            headers
          }
        );
      } catch (e) {
        console.error(e);
        throw e;
      }
    },

    async createBoardCategory(categoryName) {
      try {
        let response = await axios.post(
          backend + "/main/admin/board/create",
          { categoryName: categoryName },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        if (response.data.isSuccess === true) {
          alert(`${categoryName} 생성 완료!`);
          this.$router.push({ path: `/admin/board/category` });
        }
      } catch (e) {
        if (e.response && e.response.data) {
          console.log(e.response.data);
          if (e.response.data.code === "BOARD-CATEGORY-002") {
            alert("동일한 이름의 카테고리가 이미 존재합니다.");
          }
        }
      }
    },

    async findBoardDetailByUserIdx(boardIdx) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };

        this.isLoading = true;

        let response = await axios.get(`${backend}/main/board/mywrite/${boardIdx}`, {
          headers
        });

        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }

        this.boardDetail = response.data.result;

        return this.boardDetail;
      } catch (e) {
        console.log(e);
      } finally {
        this.isLoading = false;
      }
    },
    async updateBoard(board) {

      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };


        let response = await axios.patch(`${backend}/main/board/update`, board, {
          headers
        });
        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }
        if (response.data.isSuccess === true) {
          alert("게시글이 수정되었습니다.");
          window.location.href = "/board/" + board.boardIdx;
        }
      } catch (e) {
        console.log(e);
      }
    },
    async getStudyDetail() {
      try {
        let response = await axios.get(backend + "/main/board/2");
        this.boardDetail = response.data.result;

        return this.boardDetail;
      } catch (e) {
        console.log(e);
      }
    },
    // 태그별 글 불러오기
    async getTagBoardList(boardCategoryIdx, sortType, page = 1) {
      let selectTagIdx = this.tagIdx;
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
          "/main/board/tag/" +
          selectTagIdx +
          "/" +
          boardCategoryIdx +
          "/" +
          sortType +
          "?page=" +
          (page - 1)
        );
        this.boardList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalCnt;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    async getSearchTagBoardList(
      boardCategoryIdx,
      searchTerm,
      sortType,
      page = 1
    ) {
      let selectTagIdx = this.tagIdx;
      try {
        this.isLoading = true;

        const params = new URLSearchParams({
          page: page - 1,
        }).toString();

        let response = await axios.get(
          backend +
          `/main/board/tag/${selectTagIdx}/${boardCategoryIdx}/${sortType}/search?searchTerm=${encodeURIComponent(
            searchTerm
          )}&${params}`,
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );

        this.boardList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalCnt;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (e) {
        console.log(e);
      } finally {
        this.isLoading = false;
      }
    },

    async deleteBoardCategory(boardCategoryIdx) {
      try {
        await axios.delete(backend + "/main/admin/board/delete/" + boardCategoryIdx);
      } catch (e) {
        console.error(e);
        throw e;
      }
    },

    async updateBoardCategory(boardCategoryIdx, newCategoryName) {
      try {
        let response = await axios.patch(
          backend + "/main/admin/board/update/" + boardCategoryIdx,
          { categoryName: newCategoryName },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        if (response.data.isSuccess === true) {
          alert(`카테고리 이름 수정 완료!`);
          this.$router.push({ path: `/admin/board/update` });
        }
      } catch (e) {
        if (e.response && e.response.data) {
          console.log(e.response.data);
          if (e.response.data.code === "BOARD-CATEGORY-002") {
            alert("동일한 이름의 카테고리가 이미 존재합니다.");
          }
        }
      }
    },
    async findMyBoardListByCategory(boardCategoryIdx, option, page = 1) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };

        let response = await axios.get(
          backend +
          "/main/board/mylist/" +
          boardCategoryIdx +
          "/" +
          option +
          "?page=" +
          (page - 1),
          {
            headers
          }
        );
        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }
        this.boardList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalCnt;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (error) {
        console.error(error);
      }
    },
    async findBoardScrapListByCategory(boardCategoryIdx, option, page = 1) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };

        let response = await axios.get(
          backend +
          "/main/boardscrap/list/" +
          boardCategoryIdx +
          "/" +
          option +
          "?page=" +
          (page - 1),
          {
            headers
          }
        );
        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }
        this.boardList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalCnt;

        if (this.boardList.length === 0) {
          this.isBoardExist = false;
          this.isPageExist = false;
        } else {
          this.isBoardExist = true;
          this.isPageExist = true;
        }
      } catch (error) {
        console.error(error);
      }
    },

    async deleteBoard(boardIdx) {
      try {

        this.validateToken();

        const headers = this.isTokenExpired
          ? {
            Authorization: `Bearer ${accessToken}`,
            RefreshToken: `Bearer ${refreshToken}`,
            "Content-Type": "application/json",
          }
          : {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          };

        let response = await axios.delete(
          backend + "/main/board/delete/" + boardIdx,
          {
            headers
          }
        );

        if (response.headers["new-refresh-token"] != null) {
          if (
            response.headers["new-refresh-token"] !=
            localStorage.getItem("refreshToken")
          ) {
            localStorage.setItem("refreshToken", "");
            localStorage.setItem(
              "refreshToken",
              response.headers["new-refresh-token"]
            );
          }
        }

        if (response.headers["new-access-token"] != null) {
          if (
            response.headers["new-access-token"] !=
            localStorage.getItem("accessToken")
          ) {
            localStorage.setItem("accessToken", "");
            localStorage.setItem(
              "accessToken",
              response.headers["new-access-token"]
            );
          }
        }

        return response.data;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
