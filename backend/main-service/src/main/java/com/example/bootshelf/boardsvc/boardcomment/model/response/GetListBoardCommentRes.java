package com.example.bootshelf.boardsvc.boardcomment.model.response;

import lombok.*;
import org.joda.time.LocalDateTime;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetListBoardCommentRes {
    private Integer idx;
    private Integer boardIdx;
    private Integer userIdx;
    private String nickName;
    private String profileImage;
    private String boardCommnetContent;
    private Integer upCnt;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean status;
    private List<GetListBoardCommentRes> children; // 자식 댓글 목록

}
