package com.example.bootshelf.reviewsvc.review.model.response;

import lombok.*;
import org.joda.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetSearchListReviewRes {

    private Integer reviewIdx;
    private Integer userIdx;
    private String userNickName;
    private String profileImage;
    private Integer reviewCategoryIdx;
    private String reviewCategoryName;
    private String reviewTitle;
    private String reviewContent;
    private String reviewImage;
    private String courseName;
    private Integer courseEvaluation;
    private Integer viewCnt;
    private Integer upCnt;
    private Integer scrapCnt;
    private Integer commentCnt;
    private LocalDateTime updatedAt;
}
