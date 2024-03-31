import { defineStore } from "pinia";
import axios from "axios";

const backend = "http://192.168.0.61/api";
// const backend = "http://localhost:8080";

const storedToken = localStorage.getItem("token");

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
  }),

  actions: {
    async createBoard(board) {
      // const formData = new FormData();

      // let json = JSON.stringify(board);
      // formData.append("board", new Blob([json], { type: "application/json" }));

      // if (boardImages) {
      //   this.boardImages.forEach((base64, index) => {
      //     const blob = this.base64ToBlob(base64);
      //     formData.append(`boardImages`, blob, `image${index}.jpg`);
      //   });
      // }

      try {
        let response = await axios.post(backend + `/board/create`, board, {
          headers: {
            Authorization: `Bearer ${storedToken}`,
            "Content-Type": "application/json",
          },
        });
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
          } else if(e.response.data.code === "COMMON-001") {
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
            "/board/search?query=" +
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

    async findListByCategory(boardCategoryIdx, sortType, page = 1) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
            "/board/category/" +
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
    async getSearchBoardList(searchTerm, sortType) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
            `/board/${sortType}/search?searchTerm=${encodeURIComponent(
              searchTerm
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
        let response = await axios.get(backend + "/board/" + boardIdx);
        this.boardDetail = response.data.result;

        return this.boardDetail;
      } catch (e) {
        console.log(e);
      }
    },
    async createBoardUp(token, requestBody) {
      try {
        let response = await axios.post(
          backend + "/boardup/create",
          requestBody,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          }
        );

        return response;
      } catch (e) {
        console.error("게시글 추천 실패", e);
        throw e;
      }
    },
    async createBoardScrap(token, requestBody) {
      try {
        let response = await axios.post(
          backend + "/boardscrap/create",
          requestBody,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          }
        );

        return response;
      } catch (e) {
        console.error("게시글 스크랩 실패", e);
        throw e;
      }
    },
    async checkBoardUp(token, boardIdx) {
      try {
        let response = await axios.get(`${backend}/boardup/check/${boardIdx}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        this.isRecommended = response.data.result.status;

        return response;
      } catch (e) {
        console.error(e);
        throw e;
      }
    },
    async checkBoardScrap(token, boardIdx) {
      try {
        let response = await axios.get(
          `${backend}/boardscrap/check/${boardIdx}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        this.isScrapped = response.data.result.status;

        return response;
      } catch (e) {
        console.error(e);
        throw e;
      }
    },
    async cancelBoardUp(token, boardUpIdx) {
      try {
        await axios.patch(
          `${backend}/boardup/delete/${boardUpIdx}`,
          {},
          {
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          }
        );
      } catch (e) {
        console.error(e);
        throw e;
      }
    },
    async cancelBoardScrap(token, boardScrapIdx) {
      try {
        await axios.patch(
          `${backend}/boardscrap/delete/${boardScrapIdx}`,
          {},
          {
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          }
        );
      } catch (e) {
        console.error(e);
        throw e;
      }
    },
    async getCategoryBoardListByQuery(
      boardCategoryIdx,
      query,
      option,
      page = 1
    ) {
      try {
        this.isLoading = true;

        let response = await axios.get(
          backend +
            "/board/search/by/" +
            boardCategoryIdx +
            "?query=" +
            query +
            "&sortType=" +
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

    async createBoardCategory(categoryName) {
      try {
        let response = await axios.post(
          backend + "/admin/board/create",
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
        this.isLoading = true;

        let response = await axios.get(`${backend}/board/mywrite/${boardIdx}`, {
          headers: {
            Authorization: `Bearer ${storedToken}`,
          },
        });
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
        let response = await axios.patch(`${backend}/board/update`, board, {
          headers: {
            Authorization: `Bearer ${storedToken}`,
            "Content-Type": "application/json",
          },
        });
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
        let response = await axios.get(backend + "/board/2");
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
            "/board/tag/" +
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
            `/board/tag/${selectTagIdx}/${boardCategoryIdx}/${sortType}/search?searchTerm=${encodeURIComponent(
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
        await axios.delete(backend + "/admin/board/delete/" + boardCategoryIdx);
      } catch (e) {
        console.error(e);
        throw e;
      }
    },

    async updateBoardCategory(boardCategoryIdx, newCategoryName) {
      try {
        let response = await axios.patch(
          backend + "/admin/board/update/" + boardCategoryIdx,
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
        let response = await axios.get(
          backend +
            "/board/mylist/" +
            boardCategoryIdx +
            "/" +
            option +
            "?page=" +
            (page - 1),
          {
            headers: {
              Authorization: `Bearer ${storedToken}`,
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
      } catch (error) {
        console.error(error);
      }
    },
    async findBoardScrapListByCategory(boardCategoryIdx, option, page = 1) {
      try {
        let response = await axios.get(
          backend +
            "/boardscrap/list/" +
            boardCategoryIdx +
            "/" +
            option +
            "?page=" +
            (page - 1),
          {
            headers: {
              Authorization: `Bearer ${storedToken}`,
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
      } catch (error) {
        console.error(error);
      }
    },

    async deleteBoard(boardIdx) {
      try {
        let response = await axios.delete(
          backend + "/board/delete/" + boardIdx,
          {
            headers: {
              Authorization: `Bearer ${storedToken}`,
              "Content-Type": "application/json",
            },
          }
        );

        return response.data;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
