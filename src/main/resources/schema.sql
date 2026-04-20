CREATE TABLE IF NOT EXISTS mpa (
    id INTEGER PRIMARY KEY,
    name VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS  directors (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    release_date DATE,
    duration DOUBLE,
    mpa_id INTEGER,
    FOREIGN KEY (mpa_id) REFERENCES mpa(id) ON DELETE SET NULL,
    FOREIGN KEY (director_id) REFERENCES directors(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_directors (
	film_id BIGINT NOT NULL,
    director_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, director_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (director_id) REFERENCES directors(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    UNIQUE (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS friend_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    to_user_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    status_id INTEGER NOT NULL,
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (from_user_id, to_user_id)
);