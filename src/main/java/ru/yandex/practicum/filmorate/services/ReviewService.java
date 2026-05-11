package ru.yandex.practicum.filmorate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.db.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.db.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewDbStorage reviewStorage;
    private final ReviewMapper reviewMapper;
    private final EventDbStorage eventStorage;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewDbStorage reviewStorage, ReviewMapper reviewMapper, EventDbStorage eventStorage) {
        this.reviewStorage = reviewStorage;
        this.reviewMapper = reviewMapper;
        this.eventStorage = eventStorage;
    }

    public Collection<ReviewDto> findAllReviews(Long filmId, Integer count) {
        return reviewStorage.findAllReviews(filmId, count).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<ReviewDto> findById(long reviewId) {
        return reviewStorage.findById(reviewId)
                .map(reviewMapper::toDto);
    }

    public Optional<Review> removeReview(Long id) {
        Optional<Review> deletedReview = reviewStorage.removeReview(id);
        eventStorage.addEvent(deletedReview.get().getUserId(), EventType.REVIEW, Operation.REMOVE, deletedReview.get().getReviewId());
        return deletedReview;
    }

    public Review create(Review review) {
        Review createdReview = reviewStorage.create(review);
        eventStorage.addEvent(createdReview.getUserId(), EventType.REVIEW, Operation.ADD, createdReview.getReviewId());
        return createdReview;
    }

    public Review update(Review newReview) {
        Review updatedReview = reviewStorage.update(newReview);
        eventStorage.addEvent(updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE, updatedReview.getReviewId());
        return updatedReview;
    }

    public Optional<Review> addLike(Long reviewId, Long userId) {
        return reviewStorage.addLike(reviewId, userId);
    }

    public Optional<Review> addDislike(Long reviewId, Long userId) {
        return reviewStorage.addDislike(reviewId, userId);
    }

    public Optional<Review> removeLike(Long reviewId, Long userId) {
        return reviewStorage.removeLike(reviewId, userId);
    }

    public Optional<Review> removeDislike(Long reviewId, Long userId) {
        return reviewStorage.removeDislike(reviewId, userId);
    }
}
