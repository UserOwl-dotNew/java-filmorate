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
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.db.*;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, GenreDbStorage.class})
class FilmorateApplicationTests {
    @Autowired
    private final UserDbStorage userStorage;
    private final FriendsRequestDbStorage friendsStorage;
    private final FilmDbStorage filmStorage;
    private final LikeDbStorage likeStorage;
    private final GenreDbStorage genreStorage;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
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
        LocalDate now = LocalDate.now();

        Optional<Genre> optGenre = genreStorage.findById(1L);

        List<Genre> genres = new ArrayList<>();
        genres.add(optGenre.get());

        Film film = new Film();
        film.setName("Name");
        film.setGenres(genres);
        film.setMpa(new MPA());
        film.setDescription("very funny film");
        film.setReleaseDate(now);
        film.setDuration(120D);

        Film createFilm = filmStorage.create(film);

        User user = new User();
        user.setName("Dima");
        user.setEmail("blabla@yandex.ru");
        user.setLogin("Login");
        user.setBirthday(now);
        User userCreate = userStorage.create(user);

        likeStorage.create(userCreate.getId(), createFilm.getId());

        Film film1 = new Film();
        film1.setName("bobriki");
        film1.setGenres(genres);
        film1.setMpa(new MPA());
        film1.setDescription("very good film");
        film1.setReleaseDate(now);
        film1.setDuration(120D);

        Film createFilm1 = filmStorage.create(film1);

        User user1 = new User();
        user1.setName("Vova");
        user1.setEmail("Vova@yandex.ru");
        user1.setLogin("LoginVova");
        user1.setBirthday(now);
        User userCreate1 = userStorage.create(user1);

        likeStorage.create(userCreate.getId(), createFilm1.getId());
        likeStorage.create(userCreate1.getId(), createFilm1.getId());

        System.out.println("TEST " + createFilm1.getGenres());
        System.out.println("TEST " + createFilm.getGenres());

        List<Film> filmPopularList = filmStorage.findPopular(2, optGenre.get().getId(), now);
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
}