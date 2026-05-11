package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();

        review.setId(rs.getLong("id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUseful(rs.getInt("useful"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        review.setLikesCount(rs.getInt("likes_count"));
        review.setDislikesCount(rs.getInt("dislikes_count"));

        return review;
    }
}
