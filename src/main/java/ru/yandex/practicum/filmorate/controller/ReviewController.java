package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.services.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(@RequestBody NewReviewRequest request) throws InternalServerException {
        return reviewService.create(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto update(@RequestBody UpdateReviewRequest request) throws InternalServerException {
        return reviewService.update(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long reviewId) {
        reviewService.delete(reviewId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto findById(@PathVariable("id") Long reviewId) {
        return reviewService.findById(reviewId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReviewDto> findByFilmId(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        return reviewService.findByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto addLike(@PathVariable("id") Long reviewId,
                             @PathVariable Long userId) {
        return reviewService.addLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto deleteLike(@PathVariable("id") Long reviewId,
                                @PathVariable Long userId) {
        return reviewService.deleteLike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto addDislike(@PathVariable("id") Long reviewId,
                                @PathVariable Long userId) {
        return reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto deleteDislike(@PathVariable("id") Long reviewId,
                                   @PathVariable Long userId) {
        return reviewService.deleteDislike(reviewId, userId);
    }
}
