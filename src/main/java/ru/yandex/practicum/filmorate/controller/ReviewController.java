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
import ru.yandex.practicum.filmorate.exception.ParameterNotValidException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
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
    public Collection<ReviewDto> findAllReviews(@RequestParam(required = false) Long filmId, @RequestParam(required = false) Integer count) {
        return service.findAllReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public Optional<ReviewDto> findById(@PathVariable String id) {
        if (id == null || id.equals("null") || id.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        Long reviewId = Long.parseLong(id);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Review> removerReview(@PathVariable String id) {
        if (id == null || id.equals("null") || id.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        Long reviewId = Long.parseLong(id);

        Optional<ReviewDto> optReviewDto = service.findById(reviewId);

        optReviewDto.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", reviewId)));

        Optional<Review> optReview = service.removeReview(reviewId);

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> addLike(@PathVariable String id, @PathVariable String userId) {
        if (id == null || id.equals("null") || id.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        if (userId == null || userId.equals("null") || userId.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        Long reviewId = Long.parseLong(id);
        Long uId = Long.parseLong(userId);

        Optional<User> optUser = userService.find(uId);

        optUser.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        Optional<Review> optReview = service.addLike(reviewId, uId);

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislike(@PathVariable String id, @PathVariable String userId) {
        if (id == null || id.equals("null") || id.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        if (userId == null || userId.equals("null") || userId.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        Long reviewId = Long.parseLong(id);
        Long uId = Long.parseLong(userId);

        Optional<User> optUser = userService.find(uId);

        optUser.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        Optional<Review> optReview = service.addDislike(reviewId, uId);

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> removerLike(@PathVariable String id, @PathVariable String userId) {
        if (id == null || id.equals("null") || id.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        if (userId == null || userId.equals("null") || userId.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        Long reviewId = Long.parseLong(id);
        Long uId = Long.parseLong(userId);
        Optional<User> optUser = userService.find(uId);

        optUser.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        Optional<Review> optReview = service.removeLike(reviewId, uId);

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", id)));

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> removeDislike(@PathVariable String id, @PathVariable String userId) {
        if (id == null || id.equals("null") || id.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        if (userId == null || userId.equals("null") || userId.equals("undefined")) {
            throw new ParameterNotValidException("Идентификатор не может быть строкой 'null' или 'undefined'");
        }

        Long reviewId = Long.parseLong(id);
        Long uId = Long.parseLong(userId);

        Optional<User> optUser = userService.find(uId);

        optUser.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        Optional<Review> optReview = service.removeDislike(reviewId, uId);

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", id)));

        return optReview.map(review -> ResponseEntity
                .status(HttpStatus.OK)
                .body(review)).orElseGet(() -> ResponseEntity
                .notFound().build());
    }
}
