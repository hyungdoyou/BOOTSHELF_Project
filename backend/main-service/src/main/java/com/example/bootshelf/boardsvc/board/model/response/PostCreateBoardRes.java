package com.example.bootshelf.boardsvc.board.model.response;

import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCreateBoardRes {
    private Integer boardIdx;

    private String boardtitle;

    private String boardcontent;

    private Integer boardCategoryIdx;

    private List<String> boardTagList;

}
