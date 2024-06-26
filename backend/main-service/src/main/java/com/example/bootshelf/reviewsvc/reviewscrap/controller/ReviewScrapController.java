package com.example.bootshelf.reviewsvc.reviewscrap.controller;

import com.example.bootshelf.common.BaseRes;
import com.example.bootshelf.reviewsvc.reviewscrap.model.request.PostCreateReviewScrapReq;
import com.example.bootshelf.reviewsvc.reviewscrap.service.ReviewScrapService;
import com.example.bootshelf.user.model.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Tag(name = "후기", description = "ReviewScrap CRUD")
@Api(tags = "후기 스크랩")
@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("/main/reviewscrap")
public class ReviewScrapController {

    private final ReviewScrapService reviewScrapService;

    @Operation(summary = "ReviewScrap 추가",
            description = "리뷰 게시글을 스크랩하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "해당 Review가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/create")
    public ResponseEntity<BaseRes> createReviewScrap(
            @AuthenticationPrincipal User user,
            @RequestBody PostCreateReviewScrapReq postCreateReviewScrapReq
    ) {
        return ResponseEntity.ok().body(reviewScrapService.createReviewScrap(user, postCreateReviewScrapReq));
    }


    @Operation(summary = "ReviewScrap 카테고리별 목록 조회",
            description = "카테고리별 스크랩한 리뷰 게시물 목록을 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/list/{reviewCategoryIdx}/{sortType}")
    public ResponseEntity<BaseRes> findReviewScrapListByCategory(
            @AuthenticationPrincipal User user,
            @PathVariable(value = "reviewCategoryIdx") Integer reviewCategoryIdx,
            @PathVariable @NotNull(message = "조건 유형은 필수 입력 항목입니다.") @Positive(message = "조건 유형은 1이상의 양수입니다.") @ApiParam(value = "정렬유형 : 1 (최신순), 2 (추천수 순), 3 (조회수 순), 4 (스크랩수 순), 5 (댓글수 순)") Integer sortType,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok().body(reviewScrapService.findReviewScrapListByCategory(user, reviewCategoryIdx, sortType, pageable));
    }

    @Operation(summary = "ReviewScrap 여부 조회",
            description = "게시글을 스크랩 여부를 확인하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/check/{reviewIdx}")
    public ResponseEntity<BaseRes> checkReviewScrap(
            @AuthenticationPrincipal User user,
            @PathVariable Integer reviewIdx
    ) {
        return ResponseEntity.ok().body(reviewScrapService.checkReviewScrap(user, reviewIdx));
    }


    @Operation(summary = "ReviewScrap 스크랩 삭제",
            description = "스크랩한 리뷰 게시물을 삭제하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/delete/{reviewScrapIdx}")
    public ResponseEntity<BaseRes> deleteReviewScrap(
            @AuthenticationPrincipal User user,
            @PathVariable Integer reviewScrapIdx
    ) {
        return ResponseEntity.ok().body(reviewScrapService.deleteReviewScrap(user, reviewScrapIdx));
    }

    // 현재 미사용 API
//    @Operation(summary = "ReviewScrap 목록 조회",
//            description = "스크랩한 리뷰 게시물 목록을 조회하는 API입니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "성공"),
//            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//    })
//    @GetMapping("/list")
//    public ResponseEntity<BaseRes> findReviewScrapList(
//            @AuthenticationPrincipal User user,
//            @PageableDefault(size = 5) Pageable pageable
//    ) {
//        return ResponseEntity.ok().body(reviewScrapService.findReviewScrapList(user, pageable));
//    }
}