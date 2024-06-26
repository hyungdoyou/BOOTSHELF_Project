package com.example.bootshelf.reviewsvc.review.model.entity;

import com.example.bootshelf.reviewsvc.review.model.request.PatchUpdateReviewReq;
import com.example.bootshelf.reviewsvc.reviewcategory.model.ReviewCategory;
import com.example.bootshelf.reviewsvc.reviewcomment.model.entity.ReviewComment;
import com.example.bootshelf.reviewsvc.reviewscrap.model.entity.ReviewScrap;
import com.example.bootshelf.reviewsvc.reviewup.model.entity.ReviewUp;
import com.example.bootshelf.user.model.entity.User;
import lombok.*;
import org.joda.time.LocalDateTime;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_idx")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReviewCategory_idx")
    private ReviewCategory reviewCategory;

    @OneToMany(mappedBy = "review")
    private List<ReviewUp> reviewUpList = new ArrayList<>();

    @OneToMany(mappedBy = "review")
    private List<ReviewScrap> reviewScrapList = new ArrayList<>();

    @OneToMany(mappedBy = "review")
    private List<ReviewComment> reviewCommentList = new ArrayList<>();

    @Column(nullable = false, length = 500)
    private String reviewTitle;

    @Column(nullable = false, length = 5000)
    private String reviewContent;

    @Column(nullable = false, length = 200)
    private String courseName;

    @Column(nullable = false)
    private Integer courseEvaluation;

    @Column(nullable = false)
    private Integer viewCnt;

    @Column(nullable = false)
    private Integer upCnt;

    @Column(nullable = false)
    private Integer scrapCnt;

    @Column(nullable = false)
    private Integer commentCnt;

    @Column(nullable = false)
    private Boolean status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void increaseScrapCnt() {
        this.scrapCnt += 1;
    }

    public void decreaseScrapCnt() {
        this.scrapCnt -= 1;
    }

    public void increaseUpCnt() {
        this.upCnt += 1;
    }

    public void decreaseUpCnt() {
        this.upCnt -= 1;
    }

    public void increaseViewCount() {
        this.viewCnt += 1;
    }

    public void increaseCommentUpCnt() {
        this.commentCnt += 1;
    }

    public void decreaseCommentUpCnt() {
        this.commentCnt -= 1;
    }

    public void update(PatchUpdateReviewReq patchUpdateReviewReq) {
        if (patchUpdateReviewReq.getReviewTitle() != null) {
            this.reviewTitle = patchUpdateReviewReq.getReviewTitle();
        }
        if (patchUpdateReviewReq.getReviewContent() != null) {
            this.reviewContent = patchUpdateReviewReq.getReviewContent();
        }
        if (patchUpdateReviewReq.getCourseEvaluation() != null) {
            this.courseEvaluation = patchUpdateReviewReq.getCourseEvaluation();
        }
    }
}
