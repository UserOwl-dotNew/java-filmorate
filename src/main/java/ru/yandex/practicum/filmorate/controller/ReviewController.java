package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.services.ReviewService;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;
    private final UserService userService;

    @GetMapping
    public Collection<ReviewDto> findAllReviews(@RequestParam(required = false) Long filmId,
                                                @RequestParam(required = false) Integer count) {
        return service.findAllReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public ReviewDto findById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с review_id=" + id + " не найден"));
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("Создание отзыва: {}", review);
        Review createdReview = service.create(review);
        log.info("Создан отзыв {}.", createdReview.getContent());
        return createdReview;
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review newReview) {
        Review oldReview = service.update(newReview);
        log.info("Обновлён отзыв с идентификатором {}.", oldReview.getReviewId());
        return oldReview;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Review> removerReview(@PathVariable Long id) {
        Optional<ReviewDto> optReviewDto = service.findById(id);
        optReviewDto.orElseThrow(() -> new NotFoundException("Отзыв с id=" + id + " не найден"));

        Optional<Review> optReview = service.removeReview(id);

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> addLike(@PathVariable Long id, @PathVariable Long userId) {
        Optional<User> optUser = userService.find(userId);
        optUser.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Optional<Review> optReview = service.addLike(id, userId);

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislike(@PathVariable Long id, @PathVariable Long userId) {
        Optional<User> optUser = userService.find(userId);
        optUser.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Optional<Review> optReview = service.addDislike(id, userId);

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> removerLike(@PathVariable Long id, @PathVariable Long userId) {
        Optional<User> optUser = userService.find(userId);
        optUser.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Optional<Review> optReview = service.removeLike(id, userId);
        optReview.orElseThrow(() -> new NotFoundException("Отзыв с id=" + id + " не найден"));

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        Optional<User> optUser = userService.find(userId);
        optUser.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Optional<Review> optReview = service.removeDislike(id, userId);
        optReview.orElseThrow(() -> new NotFoundException("Отзыв с id=" + id + " не найден"));

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }
}