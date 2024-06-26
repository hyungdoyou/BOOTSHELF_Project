package com.example.bootshelf.boardsvc.boardscrap.model.entity;

import com.example.bootshelf.boardsvc.board.model.entity.Board;
import com.example.bootshelf.boardsvc.boardscrap.model.request.PostCreateBoardScrapReq;
import com.example.bootshelf.user.model.entity.User;
import lombok.*;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_idx")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Board_idx")
    private Board board;

    @Column(nullable = false)
    private Boolean status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static BoardScrap toEntity(User user, PostCreateBoardScrapReq req) {
        return BoardScrap.builder()
                .user(User.builder().idx(user.getIdx()).build())
                .board(Board.builder().idx(req.getBoardIdx()).build())
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
