package ru.yandex.practicum.filmorate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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

    public Optional<ReviewDto> findById(String id) {
        raiseExceptionIfBlank(id);

        Long reviewId = parseStringId(id);

        return reviewStorage.findById(reviewId)
                .map(reviewMapper::toDto);
    }

    public Optional<Review> removeReview(String id) {
        Long reviewId = parseStringId(id);

        Optional<Review> deletedReview = reviewStorage.removeReview(reviewId);

        deletedReview.ifPresent(review ->
                eventStorage.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId()));
        return deletedReview;
    }

    public Review create(Review review) {
        Review createdReview = reviewStorage.create(review);
        eventStorage.addEvent(createdReview.getUserId(), EventType.REVIEW, Operation.ADD, createdReview.getReviewId());
        return createdReview;
    }

    public Review update(Review newReview) {
        if (newReview.getReviewId() == null) {
            throw new ValidationException("Идентификатор не указан.");
        }

        Review updatedReview = reviewStorage.update(newReview);
        eventStorage.addEvent(updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE, updatedReview.getReviewId());
        return updatedReview;
    }

    public Optional<Review> addLike(String reviewId, String userId) {
        raiseExceptionIfBlank(reviewId);
        raiseExceptionIfBlank(userId);

        Optional<Review> result = reviewStorage.addLike(parseStringId(reviewId), parseStringId(userId));
        if (result.isPresent()) {
            eventStorage.addEvent(parseStringId(userId), EventType.LIKE, Operation.ADD, parseStringId(reviewId));
        }
        return result;
    }

    public Optional<Review> addDislike(String reviewId, String userId) {
        raiseExceptionIfBlank(reviewId);
        raiseExceptionIfBlank(userId);

        Optional<Review> result = reviewStorage.addDislike(parseStringId(reviewId), parseStringId(userId));
        if (result.isPresent()) {
            eventStorage.addEvent(parseStringId(userId), EventType.LIKE, Operation.ADD, parseStringId(reviewId));
        }
        return result;
    }

    public Optional<Review> removeLike(String reviewId, String userId) {
        raiseExceptionIfBlank(reviewId);
        raiseExceptionIfBlank(userId);

        Optional<Review> result = reviewStorage.removeLike(parseStringId(reviewId), parseStringId(userId));

        if (result.isPresent()) {
            eventStorage.addEvent(parseStringId(userId), EventType.LIKE, Operation.REMOVE, parseStringId(reviewId));
        }
        return result;
    }

    public Optional<Review> removeDislike(String reviewId, String userId) {
        raiseExceptionIfBlank(reviewId);
        raiseExceptionIfBlank(userId);

        Optional<Review> result = reviewStorage.removeDislike(parseStringId(reviewId), parseStringId(userId));
        if (result.isPresent()) {
            eventStorage.addEvent(parseStringId(userId), EventType.LIKE, Operation.REMOVE, parseStringId(reviewId));
        }
        return result;
    }

    private Long parseStringId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ValidationException("Идентификатор должен быть числом.");
        }
    }

    private void raiseExceptionIfBlank(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("Идентификатор не указан.");
        }
    }
}
