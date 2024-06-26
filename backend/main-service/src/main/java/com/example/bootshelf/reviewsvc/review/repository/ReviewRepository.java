package com.example.bootshelf.reviewsvc.review.repository;

import com.example.bootshelf.reviewsvc.review.model.entity.Review;
import com.example.bootshelf.reviewsvc.review.repository.querydsl.ReviewRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer>, ReviewRepositoryCustom {
    Optional<Review> findByIdx(Integer idx);
    Optional<Review> findByReviewTitle(String ReviewTitle);

    Optional<Review> findByIdxAndUserIdx(Integer reviewIdx, Integer userIdx);

    public Integer deleteByIdxAndUserIdx(Integer reviewIdx, Integer userIdx);
}
