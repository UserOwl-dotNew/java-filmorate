package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.db.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDbStorage reviewDbStorage;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final EventDbStorage eventDbStorage;

    public ReviewDto create(NewReviewRequest request) throws InternalServerException {
        validateReviewRequest(request);
        Review review = ReviewMapper.mapToReview(request);
        review = reviewDbStorage.create(review);
        eventDbStorage.addEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto update(UpdateReviewRequest request) throws InternalServerException {
        Review review = reviewDbStorage.findById(request.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + request.getReviewId() + " не найден"));
        review = ReviewMapper.updateReviewFields(review, request);
        review = reviewDbStorage.update(review);
        eventDbStorage.addEvent(review.getUserId(), EventType.REVIEW, Operation.UPDATE, review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public void delete(Long reviewId) {
        Review review = reviewDbStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
        eventDbStorage.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId());
        reviewDbStorage.deleteById(reviewId);
    }

    public ReviewDto findById(Long reviewId) {
        Review review = reviewDbStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
        return ReviewMapper.mapToReviewDto(review);
    }

    public List<ReviewDto> findByFilmId(Long filmId, int count) {
        List<Review> reviews;
        if (filmId == null) {
            reviews = reviewDbStorage.findAll(count);
        } else {
            filmDbStorage.findById(filmId)
                    .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
            reviews = reviewDbStorage.findByFilmId(filmId, count);
        }
        return reviews.stream()
                .map(ReviewMapper::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public ReviewDto addLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewDbStorage.addLike(reviewId, userId, true);
        return findById(reviewId);
    }

    public ReviewDto addDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewDbStorage.addLike(reviewId, userId, false);
        return findById(reviewId);
    }

    public ReviewDto deleteLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewDbStorage.deleteLike(reviewId, userId);
        return findById(reviewId);
    }

    public ReviewDto deleteDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewDbStorage.deleteLike(reviewId, userId);
        return findById(reviewId);
    }

    private void validateReviewRequest(NewReviewRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ValidationException("Содержание отзыва не может быть пустым");
        }
        if (request.getUserId() == null) {
            throw new ValidationException("ID пользователя должен быть указан");
        }
        if (request.getFilmId() == null) {
            throw new ValidationException("ID фильма должен быть указан");
        }
        if (request.getIsPositive() == null) {
            throw new ValidationException("Тип отзыва (положительный/отрицательный) должен быть указан");
        }
        userDbStorage.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + request.getUserId() + " не найден"));
        filmDbStorage.findById(request.getFilmId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + request.getFilmId() + " не найден"));
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        reviewDbStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
        userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
