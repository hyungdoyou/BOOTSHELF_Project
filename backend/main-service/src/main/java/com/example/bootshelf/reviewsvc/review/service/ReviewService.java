package com.example.bootshelf.reviewsvc.review.service;

import com.example.bootshelf.common.BaseRes;
import com.example.bootshelf.common.error.ErrorCode;
import com.example.bootshelf.common.error.entityexception.ReviewException;
import com.example.bootshelf.reviewsvc.review.model.entity.Review;
import com.example.bootshelf.reviewsvc.review.model.request.PatchUpdateReviewReq;
import com.example.bootshelf.reviewsvc.review.model.request.PostCreateReviewReq;
import com.example.bootshelf.reviewsvc.review.model.response.*;
import com.example.bootshelf.reviewsvc.review.repository.ReviewRepository;
import com.example.bootshelf.reviewsvc.reviewcategory.model.ReviewCategory;
import com.example.bootshelf.reviewsvc.reviewcomment.model.entity.ReviewComment;
import com.example.bootshelf.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = false)
    public BaseRes createReview(User user, PostCreateReviewReq postCreateReviewReq) {

        Optional<Review> result = reviewRepository.findByReviewTitle(postCreateReviewReq.getReviewTitle());

        // 후기글 제목 중복에 대한 예외 처리
        if (result.isPresent()) {
            throw new ReviewException(ErrorCode.DUPLICATE_REVIEW_TITLE, String.format("Review Title [ %s ] is duplicated.", postCreateReviewReq.getReviewTitle()));
        }

        Review review = Review.builder()
                .user(user)
                .reviewCategory(ReviewCategory.builder().idx(postCreateReviewReq.getReviewCategoryIdx()).build())
                .reviewTitle(postCreateReviewReq.getReviewTitle())
                .reviewContent(postCreateReviewReq.getReviewContent())
                .courseName(postCreateReviewReq.getCourseName())
                .courseEvaluation(postCreateReviewReq.getCourseEvaluation())
                .viewCnt(0)
                .upCnt(0)
                .scrapCnt(0)
                .commentCnt(0)
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        review = reviewRepository.save(review);


        PostCreateReviewRes postCreateReviewRes = PostCreateReviewRes.builder()
                .reviewIdx(review.getIdx())
                .reviewCategoryIdx(postCreateReviewReq.getReviewCategoryIdx())
                .reviewTitle(postCreateReviewReq.getReviewTitle())
                .courseName(postCreateReviewReq.getCourseName())
                .reviewContent(postCreateReviewReq.getReviewContent())
                .courseEvaluation(postCreateReviewReq.getCourseEvaluation())
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 등록 성공")
                .result(postCreateReviewRes)
                .build();

        return baseRes;
    }

    // 목록 조회 시 글만 추출
    public static String extractText(String html) {
        Document doc = Jsoup.parse(html);
        return doc.text();
    }

    // 인증회원 본인이 작성한 후기글 목록 조회
    @Transactional(readOnly = true)
    public BaseRes myList(User user, Pageable pageable, Integer reviewCategoryIdx, Integer sortType) {

        Page<Review> reviewList = reviewRepository.findMyReviewList(user.getIdx(), pageable, reviewCategoryIdx, sortType);

        List<GetMyListReviewRes> getMyListReviewResList = new ArrayList<>();

        for (Review review : reviewList) {

            String textContent = extractText(review.getReviewContent());

            GetMyListReviewRes getMyListReviewRes = GetMyListReviewRes.builder()
                    .idx(review.getIdx())
                    .reviewCategoryIdx(review.getReviewCategory().getIdx())
                    .title(review.getReviewTitle())
                    .content(textContent)
                    .courseName(review.getCourseName())
                    .courseEvaluation(review.getCourseEvaluation())
                    .viewCnt(review.getViewCnt())
                    .upCnt(review.getUpCnt())
                    .scrapCnt(review.getScrapCnt())
                    .commentCnt(review.getCommentCnt())
                    .type("review")
                    .boardType("write")
                    .updatedAt(review.getUpdatedAt())
                    .build();

            getMyListReviewResList.add(getMyListReviewRes);
        }

        Long totalCnt = reviewList.getTotalElements();
        Integer totalPages = reviewList.getTotalPages();

        GetMyListReviewResResult result = GetMyListReviewResResult.builder()
                .totalCnt(totalCnt)
                .totalPages(totalPages)
                .list(getMyListReviewResList)
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("인증회원 본인 후기글 목록 조회 요청 성공")
                .result(result)
                .build();

        return baseRes;
    }

    // 정렬 조건 별 후기글 목록 조회
    @Transactional(readOnly = true)
    public BaseRes listReview(Integer reviewCategoryIdx, Integer sortType, Pageable pageable) {

        Page<Review> reviewList = reviewRepository.findReviewList(reviewCategoryIdx, sortType, pageable);

        List<GetListReviewRes> getListReviewResList = new ArrayList<>();

        for (Review review : reviewList) {

            String textContent = extractText(review.getReviewContent());

            GetListReviewRes getListReviewRes = GetListReviewRes.builder()
                    .reviewIdx(review.getIdx())
                    .userIdx(review.getUser().getIdx())
                    .userNickName(review.getUser().getNickName())
                    .profileImage(review.getUser().getProfileImage())
                    .reviewTitle(review.getReviewTitle())
                    .reviewContent(textContent)
                    .courseName(review.getCourseName())
                    .courseEvaluation(review.getCourseEvaluation())
                    .viewCnt(review.getViewCnt())
                    .upCnt(review.getUpCnt())
                    .scrapCnt(review.getScrapCnt())
                    .commentCnt(review.getCommentCnt())
                    .updatedAt(review.getUpdatedAt())
                    .build();

            Document doc = Jsoup.parse(review.getReviewContent());
            Elements images = doc.select("img");

            if (!images.isEmpty()) {
                getListReviewRes.setReviewImage(images.get(0).attr("src"));
            }

            getListReviewResList.add(getListReviewRes);
        }

        Long totalCnt = reviewList.getTotalElements();
        Integer totalPages = reviewList.getTotalPages();

        GetListReviewResResult result = GetListReviewResResult.builder()
                .totalCnt(totalCnt)
                .totalPages(totalPages)
                .list(getListReviewResList)
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 목록 조회 요청 성공")
                .result(result)
                .build();

        return baseRes;
    }

    // 후기글 상세 조회
    @Transactional(readOnly = false)
    public BaseRes readReview(Integer reviewIdx) {

        Optional<Review> result = reviewRepository.findByIdx(reviewIdx);

        if (!result.isPresent()) {
            throw new ReviewException(ErrorCode.REVIEW_NOT_EXISTS, String.format("Review Idx [ %s ] is not exists.", reviewIdx));
        }

        Review review = result.get();
        review.increaseViewCount();
        reviewRepository.save(review);

        // 댓글 조회
        List<GetListCommentReviewRes> getListCommentReviewResList = new ArrayList<>();

        for (ReviewComment reviewComment : review.getReviewCommentList()) {

            // 댓글이 최상위 댓글일 때만 처리
            if (reviewComment.getParent() == null) {
                getListCommentReviewResList.add(convertToCommentReviewRes(reviewComment));
            }
        }

        GetReadReviewRes getReadReviewRes = GetReadReviewRes.builder()
                .reviewIdx(review.getIdx())
                .reviewCategoryName(review.getReviewCategory().getCategoryName())
                .userIdx(review.getUser().getIdx())
                .userNickName(review.getUser().getNickName())
                .profileImage(review.getUser().getProfileImage())
                .reviewTitle(review.getReviewTitle())
                .reviewContent(review.getReviewContent())
                .courseName(review.getCourseName())
                .courseEvaluation(review.getCourseEvaluation())
                .upCnt(review.getUpCnt())
                .scrapCnt(review.getScrapCnt())
                .commentCnt(review.getCommentCnt())
                .updatedAt(review.getUpdatedAt())
                .reviewCommentList(getListCommentReviewResList)
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 상세 조회 요청 성공")
                .result(getReadReviewRes)
                .build();

        return baseRes;
    }

    @Transactional(readOnly = true)
    public GetListCommentReviewRes convertToCommentReviewRes(ReviewComment reviewComment) {

        List<GetListCommentReviewRes> childCommentsRes = new ArrayList<>();

        for (ReviewComment childComment : reviewComment.getChildren()) {
            GetListCommentReviewRes childRes = convertToCommentReviewRes(childComment);
            childCommentsRes.add(childRes);
        }

        return GetListCommentReviewRes.builder()
                .commentIdx(reviewComment.getIdx())
                .userIdx(reviewComment.getUser().getIdx())
                .userNickName(reviewComment.getUser().getNickName())
                .profileImage(reviewComment.getUser().getProfileImage())
                .reviewCommentContent(reviewComment.getReviewCommentContent())
                .upCnt(reviewComment.getUpCnt())
                .updatedAt(reviewComment.getUpdatedAt())
                .children(childCommentsRes)
                .build();
    }

    @Transactional(readOnly = true)
    public BaseRes searchReviewListByQuery(String query, Integer searchType, Pageable pageable) {
        Page<Review> reviewList = reviewRepository.searchReviewListByQuery(pageable, query, searchType);

        List<GetReviewListByQueryRes> getReviewListByQueryResList = new ArrayList<>();

        for (Review review : reviewList) {

            String textContent = extractText(review.getReviewContent());

            GetReviewListByQueryRes getReviewListByQueryRes = GetReviewListByQueryRes.builder()
                    .reviewIdx(review.getIdx())
                    .reviewTitle(review.getReviewTitle())
                    .reviewContent(textContent)
                    .reviewCategoryName(review.getReviewCategory().getCategoryName())
                    .nickName(review.getUser().getNickName())
                    .createdAt(review.getCreatedAt())
                    .viewCnt(review.getViewCnt())
                    .commentCnt(review.getCommentCnt())
                    .upCnt(review.getUpCnt())
                    .build();

            getReviewListByQueryResList.add(getReviewListByQueryRes);
        }

        Long totalCnt = reviewList.getTotalElements();
        Integer totalPages = reviewList.getTotalPages();

        GetReviewListByQueryResResult result = GetReviewListByQueryResResult.builder()
                .totalCnt(totalCnt)
                .totalPages(totalPages)
                .list(getReviewListByQueryResList)
                .build();

        return BaseRes.builder()
                .isSuccess(true)
                .message("메인 페이지 검색 결과 조회 성공 <후기>")
                .result(result)
                .build();
    }

    // 후기글 수정
    @Transactional(readOnly = false)
    public BaseRes updateReview(User user, PatchUpdateReviewReq patchUpdateReviewReq) {

        Optional<Review> result = reviewRepository.findByIdxAndUserIdx(patchUpdateReviewReq.getReviewIdx(), user.getIdx());

        // 수정하고자 하는 후기글을 못찾을 때 예외 처리
        if (!result.isPresent()) {
            throw new ReviewException(ErrorCode.REVIEW_NOT_EXISTS, String.format("Review Idx [ %s ] is not exists.", patchUpdateReviewReq.getReviewIdx()));
        }

        Review review = result.get();
        if(!review.getReviewTitle().equals(patchUpdateReviewReq.getReviewTitle())) {
            // 수정 후기글 제목 중복에 대한 예외 처리
            Optional<Review> resultTitle = reviewRepository.findByReviewTitle(patchUpdateReviewReq.getReviewTitle());

            if (resultTitle.isPresent()) {
                throw new ReviewException(ErrorCode.DUPLICATE_REVIEW_TITLE, String.format("Review Title [ %s ] is duplicated.", patchUpdateReviewReq.getReviewTitle()));
            }
        }

        review.update(patchUpdateReviewReq);

        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 수정 성공")
                .result("요청 성공")
                .build();

        return baseRes;
    }

    // 후기글 삭제
    @Transactional(readOnly = false)
    public BaseRes deleteReview(User user, Integer reviewIdx) {

        Optional<Review> result = reviewRepository.findByIdxAndUserIdx(reviewIdx, user.getIdx());

        // 삭제하고자 하는 후기글을 못찾을 때 예외 처리
        if (!result.isPresent()) {
            throw new ReviewException(ErrorCode.REVIEW_NOT_EXISTS, String.format("Review Idx [ %s ] is not exists.", reviewIdx));
        }

        Review review = result.get();
        review.setStatus(false);
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 삭제 성공")
                .result("요청 성공")
                .build();

        return baseRes;
    }

    // 검색어 별 후기글 목록 조회
    @Transactional(readOnly = true)
    public BaseRes searchReview(Integer reviewCategoryIdx, Integer sortType, String searchTerm, Pageable pageable) {

        Page<Review> reviewList = reviewRepository.findReviewsBySearchTerm(reviewCategoryIdx, sortType, searchTerm, pageable);

        List<GetSearchListReviewRes> getSearchListReviewResList = new ArrayList<>();

        for (Review review : reviewList) {

            String textContent = extractText(review.getReviewContent());

            GetSearchListReviewRes getSearchListReviewRes = GetSearchListReviewRes.builder()
                    .reviewIdx(review.getIdx())
                    .userIdx(review.getUser().getIdx())
                    .userNickName(review.getUser().getNickName())
                    .profileImage(review.getUser().getProfileImage())
                    .reviewCategoryIdx(review.getReviewCategory().getIdx())
                    .reviewCategoryName(review.getReviewCategory().getCategoryName())
                    .reviewTitle(review.getReviewTitle())
                    .reviewContent(textContent)
                    .courseName(review.getCourseName())
                    .courseEvaluation(review.getCourseEvaluation())
                    .viewCnt(review.getViewCnt())
                    .upCnt(review.getUpCnt())
                    .scrapCnt(review.getScrapCnt())
                    .commentCnt(review.getCommentCnt())
                    .updatedAt(review.getUpdatedAt())
                    .build();

            Document doc = Jsoup.parse(review.getReviewContent());
            Elements images = doc.select("img");

            if (!images.isEmpty()) {
                getSearchListReviewRes.setReviewImage(images.get(0).attr("src"));
            }

            getSearchListReviewResList.add(getSearchListReviewRes);
        }

        Long totalCnt = reviewList.getTotalElements();
        Integer totalPages = reviewList.getTotalPages();

        GetSearchListReviewResResult result = GetSearchListReviewResResult.builder()
                .totalCnt(totalCnt)
                .totalPages(totalPages)
                .list(getSearchListReviewResList)
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 제목으로 검색결과 조회 요청 성공")
                .result(result)
                .build();

        return baseRes;
    }

    // Hot 인기 게시글 조회
    @Transactional(readOnly = true)
    public BaseRes listHotReview(Integer reviewCategoryIdx, Integer sortType, Pageable pageable) {

        Page<Review> reviewList = reviewRepository.findReviewList(reviewCategoryIdx, sortType, pageable);

        List<GetListHotReviewRes> getListHotReviewResList = new ArrayList<>();

        for (Review review : reviewList) {

            String textContent = extractText(review.getReviewContent());

            GetListHotReviewRes getListHotReviewRes = GetListHotReviewRes.builder()
                    .idx(review.getIdx())
                    .userIdx(review.getUser().getIdx())
                    .nickName(review.getUser().getNickName())
                    .profileImage(review.getUser().getProfileImage())
                    .title(review.getReviewTitle())
                    .content(textContent)
                    .courseName(review.getCourseName())
                    .courseEvaluation(review.getCourseEvaluation())
                    .viewCnt(review.getViewCnt())
                    .upCnt(review.getUpCnt())
                    .scrapCnt(review.getScrapCnt())
                    .commentCnt(review.getCommentCnt())
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .type("review")
                    .build();


            Document doc = Jsoup.parse(review.getReviewContent());
            Elements images = doc.select("img");

            if (!images.isEmpty()) {
                getListHotReviewRes.setImage(images.get(0).attr("src"));
            }

            getListHotReviewResList.add(getListHotReviewRes);
        }

        Long totalCnt = reviewList.getTotalElements();
        Integer totalPages = reviewList.getTotalPages();

        GetListHotReviewResResult result = GetListHotReviewResResult.builder()
                .totalCnt(totalCnt)
                .totalPages(totalPages)
                .list(getListHotReviewResList)
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("인기 후기글 카테고리별 목록 조회 요청 성공")
                .result(result)
                .build();

        return baseRes;
    }

    @Transactional(readOnly = true)
    public BaseRes searchHotReview(Integer reviewCategoryIdx, Integer sortType, String searchTerm, Pageable pageable) {

        Page<Review> reviewList = reviewRepository.findReviewsBySearchTerm(reviewCategoryIdx, sortType, searchTerm, pageable);

        List<GetListHotReviewRes> getListHotReviewResList = new ArrayList<>();

        for (Review review : reviewList) {

            String textContent = extractText(review.getReviewContent());

            GetListHotReviewRes getListHotReviewRes = GetListHotReviewRes.builder()
                    .idx(review.getIdx())
                    .userIdx(review.getUser().getIdx())
                    .nickName(review.getUser().getNickName())
                    .profileImage(review.getUser().getProfileImage())
                    .title(review.getReviewTitle())
                    .content(textContent)
                    .courseName(review.getCourseName())
                    .courseEvaluation(review.getCourseEvaluation())
                    .viewCnt(review.getViewCnt())
                    .upCnt(review.getUpCnt())
                    .scrapCnt(review.getScrapCnt())
                    .commentCnt(review.getCommentCnt())
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .type("review")
                    .build();

            Document doc = Jsoup.parse(review.getReviewContent());
            Elements images = doc.select("img");

            if (!images.isEmpty()) {
                getListHotReviewRes.setImage(images.get(0).attr("src"));
            }

            getListHotReviewResList.add(getListHotReviewRes);
        }

        Long totalCnt = reviewList.getTotalElements();
        Integer totalPages = reviewList.getTotalPages();

        GetListHotReviewResResult result = GetListHotReviewResResult.builder()
                .totalCnt(totalCnt)
                .totalPages(totalPages)
                .list(getListHotReviewResList)
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("인기 후기글 검색어별 목록 조회 요청 성공")
                .result(result)
                .build();

        return baseRes;
    }

    @Transactional(readOnly = false)
    public BaseRes findReviewDetailByUserIdx(Integer reviewIdx, User user) {
        Optional<Review> result = reviewRepository.findByIdxAndUserIdx(reviewIdx, user.getIdx());

        if (!result.isPresent()) {
            throw new ReviewException(ErrorCode.REVIEW_NOT_EXISTS, String.format("Review Idx [ %s ] is not exists.", reviewIdx));
        }

        Review review = result.get();

        GetReadReviewRes getReadReviewRes = GetReadReviewRes.builder()
                .reviewIdx(review.getIdx())
                .reviewCategoryName(review.getReviewCategory().getCategoryName())
                .reviewCategoryIdx(review.getReviewCategory().getIdx())
                .reviewTitle(review.getReviewTitle())
                .reviewContent(review.getReviewContent())
                .courseName(review.getCourseName())
                .courseEvaluation(review.getCourseEvaluation())
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("본인 작성 후기글 상세 조회 성공")
                .result(getReadReviewRes)
                .build();

        return baseRes;
    }
}
