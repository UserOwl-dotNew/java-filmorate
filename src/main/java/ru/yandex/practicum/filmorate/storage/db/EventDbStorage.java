package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.util.List;

@Repository
public class EventDbStorage extends BaseDbStorage<Event> {

    private static final String INSERT_EVENT_QUERY =
            "INSERT INTO events (user_id, event_type, operation, entity_id, timestamp) VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_USER_FEED_QUERY =
            "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp ASC";

    public EventDbStorage(JdbcTemplate jdbc, EventRowMapper mapper) {
        super(jdbc, mapper);
    }

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        long timestamp = System.currentTimeMillis();
        jdbc.update(INSERT_EVENT_QUERY, userId, eventType.name(), operation.name(), entityId, timestamp);
    }

    public List<Event> getUserFeed(Long userId) {
        return jdbc.query(FIND_USER_FEED_QUERY, mapper, userId);
    }
}
