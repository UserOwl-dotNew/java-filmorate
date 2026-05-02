package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FriendRequest;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.FriendsRequestDbStorage;
import ru.yandex.practicum.filmorate.storage.db.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FriendRequestMapper;
import ru.yandex.practicum.filmorate.storage.mappers.LikeRowMapper;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class,
        UserRowMapper.class,
        FilmDbStorage.class,
        FilmRowMapper.class,
        LikeDbStorage.class,
        LikeRowMapper.class,
        FriendsRequestDbStorage.class,
        FriendRequestMapper.class
})
class FilmorateApplicationTests {

    @Autowired
    private final UserDbStorage userStorage;
    private final FriendsRequestDbStorage friendsStorage;
    private final FilmDbStorage filmStorage;
    private final LikeDbStorage likeStorage;

    private Film buildFilm(String name, String description) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setGenres(new ArrayList<>()); // пустой список жанров — не null!
        film.setMpa(new MPA());            // пустой MPA — не null!
        film.setReleaseDate(LocalDate.of(2000, 1, 1)); // любая дата после 1895
        film.setDuration(120D);            // положительное число
        return film;
    }
    private User buildUser(String name, String email, String login) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(LocalDate.now());
        return user;
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Long createDirector(String name) {
        jdbcTemplate.update("INSERT INTO directors (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM directors WHERE name = ?", Long.class, name);
    }

    private void linkDirectorToFilm(Long filmId, Long directorId) {
        jdbcTemplate.update(
                "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                filmId, directorId);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM directors");
        jdbcTemplate.execute("ALTER TABLE directors ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.execute("DELETE FROM film_genre");
        jdbcTemplate.execute("DELETE FROM film_directors");

        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("ALTER TABLE likes ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.execute("DELETE FROM friend_request");
        jdbcTemplate.execute("ALTER TABLE friend_request ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    public void testFindUserById() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        Optional<User> userOptional = userStorage.findById(userCreate.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u.getId()).isEqualTo(userCreate.getId())
                );
    }

    @Test
    public void testCreateUser() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        assertThat(userCreate)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Dima")
                .hasFieldOrPropertyWithValue("email", "blabla@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "Login");
    }

    @Test
    void testFindAllUsers() throws InternalServerException {
        List<User> users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(0);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(1);
    }

    @Test
    void testUpdateUser() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        User newUser = user;
        newUser.setId(userCreate.getId());
        newUser.setName("Vova");

        User updateUser = userStorage.update(newUser);
        assertThat(updateUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Vova");
    }

    @Test
    void testDeleteUser() throws InternalServerException {
        List<User> users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(0);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(1);

        userStorage.delete(userCreate.getId());
        users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(0);
    }

    @Test
    void testFindUserByEmail() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        Optional<User> findUserByEmail = userStorage.findByEmail("blabla@yandex.ru");
        assertThat(findUserByEmail)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u.getId()).isEqualTo(user.getId())
                );
    }

    @Test
    void testAddFriend() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        User user1 = new User();
        user1.setName("Vova");
        user1.setEmail("vova@yandex.ru");
        user1.setLogin("LoginVova");
        user1.setBirthday(LocalDate.now());
        User userCreate1 = userStorage.create(user1);

        List<FriendRequest> friendsList = friendsStorage.findAll();
        assertThat(friendsList.size()).isEqualTo(0);

        Long idFriendRequest = friendsStorage.create(user.getId(), user1.getId());
        friendsList = friendsStorage.findAll();

        assertThat(friendsList.size()).isEqualTo(1);
    }

    @Test
    void testDeleteFriend() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        User user1 = new User();
        user1.setName("Vova");
        user1.setEmail("vova@yandex.ru");
        user1.setLogin("LoginVova");
        user1.setBirthday(LocalDate.now());
        User userCreate1 = userStorage.create(user1);

        List<FriendRequest> friendsList = friendsStorage.findAll();
        assertThat(friendsList.size()).isEqualTo(0);

        Long idFriendRequest = friendsStorage.create(userCreate.getId(), userCreate1.getId());
        friendsList = friendsStorage.findAll();
        assertThat(friendsList.size()).isEqualTo(1);

        friendsStorage.delete(userCreate.getId(), userCreate1.getId());

        friendsList = friendsStorage.findAll();

        assertThat(friendsList.size()).isEqualTo(0);
    }

    @Test
    void testGetUserFriends() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        User user1 = new User();
        user1.setName("Vova");
        user1.setEmail("vova@yandex.ru");
        user1.setLogin("LoginVova");
        user1.setBirthday(LocalDate.now());
        User userCreate1 = userStorage.create(user1);

        List<FriendRequest> friendsList = friendsStorage.findAll();
        assertThat(friendsList.size()).isEqualTo(0);

        Long idFriendRequest = friendsStorage.create(userCreate.getId(), userCreate1.getId());
        friendsList = friendsStorage.findAll();
        assertThat(friendsList.size()).isEqualTo(1);

        List<User> friendsUser = userStorage.findFriends(userCreate.getId());

        System.out.println("frinedsUser: " + friendsUser + "\n");
        Optional<User> friend = friendsUser.stream()
                .filter(u -> u.getId().equals(userCreate1.getId()))
                .findFirst();
        assertThat(friend)
                .isPresent()
                .hasValueSatisfying(u ->
                        u.getId().equals(userCreate1.getId())
                );
    }

    @Test
    void testGetMutualFriends() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        User user1 = new User();
        user1.setName("Vova");
        user1.setEmail("vovan@yandex.ru");
        user1.setLogin("LoginVovan");
        user1.setBirthday(LocalDate.now());
        User userCreate1 = userStorage.create(user1);

        User user2 = new User();
        user2.setName("Vova");
        user2.setEmail("vova@yandex.ru");
        user2.setLogin("LoginVova");
        user2.setBirthday(LocalDate.now());
        User userCreate2 = userStorage.create(user2);

        List<FriendRequest> friendsList = friendsStorage.findAll();
        assertThat(friendsList.size()).isEqualTo(0);

        Long idFriendRequest = friendsStorage.create(userCreate.getId(), userCreate1.getId());

        System.out.printf("userCreate1: %d, userCreate2: %d \n", userCreate1.getId(), userCreate2.getId());
        Long idFriendRequest1 = friendsStorage.create(userCreate1.getId(), userCreate2.getId());

        Long idFriendRequest2 = friendsStorage.create(userCreate.getId(), userCreate2.getId());

        friendsList = friendsStorage.findAll();
        assertThat(friendsList.size()).isEqualTo(3);

        List<User> mutualFriends = userStorage.findMutualFriends(userCreate.getId(), userCreate1.getId());
        assertThat(mutualFriends.size()).isEqualTo(1);
    }

    @Test
    void testAddSelfAsFriend_ShouldThrowException() throws InternalServerException {
        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        List<User> friends = userStorage.findFriends(userCreate.getId());
        assertThat(friends.size()).isEqualTo(0);

        assertThrows(ConditionsNotMetException.class, () -> {
            friendsStorage.create(userCreate.getId(), userCreate.getId());
        });

        friends = userStorage.findFriends(userCreate.getId());
        assertThat(friends.size()).isEqualTo(0);
    }

    @Test
    void testCreateFilm() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        assertThat(createFilm)
                .hasFieldOrPropertyWithValue("name", "Name")
                .hasFieldOrPropertyWithValue("description", "very funny film")
                .hasFieldOrPropertyWithValue("duration", 120D);
    }

    @Test
    void testFindFilmById() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        Optional<Film> findFilm = filmStorage.findById(createFilm.getId());
        assertThat(findFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f.getId()).isEqualTo(createFilm.getId())
                );
    }

    @Test
    void testFindAllFilms() throws InternalServerException {
        List<Film> listFilms = filmStorage.findAll();
        assertThat(listFilms.size()).isEqualTo(0);

        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        listFilms = filmStorage.findAll();
        assertThat(listFilms.size()).isEqualTo(1);
    }

    @Test
    void testUpdateFilm() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        Film newFilm = film;
        newFilm.setDescription("NOT FUNNY FILM");

        Film updateFilm = filmStorage.update(newFilm);
        assertThat(updateFilm)
                .hasFieldOrPropertyWithValue("description", "NOT FUNNY FILM")
                .hasFieldOrPropertyWithValue("id", createFilm.getId());
    }

    @Test
    void testDeleteFilm() throws InternalServerException {
        List<Film> listFilms = filmStorage.findAll();
        assertThat(listFilms.size()).isEqualTo(0);

        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        listFilms = filmStorage.findAll();
        assertThat(listFilms.size()).isEqualTo(1);

        System.out.println("createFilm.getId(): " + createFilm.getId());
        filmStorage.delete(createFilm.getId());

        listFilms = filmStorage.findAll();
        assertThat(listFilms.size()).isEqualTo(0);
    }

    @Test
    void testFindPopularFilms() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        likeStorage.create(userCreate.getId(), createFilm.getId());

        Film film1 = new Film();
        film1.setName("bobriki");
        film1.setGenres(new ArrayList<>());
        film1.setMpa(new MPA());
        film1.setDescription("very good film");
        film1.setReleaseDate(LocalDate.now());
        film1.setDuration(120D);

        Film createFilm1 = filmStorage.create(film1);

        User user1 = new User();
        user1.setName("Vova");
        user1.setEmail("Vova@yandex.ru");
        user1.setLogin("LoginVova");
        user1.setBirthday(LocalDate.now());
        User userCreate1 = userStorage.create(user1);

        likeStorage.create(userCreate.getId(), createFilm1.getId());
        likeStorage.create(userCreate1.getId(), createFilm1.getId());

        List<Film> filmPopularList = filmStorage.findPopular(2);
        Film firstPopularFilm = filmPopularList.getFirst();

        assertThat(firstPopularFilm)
                .hasFieldOrPropertyWithValue("id", createFilm1.getId());
    }

    @Test
    void testFindNonExistentFilm_ShouldReturnEmpty() {
        Optional<Film> findNonExistenFilm = filmStorage.findById(1);
        assertThat(findNonExistenFilm)
                .isEmpty();
    }

    @Test
    void testAddLike() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        Long countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(0);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        likeStorage.create(userCreate.getId(), createFilm.getId());

        countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(1);
    }

    @Test
    void testRemoveLike() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        Long countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(0);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        likeStorage.create(userCreate.getId(), createFilm.getId());

        countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(1);

        countLikes = likeStorage.delete(userCreate.getId(), createFilm.getId());
        assertThat(countLikes).isEqualTo(0);
    }

    @Test
    void testCountLikes() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        Long countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(0);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        User user1 = new User();
        user1.setName("Dima");
        user1.setEmail("blaa@yandex.ru");
        user1.setLogin("Login");
        user1.setBirthday(LocalDate.now());
        User userCreate1 = userStorage.create(user1);

        User user2 = new User();
        user2.setName("Dima");
        user2.setEmail("blablo@yandex.ru");
        user2.setLogin("Login");
        user2.setBirthday(LocalDate.now());
        User userCreate2 = userStorage.create(user2);

        likeStorage.create(userCreate.getId(), createFilm.getId());
        likeStorage.create(userCreate1.getId(), createFilm.getId());
        likeStorage.create(userCreate2.getId(), createFilm.getId());

        countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(3);
    }

    @Test
    void testAddDuplicateLike_ShouldHandleGracefully() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);
        Film createFilm = filmStorage.create(film);

        Long countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(0);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        likeStorage.create(userCreate.getId(), createFilm.getId());
        countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(1);

        likeStorage.create(userCreate.getId(), createFilm.getId());
        countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(1);
    }

    @Test
    void testRemoveNonExistentLike_ShouldReturnSameCount() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);
        Film createFilm = filmStorage.create(film);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.now());
        User userCreate = userStorage.create(user);

        Long countLikes = likeStorage.delete(userCreate.getId(), createFilm.getId());
        assertThat(countLikes).isEqualTo(0);
    }

    @Test
    void testCountLikesForFilmWithoutLikes() throws InternalServerException {
        Film film = new Film();
        film.setName("Name");
        film.setGenres(new ArrayList<>());
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120D);
        Film createFilm = filmStorage.create(film);

        Long countLikes = likeStorage.countLikes(createFilm.getId());
        assertThat(countLikes).isEqualTo(0);
    }

    // Регистр не важен
    @Test
    void testSearchByTitle_caseInsensitive() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица", "Sci-fi боевик"));

        //Ищем строчными буквами - должен найти
        List<Film> result = filmStorage.search("матрица", List.of("title"));
        assertThat(result).hasSize(1);
    }

    // Частичное совпадение
    @Test
    void testSearchByTitle_partinalMatch() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица: Перезагрузка", "Продолжение"));
        filmStorage.create(buildFilm("Матрица: Революция", "Финал"));
        filmStorage.create(buildFilm("Гладиатор", "Рим"));

        // "матриц" должен найти оба фильма
        List<Film> result = filmStorage.search("матриц", List.of("title"));

        assertThat(result).hasSize(2);
    }

    // Нет совпадений
    @Test
    void testSearchByTitle_noMatch() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица", "Sci-fi боевик"));

        List<Film> result = filmStorage.search("Аватар", List.of("title"));

        assertThat(result).isEmpty();
    }

    // Поиск по описанию
    @Test
    void testSearchByDescription_shouldFindMatch() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица", "Захватывающий sci-fi боевик"));
        filmStorage.create(buildFilm("Гладиатор", "Исторический фильм про Рим"));

        List<Film> result = filmStorage.search("sci-fi", List.of("description"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Матрица");
    }

    // Поиск по обоим полям - слово только в названии
    @Test
    void testSearchByBoth_matchInTitleOnly() throws InternalServerException {
        filmStorage.create(buildFilm("Боевик года", "Хорошее кино"));
        filmStorage.create(buildFilm("Комедия", "Веселый фильм"));

        List<Film> result = filmStorage.search("боевик", List.of("title", "description"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Боевик года");
    }

    // Поиск по обоим полям - слово только в описании
    @Test
    void testSearchByBoth_matchInDescriptionOnly() throws InternalServerException {
        filmStorage.create(buildFilm("Тихая гавань", "Лучший боевик сезона"));
        filmStorage.create(buildFilm("Комедия", " Веселый фильм"));

        List<Film> result = filmStorage.search("боевик", List.of("title", "description"));

        assertThat(result).hasSize(1);
    }

    // Слово и в названии и в описании - фильм не дублируеться
    @Test
    void testSearchByBoth_noDuplicateWhenMatchInBothFields() throws InternalServerException {
        filmStorage.create(buildFilm("Боевик", "Отличный боевик"));

        List<Film> result = filmStorage.search("боевик", List.of("title", "description"));

        assertThat(result).hasSize(1);
    }

    // Пустой query - возвращает пустой список
    @Test
    void testSearch_emptyQueryReturnsEmpty() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица", "Sci-fi"));

        List<Film> resuilt = filmStorage.search(" ", List.of("title"));

        assertThat(resuilt).isEmpty();
    }

    // Пустой by - возвращает пустой список
    @Test
    void testSearch_emptyByReturnsEmpty() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица", "Sci-fi"));

        List<Film> result = filmStorage.search("Матрица", List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void testSearchByTitle_shouldFindMatch() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица", "Sci-fi боевик"));
        filmStorage.create(buildFilm("Гладиатор", "Исторический фильм"));

        List<Film> result = filmStorage.search("Матрица", List.of("title"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Матрица");
        assertThat(result.get(0).getDirectors()).isNotNull(); // ← добавить
    }

    // Поиск по директору
    @Test
    void testSearchByDirector_shouldReturnEmpty() throws InternalServerException {
        filmStorage.create(buildFilm("Матрица", "Sci-fi"));

        // by=director без реализации должен вернуть пустой список, не ошибку
        List<Film> result = filmStorage.search("Матрица", List.of());

        assertThat(result).isEmpty();
    }

    // directors не null в findById
    @Test
    void testFindById_shouldReturnFilmWithDirectors() throws InternalServerException {
        Film film = filmStorage.create(buildFilm("Тест", "Описание"));

        Optional<Film> found = filmStorage.findById(film.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDirectors()).isNotNull(); // пустой список, но не null
    }

    //создаёт директора и фильм, проверяет что поиск по части имени директора
    // возвращает правильный фильм с заполненным списком директоров.
    @Test
    void testSearchByDirector_shouldFindMatch() throws InternalServerException {
        Long directorId = createDirector("Кристофер Нолан");
        Film film = filmStorage.create(buildFilm("Начало", "Фильм про сны"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.search("нолан", List.of("director"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Начало");
        assertThat(result.get(0).getDirectors()).isNotNull();
        assertThat(result.get(0).getDirectors()).hasSize(1);
        assertThat(result.get(0).getDirectors().get(0).getName()).isEqualTo("Кристофер Нолан");
    }

    // проверяет что поиск по директору не зависит от регистра
    @Test
    void testSearchByDirector_caseInsensitive() throws InternalServerException {
        Long directorId = createDirector("Стэнли Кубрик");
        Film film = filmStorage.create(buildFilm("Сияние", "Хоррор"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.search("КУБРИК", List.of("director"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Сияние");
    }

    // проверяет что по частичному совпадению имени одного директора находятся все его фильмы
    @Test
    void testSearchByDirector_partialMatch() throws InternalServerException {
        Long directorId = createDirector("Квентин Тарантино");
        Film film1 = filmStorage.create(buildFilm("Криминальное чтиво", "Гангстеры"));
        Film film2 = filmStorage.create(buildFilm("Бешеные псы", "Ограбление"));
        linkDirectorToFilm(film1.getId(), directorId);
        linkDirectorToFilm(film2.getId(), directorId);

        List<Film> result = filmStorage.search("тарант", List.of("director"));

        assertThat(result).hasSize(2);
    }

    //проверяет что поиск по несуществующему имени возвращает пустой список
    @Test
    void testSearchByDirector_noMatch() throws InternalServerException {
        Long directorId = createDirector("Тим Бёртон");
        Film film = filmStorage.create(buildFilm("Битлджус", "Фэнтези"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.search("нолан", List.of("director"));

        assertThat(result).isEmpty();
    }

    //фильм находится когда запрос совпадает с именем директора, но не с названием фильма
    @Test
    void testSearchByTitleOrDirector_matchByDirector() throws InternalServerException {
        Long directorId = createDirector("Спилберг");
        Film film = filmStorage.create(buildFilm("Список Шиндлера", "Исторический"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.search("спилберг", List.of("title", "director"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Список Шиндлера");
    }

    //фильм находится когда запрос совпадает с названием фильма, но не с именем директора
    @Test
    void testSearchByTitleOrDirector_matchByTitle() throws InternalServerException {
        Long directorId = createDirector("Неизвестный Режиссёр");
        Film film = filmStorage.create(buildFilm("Аватар", "Фантастика"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.search("аватар", List.of("title", "director"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Аватар");
    }

    //когда запрос совпадает и с названием и с именем директора,
    // фильм возвращается один раз без дублирования
    @Test
    void testSearchByTitleOrDirector_noDuplicates() throws InternalServerException {
        Long directorId = createDirector("Матрица Режиссёр");
        Film film = filmStorage.create(buildFilm("Матрица", "Sci-fi"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.search("матрица", List.of("title", "director"));

        assertThat(result).hasSize(1);
    }

    //проверяет что поле directors в результате поиска никогда не null и не пустое
    @Test
    void testSearchByDirector_directorsNotNull() throws InternalServerException {
        Long directorId = createDirector("Ридли Скотт");
        Film film = filmStorage.create(buildFilm("Гладиатор", "Рим"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.search("скотт", List.of("director"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDirectors()).isNotNull();
        assertThat(result.get(0).getDirectors()).isNotEmpty();
    }

    //два фильма одного директора возвращаются в порядке возрастания года выпуска
    @Test
    void testFindFilmsByDirector_sortByYear() throws InternalServerException {
        Long directorId = createDirector("Тест Директор");

        Film film1 = buildFilm("Старый фильм", "2000 год");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1 = filmStorage.create(film1);
        linkDirectorToFilm(film1.getId(), directorId);

        Film film2 = buildFilm("Новый фильм", "2020 год");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2 = filmStorage.create(film2);
        linkDirectorToFilm(film2.getId(), directorId);

        List<Film> result = filmStorage.findFilmsByDirector(directorId, "year");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReleaseDate()).isBefore(result.get(1).getReleaseDate());
    }

    //фильм с большим количеством лайков стоит первым в результате
    @Test
    void testFindFilmsByDirector_sortByLikes() throws InternalServerException {
        Long directorId = createDirector("Популярный Директор");

        Film film1 = filmStorage.create(buildFilm("Непопулярный", "мало лайков"));
        linkDirectorToFilm(film1.getId(), directorId);

        Film film2 = filmStorage.create(buildFilm("Популярный", "много лайков"));
        linkDirectorToFilm(film2.getId(), directorId);

        User user = userStorage.create(buildUser("Лайкер", "liker@test.ru", "liker"));
        likeStorage.create(user.getId(), film2.getId());

        List<Film> result = filmStorage.findFilmsByDirector(directorId, "likes");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Популярный");
    }

    //директор без фильмов возвращает пустой список
    @Test
    void testFindFilmsByDirector_noFilms() {
        Long directorId = createDirector("Режиссёр Без Фильмов");

        List<Film> result = filmStorage.findFilmsByDirector(directorId, "year");

        assertThat(result).isEmpty();
    }

    //проверяет что в каждом фильме из результата поле directors
    // заполнено корректно с правильным именем
    @Test
    void testFindFilmsByDirector_directorsNotNull() throws InternalServerException {
        Long directorId = createDirector("Проверочный Директор");
        Film film = filmStorage.create(buildFilm("Проверка", "Тест"));
        linkDirectorToFilm(film.getId(), directorId);

        List<Film> result = filmStorage.findFilmsByDirector(directorId, "year");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDirectors()).isNotNull();
        assertThat(result.get(0).getDirectors()).hasSize(1);
        assertThat(result.get(0).getDirectors().get(0).getName()).isEqualTo("Проверочный Директор");
    }
}