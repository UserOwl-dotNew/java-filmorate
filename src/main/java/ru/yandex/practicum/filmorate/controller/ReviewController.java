package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.services.ReviewService;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;
    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    @GetMapping
    public Collection<ReviewDto> findAllReviews() {
        return service.findAllReviews();
    }

    @GetMapping("/{reviewId}")
    public Optional<ReviewDto> findById(@PathVariable long reviewId) {
        return service.findById(reviewId);
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        System.out.println(review);
        Review createdReview = service.create(review);
        log.info("Создан отзыв {}.", createdReview.getContent());
        return createdReview;
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review newReview) {
        Review oldReview = service.update(newReview);

        log.info("Обновлён фильм с идентификатором {}.", oldReview.getId());

        return oldReview;
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Review> removerReview(@PathVariable Long reviewId) {
        Optional<ReviewDto> optReviewDto = service.findById(reviewId);

        optReviewDto.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", reviewId)));

        Optional<Review> optReview = service.removeReview(reviewId);

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }
}
