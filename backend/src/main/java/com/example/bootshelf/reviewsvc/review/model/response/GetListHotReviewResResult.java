package com.example.bootshelf.reviewsvc.review.model.response;

import com.example.bootshelf.boardsvc.board.model.response.GetListHotBoardRes;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetListHotReviewResResult {
    private Long totalCnt;
    private Integer totalPages;
    private List<GetListHotReviewRes> list;
}
