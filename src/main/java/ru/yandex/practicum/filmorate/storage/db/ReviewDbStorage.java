package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final ReviewRowMapper mapper;

//    public List<Review> findAllReviews() {
//        String query = "SELECT * FROM reviews";
//        return jdbc.query(query, mapper);
//    }

    public List<Review> findAllReviews(Long filmId, Integer count) {
        if (count == null) {
            count = 10;
        }

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("count", count);

        String query = "SELECT r.* " +
                "FROM reviews AS r ";

        if (filmId != null) {
            ((MapSqlParameterSource) namedParameters).addValue("film_id", filmId);
            query += "WHERE r.film_id = :film_id";
        }
        query += " LIMIT :count";

        return jdbc.query(query, namedParameters, mapper);
    }

    public Optional<Review> findById(Long reviewId) {
        return find(reviewId);
    }

    public Review create(Review review) {
        if (review.validateErrors().size() > 0) {
            String str = review.validateErrors()
                    .stream()
                    .collect(Collectors.joining(","));
            throw new ValidationException(str);
        }

        LocalDateTime now = LocalDateTime.now();
        Integer useful = 0;

        String sql = "INSERT INTO reviews (content, is_positive, useful, user_id, film_id, created_at) " +
                "VALUES (:content, :is_positive, :useful, :user_id, :film_id, :created_at)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("is_positive", review.getIsPositive())
                .addValue("useful", useful)
                .addValue("user_id", review.getUserId())
                .addValue("film_id", review.getFilmId())
                .addValue("created_at", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        Long reviewId = keyHolder.getKey().longValue();
        review.setId(reviewId);
        review.setCreatedAt(now);
        review.setUseful(useful);

        return review;
    }

    public Review update(Review newReview) {
        if (newReview.getId().equals(null)) {
            throw new ValidationException("Id должен быть указан.");
        }

        Optional<Review> review = find(newReview.getId());

        review.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", newReview.getId())));

        String sql = "UPDATE reviews SET content = :content, is_positive = :is_positive WHERE id = :id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", newReview.getContent())
                .addValue("is_positive", newReview.getIsPositive())
                .addValue("id", newReview.getId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        return newReview;
    }

    public Optional<Review> find(Long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);

        String sql = "SELECT r.* " +
                "FROM reviews AS r " +
                "WHERE r.id = :id";
        try {
            Review review = jdbc.queryForObject(sql, namedParameters, mapper);
            return Optional.of(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Review> removeReview(Long reviewId) {
        Optional<Review> optReview = find(reviewId);

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("reviewId", reviewId);

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", reviewId)));

        String query = "DELETE FROM reviews " +
                "WHERE reviews.id  = :reviewId";
        jdbc.update(query, namedParameters);
        return optReview;
    }
}
