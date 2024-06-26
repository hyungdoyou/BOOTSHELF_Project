package com.example.bootshelf.boardsvc.boardscrap.model.response;

import lombok.Builder;
import lombok.Data;
import org.joda.time.LocalDateTime;

@Data
@Builder
public class GetFindBoardScrapRes {
    private Integer boardScrapIdx;
    private Integer boardIdx;
    private Integer boardCategoryIdx;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}