package ru.practicum.shareit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingServiceTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bookings", "comments", "items", "users");
    }

    @Test
    public void testCreateBooking() {
        User owner = userService.createUser(new UserDto("Owner", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(new UserDto("Игорь", "yandex@mail.ru"));
        Booking booking = createBooking(item, user);

        BookingDto bookingDtoCreated = bookingService.createBooking(booking);
        Optional<BookingDto> bookingOptional = Optional.ofNullable(bookingDtoCreated);

        assertThat(bookingOptional)
                .isPresent()
                .hasValueSatisfying(bookingDto ->
                        assertThat(bookingDto).hasFieldOrPropertyWithValue("item", item)
                                .hasFieldOrPropertyWithValue("booker", user)
                                .hasFieldOrPropertyWithValue("status", booking.getStatus())
                );
        assertEquals(bookingDtoCreated.getStart().truncatedTo(ChronoUnit.SECONDS)
                , booking.getStart().truncatedTo(ChronoUnit.SECONDS)
                , "Неверно присвоено время старта");
        assertEquals(bookingDtoCreated.getEnd().truncatedTo(ChronoUnit.SECONDS)
                , booking.getEnd().truncatedTo(ChronoUnit.SECONDS)
                , "Неверно присвоено время завершения");
    }

    @Test
    public void testReplyToBookingTrue() {
        User owner = userService.createUser(new UserDto("Owner", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(new UserDto("Игорь", "yandex@mail.ru"));
        Booking booking = createBooking(item, user);

        BookingDto bookingDto = bookingService.createBooking(booking);
        Optional<BookingDto> bookingOptional = Optional
                .ofNullable(bookingService.replyToBooking(owner.getId(), bookingDto.getId(), true));

        assertThat(bookingOptional)
                .isPresent()
                .hasValueSatisfying(bookingDto1 ->
                        assertThat(bookingDto1).hasFieldOrPropertyWithValue("status"
                                , Status.APPROVED)
                );
    }

    @Test
    public void testReplyToBookingFalse() {
        User owner = userService.createUser(new UserDto("Owner", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(new UserDto("Игорь", "yandex@mail.ru"));
        Booking booking = createBooking(item, user);

        BookingDto bookingDto = bookingService.createBooking(booking);
        Optional<BookingDto> bookingOptional = Optional
                .ofNullable(bookingService.replyToBooking(owner.getId(), bookingDto.getId(), false));

        assertThat(bookingOptional)
                .isPresent()
                .hasValueSatisfying(bookingDto1 ->
                        assertThat(bookingDto1).hasFieldOrPropertyWithValue("status"
                                , Status.REJECTED)
                );
    }

    @Test
    public void get400BadRequestIncorrectData() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorForIncorrectDataBooking()
        );
        assertEquals(HttpStatus.BAD_REQUEST, er.getStatus());
    }

    @Test
    public void get404NotFoundRequestBookerIsOwner() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorForBookerIsOwner()
        );
        assertEquals(HttpStatus.NOT_FOUND, er.getStatus());
    }

    @Test
    public void get404NotFoundRequestUserNotOwnerItem() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorForUserNotOwnerItem()
        );
        assertEquals(HttpStatus.NOT_FOUND, er.getStatus());
    }

    @Test
    public void get404NotFoundGetBookingForNotFoundItem() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorGetBookingForNotFoundItem()
        );
        assertEquals(HttpStatus.NOT_FOUND, er.getStatus());
    }

    private Executable getErrorForIncorrectDataBooking() {
        return () -> {
            User owner = userService.createUser(new UserDto("Owner", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            User user = userService.createUser(new UserDto("Игорь", "yandex@mail.ru"));
            Booking booking = createBooking(item, user);
            booking.setStart(LocalDateTime.now().minusDays(1));
            bookingService.createBooking(booking);
        };
    }

    private Executable getErrorForBookerIsOwner() {
        return () -> {
            User owner = userService.createUser(new UserDto("Owner", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            Booking booking = createBooking(item, owner);
            bookingService.createBooking(booking);
        };
    }

    private Executable getErrorForUserNotOwnerItem() {
        return () -> {
            User owner = userService.createUser(new UserDto("Owner", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            User user = userService.createUser(new UserDto("Игорь", "yandex@mail.ru"));
            Booking booking = createBooking(item, user);

            BookingDto bookingDto = bookingService.createBooking(booking);
            bookingService.replyToBooking(user.getId(), bookingDto.getId(), false);
        };
    }

    private Executable getErrorGetBookingForNotFoundItem() {
        return () -> bookingService.getBookingById(1);
    }

    private Booking createBooking(Item item, User user) {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusMinutes(3));
        booking.setEnd(LocalDateTime.now().plusMinutes(30));
        booking.setItemId(item.getId());
        booking.setBookerId(user.getId());
        booking.setStatus(Status.APPROVED);
        return booking;
    }

    private ItemDto createItemDto(User user) {
        ItemDto itemDto = new ItemDto();
        itemDto.setAvailable(true);
        itemDto.setName("name");
        itemDto.setDescription("desc");
        itemDto.setOwner(user);
        return itemDto;
    }
}