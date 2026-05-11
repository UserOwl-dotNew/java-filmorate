package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.ReviewReaction;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewReactionRowMapper implements RowMapper<ReviewReaction> {
    @Override
    public ReviewReaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewReaction reaction = new ReviewReaction();

        reaction.setReviewId(rs.getLong("review_id"));
        reaction.setUserId(rs.getLong("user_id"));
        reaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        return reaction;
    }
}

