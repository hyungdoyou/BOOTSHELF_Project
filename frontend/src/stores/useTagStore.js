import { defineStore } from "pinia";
import axios from "axios";

const backend = "http://192.168.0.61/api";
// const backend = "http://localhost:9999";

export const useTagStore = defineStore("tag", {
  state: () => ({
    tagList: [],
    currentPage: 0,
    totalPages: 0,
    totalCnt: 0,
    isTokenExpired: false,
  }),
  actions: {
    async getTagList(page = 1) {
      try {
        let response = await axios.get(
          backend + "/main/admin/tag/list?page=" + (page - 1)
        );

        this.tagList = response.data.result.list;
        this.totalPages = response.data.result.totalPages;
        this.currentPage = page;
        this.totalCnt = response.data.result.totalCnt;
      } catch (e) {
        console.log(e);
      }
    },

    async createTag(tagName) {
      try {
        await axios.post(
          backend + "/main/admin/tag/create",
          { tagName: tagName },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
      } catch (e) {
        if (e.response && e.response.data) {
          if (e.response.data.code === "TAG-002") {
            alert("이미 존재하는 태그명입니다.");
          }
        }
      }
    },

    async updateTag(tagIdx, newCategoryName) {
      try {
        await axios.patch(backend + "/main/admin/tag/update/", {
          tagIdx: tagIdx,
          tagName: newCategoryName,
        });
      } catch (e) {
        if (e.response && e.response.data) {
          if (e.response.data.code === "TAG-001") {
            alert("해당 태그가 존재하지 않습니다.");
          } else if (e.response.data.code === "TAG-002") {
            alert("이미 존재하는 태그명입니다.");
          }
        }
      }
    },

    async deleteTag(tagIdx) {
      try {
        let response = await axios.delete(
          backend + "/main/admin/tag/delete/" + tagIdx
        );
        return response;
      } catch (e) {
        if (e.response && e.response.data) {
          if (e.response.data.code === "TAG-001") {
            alert("해당 태그가 존재하지 않습니다.");
          }
        }
      }
    },
  },
});
