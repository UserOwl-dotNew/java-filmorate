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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
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
        query += " ORDER BY useful DESC ";
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

        if (review.getFilmId() < 0) {
            throw new ValidationException("Идентификатор меньше нуля.");
        }

        Optional<Film> film = findFilm(review.getFilmId());
        film.orElseThrow(() -> new NotFoundException(String.format("Фильм с id=%s не найден", review.getFilmId())));

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

    public Optional<Review> addLike(Long reviewId, Long userId) {
        Optional<Review> optReview = addReaction("like", reviewId, userId);

        return optReview;
    }

    public Optional<Review> addDislike(Long reviewId, Long userId) {
        Optional<Review> optReview = addReaction("dislike", reviewId, userId);

        return optReview;
    }

    public Optional<Review> removeLike(Long reviewId, Long userId) {
        Optional<Review> optReview = removeReaction("like", reviewId, userId);

        return optReview;
    }

    public Optional<Review> removeDislike(Long reviewId, Long userId) {
        Optional<Review> optReview = removeReaction("dislike", reviewId, userId);

        return optReview;
    }

    public Optional<Review> addReaction(String reactionType, Long reviewId, Long userId) {
        if (!(reactionType.equals("like") || reactionType.equals("dislike"))) {
            throw new ValidationException("Тип реакции должен быть like или dislike");
        }

        Optional<Review> optReview = find(reviewId);

        Optional<User> user = findUser(userId);

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", reviewId)));
        user.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        LocalDateTime now = LocalDateTime.now();

        String sql = "INSERT INTO review_reactions (review_id, user_id, reaction_type, created_at)" +
                "VALUES (:review_id, :user_id, :reaction_type, :created_at)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", optReview.get().getId())
                .addValue("user_id", userId)
                .addValue("reaction_type", reactionType)
                .addValue("created_at", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        Integer useful = getUseful(reviewId);
        setUseful(reviewId, useful);

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

        optReview.orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%s не найден", reviewId)));
        user.orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        String query = "DELETE FROM review_reactions " +
                "WHERE review_reactions.review_id = :reviewId " +
                "AND review_reactions.reaction_type = :reactionType " +
                "AND review_reactions.user_id = :userId ";
        jdbc.update(query, namedParameters);

        Integer useful = getUseful(reviewId);
        setUseful(reviewId, useful);

        return optReview;
    }

    public Integer setUseful(Long reviewId, Integer useful) {
        if (reviewId.equals(null)) {
            throw new ValidationException("Id должен быть указан.");
        }

        String sql = "UPDATE reviews SET useful = :useful WHERE id = :id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("useful", useful)
                .addValue("id", reviewId);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        return useful;
    }

    public Integer getUseful(Long reviewId) {
        Integer likes =  countLikes(reviewId);
        Integer dislikes = countDislikes(reviewId);

        if (dislikes > likes) {
            return 0;
        }
        return likes - dislikes;
    }

    public Integer countLikes(Long reviewId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("reviewId", reviewId);

        String query = "SELECT COUNT(review_reactions.review_id) FROM review_reactions WHERE review_reactions.reaction_type = 'like' AND review_reactions.review_id = :reviewId";
        Integer count = jdbc.queryForObject(query, namedParameters, Integer.class);
        return count;
    }

    public Integer countDislikes(Long reviewId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("reviewId", reviewId);

        String query = "SELECT COUNT(review_reactions.review_id) FROM review_reactions WHERE review_reactions.reaction_type = 'dislike' AND review_reactions.review_id = :reviewId";
        Integer count = jdbc.queryForObject(query, namedParameters, Integer.class);
        return count;
    }

    private Optional<User> findUser(Long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);

        String sql = "SELECT * FROM users WHERE id = :id";
        try {
            User user = jdbc.queryForObject(sql, namedParameters, userMapper);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Optional<Film> findFilm(Long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);

        String sql = "SELECT * FROM films WHERE id = :id";
        try {
            Film film = jdbc.queryForObject(sql, namedParameters, filmMapper);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
