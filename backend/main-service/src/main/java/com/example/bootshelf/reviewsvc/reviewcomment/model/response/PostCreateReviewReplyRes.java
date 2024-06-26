package com.example.bootshelf.reviewsvc.reviewcomment.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCreateReviewReplyRes {
    private Integer parentIdx;
    private String reviewCommentContent;
}
