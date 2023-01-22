package ru.practicum.shareit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.practicum.shareit.booking.converter.StringToEnumConverter;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
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
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingServiceIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingRepository bookingRepository;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bookings", "comments", "items", "users");
    }

    @Test
    public void testCreateBooking() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
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
        assertEquals(bookingDtoCreated.getStart().truncatedTo(ChronoUnit.SECONDS),
                booking.getStart().truncatedTo(ChronoUnit.SECONDS), "Неверно присвоено время старта");
        assertEquals(bookingDtoCreated.getEnd().truncatedTo(ChronoUnit.SECONDS),
                booking.getEnd().truncatedTo(ChronoUnit.SECONDS), "Неверно присвоено время завершения");
    }

    @Test
    public void testReplyToBookingTrue() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);

        BookingDto bookingDto = bookingService.createBooking(booking);
        Optional<BookingDto> bookingOptional = Optional
                .ofNullable(bookingService.replyToBooking(owner.getId(), bookingDto.getId(), true));

        assertThat(bookingOptional)
                .isPresent()
                .hasValueSatisfying(bookingDto1 ->
                        assertThat(bookingDto1).hasFieldOrPropertyWithValue("status", Status.APPROVED)
                );
    }

    @Test
    public void testReplyToBookingFalse() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);

        BookingDto bookingDto = bookingService.createBooking(booking);
        Optional<BookingDto> bookingOptional = Optional
                .ofNullable(bookingService.replyToBooking(owner.getId(), bookingDto.getId(), false));

        assertThat(bookingOptional)
                .isPresent()
                .hasValueSatisfying(bookingDto1 ->
                        assertThat(bookingDto1).hasFieldOrPropertyWithValue("status", Status.REJECTED)
                );
    }

    @Test
    public void getBookingByIdForOwnerOrBooker() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        BookingDto bookingDtoCreated = bookingService.createBooking(booking);

        BookingDto bookingDtoResult = bookingService
                .getBookingByIdForOwnerOrBooker(bookingDtoCreated.getId(), user.getId());
        assertEquals(bookingDtoResult.getId(), bookingDtoCreated.getId(), "Неверный ID аренды");
        assertEquals(bookingDtoResult.getBooker(), bookingDtoCreated.getBooker(), "Неверный букер");
        assertEquals(bookingDtoResult.getItem(), bookingDtoCreated.getItem(), "Неверная вещь");
        assertEquals(bookingDtoResult.getStart(), bookingDtoCreated.getStart(), "Неверное время начала");
        assertEquals(bookingDtoResult.getEnd(), bookingDtoCreated.getEnd(), "Неверное время окончания");
        assertEquals(bookingDtoResult.getStatus(), bookingDtoCreated.getStatus(), "Неверный статус");
    }

    @Test
    public void getAllBookingsForUserStateAll() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService.getAllBookingsForUser(BookingState.ALL, user.getId());

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateFuture() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().plusDays(5));
        booking.setEnd(LocalDateTime.now().plusDays(6));
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService.getAllBookingsForUser(BookingState.FUTURE, user.getId());

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateWaiting() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStatus(Status.WAITING);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService.getAllBookingsForUser(BookingState.WAITING, user.getId());

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateRejected() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        BookingDto bookingDto = bookingService.createBooking(booking);
        bookingService.replyToBooking(owner.getId(), bookingDto.getId(), false);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUser(BookingState.REJECTED, user.getId());
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStatePast() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUser(BookingState.PAST, user.getId());
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateCurrent() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUser(BookingState.CURRENT, user.getId());
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    //
    @Test
    public void getAllBookingsForUserStateAllWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUserWithPagination(BookingState.ALL, user.getId(), 0, 1);

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateFutureWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().plusDays(5));
        booking.setEnd(LocalDateTime.now().plusDays(6));
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUserWithPagination(BookingState.FUTURE, user.getId(), 0, 1);

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateWaitingWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStatus(Status.WAITING);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUserWithPagination(BookingState.WAITING, user.getId(), 0, 1);

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateRejectedWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        BookingDto bookingDto = bookingService.createBooking(booking);
        bookingService.replyToBooking(owner.getId(), bookingDto.getId(), false);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUserWithPagination(BookingState.REJECTED, user.getId(), 0, 1);
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStatePastWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUserWithPagination(BookingState.PAST, user.getId(), 0, 1);
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForUserStateCurrentWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingsForUserWithPagination(BookingState.CURRENT, user.getId(), 0, 1);
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }
    //

    @Test
    public void getAllBookingForOwnerStateAll() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwner(BookingState.ALL, owner.getId());

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingForOwnerStateFuture() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().plusDays(5));
        booking.setEnd(LocalDateTime.now().plusDays(6));
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService.getAllBookingForOwner(BookingState.FUTURE, owner.getId());

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStateWaiting() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStatus(Status.WAITING);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwner(BookingState.WAITING, owner.getId());

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStateRejected() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        BookingDto bookingDto = bookingService.createBooking(booking);
        bookingService.replyToBooking(owner.getId(), bookingDto.getId(), false);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwner(BookingState.REJECTED, owner.getId());
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStatePast() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwner(BookingState.PAST, owner.getId());
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStateCurrent() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwner(BookingState.CURRENT, owner.getId());
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    //
    @Test
    public void getAllBookingForOwnerStateAllWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwnerWithPagination(BookingState.ALL, owner.getId(), 0, 1);

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingForOwnerStateFutureWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().plusDays(5));
        booking.setEnd(LocalDateTime.now().plusDays(6));
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwnerWithPagination(BookingState.FUTURE, owner.getId(), 0, 1);

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStateWaitingWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStatus(Status.WAITING);
        bookingService.createBooking(booking);
        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwnerWithPagination(BookingState.WAITING, owner.getId(), 0, 1);

        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStateRejectedWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        BookingDto bookingDto = bookingService.createBooking(booking);
        bookingService.replyToBooking(owner.getId(), bookingDto.getId(), false);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwnerWithPagination(BookingState.REJECTED, owner.getId(), 0, 1);
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStatePastWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwnerWithPagination(BookingState.PAST, owner.getId(), 0, 1);
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    @Test
    public void getAllBookingsForOwnerStateCurrentWithPagination() {
        User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(owner);
        Item item = itemService.createItem(owner.getId(), itemDto);
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        Booking booking = createBooking(item, user);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking);

        Collection<BookingDto> bookings = bookingService
                .getAllBookingForOwnerWithPagination(BookingState.CURRENT, owner.getId(), 0, 1);
        assertEquals(bookings.size(), 1, "Неверно получен список");
    }

    //
    @Test
    public void stringToEnumTest() {
        BookingState state = BookingState.ALL;
        StringToEnumConverter converter = new StringToEnumConverter();
        BookingState resultState = converter.convert("all");

        assertEquals(state, resultState, "Неверная работа конвертера");
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

    @Test
    public void get400BadRequestForReplyToBooking() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorForUnsupportedStatusReplyToBooking());
        assertEquals(HttpStatus.BAD_REQUEST, er.getStatus());
    }

    @Test
    public void get400BadRequestForCreateBooking() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorForIncorrectForFalseAvailable());
        assertEquals(HttpStatus.BAD_REQUEST, er.getStatus());
    }

    @Test
    public void get404NotFoundForGetBookingById() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorForGerBookingById());
        assertEquals(HttpStatus.NOT_FOUND, er.getStatus());
    }

    @Test
    public void get400BadRequestForPaginationUser() {
        RequestError error = Assertions.assertThrows(
                RequestError.class,
                getErrorBadRequestForPaginationUser());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    public void get400BadRequestForPaginationOwner() {
        RequestError error = Assertions.assertThrows(
                RequestError.class,
                getErrorBadRequestForPaginationOwner());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    private Executable getErrorForIncorrectDataBooking() {
        return () -> {
            User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
            Booking booking = createBooking(item, user);
            booking.setStart(LocalDateTime.now().minusDays(1));
            bookingService.createBooking(booking);
        };
    }

    private Executable getErrorForIncorrectForFalseAvailable() {
        return () -> {
            User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            itemDto.setAvailable(false);
            Item item = itemService.createItem(owner.getId(), itemDto);
            User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
            Booking booking = createBooking(item, user);
            booking.setStart(LocalDateTime.now().plusMinutes(10));
            bookingService.createBooking(booking);
        };
    }

    private Executable getErrorForBookerIsOwner() {
        return () -> {
            User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            Booking booking = createBooking(item, owner);
            bookingService.createBooking(booking);
        };
    }

    private Executable getErrorForUserNotOwnerItem() {
        return () -> {
            User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
            Booking booking = createBooking(item, user);

            BookingDto bookingDto = bookingService.createBooking(booking);
            bookingService.replyToBooking(user.getId(), bookingDto.getId(), false);
        };
    }

    private Executable getErrorGetBookingForNotFoundItem() {
        return () -> bookingService.getBookingById(1);
    }

    private Executable getErrorForUnsupportedStatusReplyToBooking() {
        return () -> {
            User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
            Booking booking = createBooking(item, user);
            BookingDto bookingDto = bookingService.createBooking(booking);

            bookingService.replyToBooking(owner.getId(), bookingDto.getId(), true);
            bookingService.replyToBooking(owner.getId(), bookingDto.getId(), true);
        };
    }

    private Executable getErrorForGerBookingById() {
        return () -> {
            User owner = userService.createUser(createUserDto("игорь", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(owner);
            Item item = itemService.createItem(owner.getId(), itemDto);
            User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
            Booking booking = createBooking(item, user);
            BookingDto bookingDtoCreated = bookingService.createBooking(booking);

            bookingService.getBookingByIdForOwnerOrBooker(bookingDtoCreated.getId(), 100);
        };
    }

    private Executable getErrorBadRequestForPaginationUser() {
        return () -> bookingService
                .getAllBookingsForUserWithPagination(BookingState.ALL, 1, -5, 0);
    }

    private Executable getErrorBadRequestForPaginationOwner() {
        return () -> bookingService
                .getAllBookingForOwnerWithPagination(BookingState.ALL, 1, -5, 0);
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

    private UserDto createUserDto(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setId(1);
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }
}