package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> {

    private static final String INSERT_REVIEW_QUERY =
            "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_REVIEW_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

    private static final String DELETE_REVIEW_QUERY =
            "DELETE FROM reviews WHERE review_id = ?";

    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM reviews WHERE review_id = ?";

    private static final String FIND_ALL_QUERY =
            "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

    private static final String FIND_BY_FILM_ID_QUERY =
            "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

    private static final String ADD_LIKE_QUERY =
            "MERGE INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)";

    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";

    private static final String UPDATE_USEFUL_QUERY =
            "UPDATE reviews SET useful = (SELECT COALESCE(SUM(CASE WHEN is_like THEN 1 ELSE -1 END), 0) " +
                    "FROM review_likes WHERE review_id = ?) WHERE review_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, ReviewRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Review create(Review review) throws InternalServerException {
        long id = insert(INSERT_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful());
        review.setReviewId(id);
        return review;
    }

    public Review update(Review review) throws InternalServerException {
        update(UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        return findById(review.getReviewId()).orElse(review);
    }

    public boolean deleteById(Long reviewId) {
        return delete(DELETE_REVIEW_QUERY, reviewId);
    }

    public Optional<Review> findById(Long reviewId) {
        return findOne(FIND_BY_ID_QUERY, reviewId);
    }

    public List<Review> findAll(int count) {
        return findMany(FIND_ALL_QUERY, count);
    }

    public List<Review> findByFilmId(Long filmId, int count) {
        return findMany(FIND_BY_FILM_ID_QUERY, filmId, count);
    }

    public void addLike(Long reviewId, Long userId, boolean isLike) {
        jdbc.update(ADD_LIKE_QUERY, reviewId, userId, isLike);
        jdbc.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_LIKE_QUERY, reviewId, userId);
        jdbc.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
    }
}
