package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewReaction;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewReactionRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final ReviewRowMapper mapper;
    private final UserRowMapper userMapper;
    private final FilmRowMapper filmMapper;
    private final ReviewReactionRowMapper reviewReactionMapper;

    public List<Review> findAllReviews(Long filmId, Integer count) {
        if (count == null) {
            count = 10;
        }

        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("count", count);

        String query = "SELECT r.*, " +
                "    SUM(CASE WHEN rr.reaction_type = 'like' THEN 1 ELSE 0 END) as likes_count, " +
                "    SUM(CASE WHEN rr.reaction_type = 'dislike' THEN 1 ELSE 0 END) as dislikes_count " +
                "FROM reviews AS r "
                + "LEFT JOIN review_reactions rr ON r.review_id = rr.review_id ";

        if (filmId != null) {
            namedParameters.addValue("film_id", filmId);
            query += "WHERE r.film_id = :film_id";
        }
        query += " GROUP BY r.review_id ";
        query += " ORDER BY r.useful DESC ";
        query += " LIMIT :count";

        return jdbc.query(query, namedParameters, mapper);
    }

    public Optional<Review> findById(Long reviewId) {
        return find(reviewId);
    }

    public Review create(Review review) {
        if (!review.validateErrors().isEmpty()) {
            String str = String.join(",", review.validateErrors());
            throw new ValidationException(str);
        }

        if (review.getFilmId() < 0 || review.getUserId() < 0) {
            throw new NotFoundException("Идентификатор меньше нуля.");
        }

        Optional<Film> film = findFilm(review.getFilmId());
        film.orElseThrow(() -> new NotFoundException(String.format("Фильм с id=%s не найден", review.getFilmId())));

        Optional<User> user = findUser(review.getUserId());
        user.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", review.getUserId())));

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

        jdbc.update(sql, params, keyHolder, new String[]{"review_id"});

        Long reviewId = keyHolder.getKey().longValue();
        review.setReviewId(reviewId);
        review.setCreatedAt(now);
        review.setUseful(useful);

        return review;
    }

    public Review update(Review newReview) {
        if (newReview.getReviewId() == null) {
            throw new ValidationException("Id должен быть указан.");
        }

        Optional<Review> review = find(newReview.getReviewId());

        review.orElseThrow(() -> new NotFoundException(String.format("Отзыв с review_id=%s не найден", newReview.getReviewId())));

        String sql = "UPDATE reviews SET content = :content, is_positive = :is_positive WHERE review_id = :review_id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", newReview.getContent())
                .addValue("is_positive", newReview.getIsPositive())
                .addValue("review_id", newReview.getReviewId());

        jdbc.update(sql, params);

        Optional<Review> optReview = find(newReview.getReviewId());

        return optReview.get();
    }

    public Optional<Review> find(Long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("review_id", id);

        String sql = "SELECT " +
                "    r.*, " +
                "    SUM(CASE WHEN rr.reaction_type = 'like' THEN 1 ELSE 0 END) as likes_count, " +
                "    SUM(CASE WHEN rr.reaction_type = 'dislike' THEN 1 ELSE 0 END) as dislikes_count " +
                "FROM reviews AS r " +
                "LEFT JOIN review_reactions rr ON r.review_id = rr.review_id " +
                "WHERE r.review_id = :review_id " +
                "GROUP BY r.review_id";

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

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с review_id=%s не найден", reviewId)));

        String query = "DELETE FROM reviews " +
                "WHERE reviews.review_id  = :reviewId";
        jdbc.update(query, namedParameters);
        return optReview;
    }

    public Optional<Review> addLike(Long reviewId, Long userId) {
        Optional<ReviewReaction> reaction = findLike(reviewId, userId);

        if (reaction.isPresent()) {
            throw new ValidationException("Реакцию уже проставил пользователь.");
        }

        addReaction("like", reviewId, userId);

        incrUseful(reviewId);

        return find(reviewId);
    }

    public Optional<Review> addDislike(Long reviewId, Long userId) {
        Optional<ReviewReaction> reaction = findDislike(reviewId, userId);

        if (reaction.isPresent()) {
            throw new ValidationException("Реакцию уже проставил пользователь.");
        }

        Optional<ReviewReaction> likeReaction = findLike(reviewId, userId);

        if (likeReaction.isPresent()) {
            removeReaction("like", reviewId, userId);
            decrUseful(reviewId);
        }

        addReaction("dislike", reviewId, userId);

        decrUseful(reviewId);

        return find(reviewId);
    }

    public Optional<Review> removeLike(Long reviewId, Long userId) {
        Optional<ReviewReaction> reaction = findLike(reviewId, userId);

        if (reaction.isEmpty()) {
            throw new ValidationException("Лайк пользователь не ставил.");
        }

        removeReaction("like", reviewId, userId);
        decrUseful(reviewId);
        return find(reviewId);
    }

    public Optional<Review> removeDislike(Long reviewId, Long userId) {
        Optional<ReviewReaction> reaction = findDislike(reviewId, userId);

        if (reaction.isEmpty()) {
            throw new ValidationException("Лайк пользователь не ставил.");
        }
        removeReaction("dislike", reviewId, userId);
        decrUseful(reviewId);
        return find(reviewId);
    }

    public Optional<Review> addReaction(String reactionType, Long reviewId, Long userId) {
        if (!(reactionType.equals("like") || reactionType.equals("dislike"))) {
            throw new ValidationException("Тип реакции должен быть like или dislike");
        }

        Optional<Review> optReview = find(reviewId);

        Optional<User> user = findUser(userId);

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с review_id=%s не найден", reviewId)));
        user.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        LocalDateTime now = LocalDateTime.now();

        String sql = "INSERT INTO review_reactions (review_id, user_id, reaction_type, created_at)" +
                "VALUES (:review_id, :user_id, :reaction_type, :created_at)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", optReview.get().getReviewId())
                .addValue("user_id", userId)
                .addValue("reaction_type", reactionType)
                .addValue("created_at", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(sql, params, keyHolder, new String[]{"review_id"});

        optReview = find(reviewId);

        return optReview;
    }

    public Optional<Review> removeReaction(String reactionType, Long reviewId, Long userId) {
        if (!(reactionType.equals("like") || reactionType.equals("dislike"))) {
            throw new ValidationException("Тип реакции должен быть like или dislike");
        }

        Optional<Review> optReview = find(reviewId);

        Optional<User> user = findUser(userId);

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("reactionType", reactionType)
                .addValue("userId", userId);

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с review_id=%s не найден", reviewId)));
        user.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        String query = "DELETE FROM review_reactions " +
                "WHERE review_reactions.review_id = :reviewId " +
                "AND review_reactions.reaction_type = :reactionType " +
                "AND review_reactions.user_id = :userId ";
        jdbc.update(query, namedParameters);
        optReview = find(reviewId);

        return optReview;
    }

    public void incrUseful(Long reviewId) {
        if (reviewId == null) {
            throw new ValidationException("Id должен быть указан.");
        }

        String sql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = :review_id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId);

        jdbc.update(sql, params);
    }

    public void decrUseful(Long reviewId) {
        if (reviewId == null) {
            throw new ValidationException("Id должен быть указан.");
        }

        String sql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = :review_id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId);

        jdbc.update(sql, params);
    }

    private Optional<User> findUser(Long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);

        String sql = "SELECT * FROM users WHERE id = :id";
        try {
            User user = jdbc.queryForObject(sql, namedParameters, userMapper);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Optional<Film> findFilm(Long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);

        String sql = "SELECT * FROM films WHERE id = :id";
        try {
            Film film = jdbc.queryForObject(sql, namedParameters, filmMapper);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Optional<ReviewReaction> findLike(Long reviewId, Long userId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("review_id", reviewId);

        String sql = "SELECT * FROM review_reactions WHERE reaction_type = 'like' AND review_id = :review_id AND user_id = :user_id";
        try {
            ReviewReaction reaction = jdbc.queryForObject(sql, namedParameters, reviewReactionMapper);
            return Optional.ofNullable(reaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    private Optional<ReviewReaction> findDislike(Long reviewId, Long userId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("review_id", reviewId);

        String sql = "SELECT * FROM review_reactions WHERE reaction_type = 'dislike' AND review_id = :review_id AND user_id = :user_id";
        try {
            ReviewReaction reaction = jdbc.queryForObject(sql, namedParameters, reviewReactionMapper);
            return Optional.ofNullable(reaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
