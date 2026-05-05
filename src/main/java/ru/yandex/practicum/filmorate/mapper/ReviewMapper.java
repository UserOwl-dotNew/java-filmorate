package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewMapper {

    public static ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }

    public static Review mapToReview(NewReviewRequest request) {
        return Review.builder()
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .userId(request.getUserId())
                .filmId(request.getFilmId())
                .useful(0)
                .build();
    }

    public static Review updateReviewFields(Review review, UpdateReviewRequest request) {
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        if (request.getIsPositive() != null) {
            review.setIsPositive(request.getIsPositive());
        }
        return review;
    }
}
