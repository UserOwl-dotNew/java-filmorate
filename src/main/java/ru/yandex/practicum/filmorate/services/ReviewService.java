package ru.yandex.practicum.filmorate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.db.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewDbStorage reviewStorage;
    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewDbStorage reviewStorage, ReviewMapper reviewMapper) {
        this.reviewStorage = reviewStorage;
        this.reviewMapper = reviewMapper;
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
        return reviewStorage.removeReview(id);
    }

    public Review create(Review review) {
        return reviewStorage.create(review);
    }

    public Review update(Review newReview) {
        return reviewStorage.update(newReview);
    }

}
