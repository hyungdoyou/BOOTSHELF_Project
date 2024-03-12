package com.example.bootshelf.review.repository.querydsl;

import com.example.bootshelf.review.model.entity.QReview;
import com.example.bootshelf.review.model.entity.Review;
import com.example.bootshelf.reviewcategory.model.QReviewCategory;
import com.example.bootshelf.reviewimage.model.QReviewImage;
import com.example.bootshelf.user.model.entity.QUser;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewRepositoryCustomImpl extends QuerydslRepositorySupport implements ReviewRepositoryCustom {
    public ReviewRepositoryCustomImpl() {
        super(Review.class);
    }

    // 인증회원 본인 후기글 목록 조회
    @Override
    public Page<Review> findMyReviewList(Integer userIdx, Pageable pageable) {
        QReview review = new QReview("review");
        QReviewCategory reviewCategory = new QReviewCategory("reviewCategory");
        QReviewImage reviewImage = new QReviewImage("reviewImage");


        List<Review> result = from(review)
                .leftJoin(review.reviewImageList, reviewImage).fetchJoin()
                .leftJoin(review.reviewCategory, reviewCategory).fetchJoin()
                .where(review.user.idx.eq(userIdx))
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch().stream().distinct().collect(Collectors.toList());

        return new PageImpl<>(result, pageable, result.size());
    }

    // 정렬 조건 별 후기글 목록 조회
    @Override
    public Page<Review> findReviewList(Integer reviewCategoryIdx, Integer sortType, Pageable pageable) {

        QReview review = new QReview("review");
        QReviewImage reviewImage = new QReviewImage("reviewImage");
        QUser user = new QUser("user");

        OrderSpecifier[] orderSpecifiers = createOrderSpecifier(sortType, review);

        List<Review> result = from(review)
                .leftJoin(review.reviewImageList, reviewImage).fetchJoin()
                .leftJoin(review.user, user).fetchJoin()
                .where(review.reviewCategory.idx.eq(reviewCategoryIdx))
                .orderBy(orderSpecifiers)
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch().stream().distinct().collect(Collectors.toList());

        return new PageImpl<>(result, pageable, result.size());
    }

    private OrderSpecifier[] createOrderSpecifier(Integer sortType, QReview review) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        // sortType에 따른 동적 정렬 조건 설정
        switch (sortType) {
            case 1:
                orderSpecifiers.add(review.updatedAt.desc());
                break;
            case 2:
                orderSpecifiers.add(review.upCnt.desc());
                break;
            case 3:
                orderSpecifiers.add(review.viewCnt.desc());
                break;
            case 4:
                orderSpecifiers.add(review.scrapCnt.desc());
                break;
            default:
                orderSpecifiers.add(review.updatedAt.desc());
        }
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}
