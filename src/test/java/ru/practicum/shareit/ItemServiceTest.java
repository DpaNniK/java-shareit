package ru.practicum.shareit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@SpringBootTest
class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @AfterEach
    void tearDown() {
        itemService.deleteAllComments();
        bookingService.deleteAllBooking();
        itemService.deleteAllItems();
        userService.deleteAllUser();
    }

    @Test
    public void testCreateItems() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(user);

        Item newItem = itemService.createItem(user.getId(), itemDto);

        Optional<Item> itemOptional = Optional.ofNullable(newItem);

        assertThat(itemOptional)
                .isPresent()
                .hasValueSatisfying(item ->
                        assertThat(item).hasFieldOrPropertyWithValue("id", newItem.getId())
                                .hasFieldOrPropertyWithValue("description", itemDto.getDescription())
                                .hasFieldOrPropertyWithValue("name", itemDto.getName())
                                .hasFieldOrPropertyWithValue("available", itemDto.getAvailable())
                                .hasFieldOrPropertyWithValue("ownerId", itemDto.getOwner().getId())
                );
    }

    @Test
    public void testUpdateItems() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(user);
        Item oldItem = itemService.createItem(user.getId(), itemDto);
        itemDto.setName("newName");

        Optional<Item> itemOptional = Optional.ofNullable(itemService.updateItem(oldItem.getId()
                , user.getId(), itemDto));

        assertThat(itemOptional)
                .isPresent()
                .hasValueSatisfying(item ->
                        assertThat(item).hasFieldOrPropertyWithValue("id", oldItem.getId())
                                .hasFieldOrPropertyWithValue("description", itemDto.getDescription())
                                .hasFieldOrPropertyWithValue("name", itemDto.getName())
                                .hasFieldOrPropertyWithValue("available", itemDto.getAvailable())
                                .hasFieldOrPropertyWithValue("ownerId", itemDto.getOwner().getId())
                );
    }

    @Test
    public void testSearchByText() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(user);
        itemService.createItem(user.getId(), itemDto);

        Collection<Item> items = itemService.searchItemByText(user.getId(), "nam");
        assertEquals(items.size(), 1, "Неверный поиск по слову");
    }

    @Test
    public void createComment() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        ItemDto itemDto = createItemDto(user);
        Item item = itemService.createItem(user.getId(), itemDto);
        Booking booking = createBooking(item, user);
        bookingRepository.save(booking);
        CommentResponseDto commentResponseDto = itemService
                .createComment(user.getId(), item.getId(), "супер-класс");

        Optional<CommentResponseDto> itemOptional = Optional.ofNullable(commentResponseDto);

        assertThat(itemOptional)
                .isPresent()
                .hasValueSatisfying(commentResponse ->
                        assertThat(commentResponse).hasFieldOrPropertyWithValue("id", commentResponseDto.getId())
                                .hasFieldOrPropertyWithValue("text", "супер-класс")
                                .hasFieldOrPropertyWithValue("authorName", user.getName())
                );
    }

    @Test
    public void getRequestErrorCreateItemNotUser() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                generateExecutableForNotFoundUser()
        );
        assertEquals(HttpStatus.NOT_FOUND, er.getStatus());
    }

    @Test
    public void getRequestErrorGetNotFoundItem() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                generateExecutableForNotFoundItem()
        );
        assertEquals(HttpStatus.NOT_FOUND, er.getStatus());
    }

    @Test
    public void getRequestErrorCreateEmptyComment() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                generateExecutableForCreateEmptyComment()
        );
        assertEquals(HttpStatus.BAD_REQUEST, er.getStatus());
    }

    @Test
    public void getRequestErrorForCreateCommentNotFoundUser() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                generateExecutableForCreateCommentNotFoundItem()
        );
        assertEquals(HttpStatus.BAD_REQUEST, er.getStatus());
    }

    private Executable generateExecutableForNotFoundItem() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        return () -> itemService.getItemById(user.getId(), 1);
    }

    private Executable generateExecutableForNotFoundUser() {
        return () -> itemService.createItem(100, createItemDto(new User()));
    }

    private Executable generateExecutableForCreateEmptyComment() {
        return () -> {
            User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
            ItemDto itemDto = createItemDto(user);
            Item item = itemService.createItem(user.getId(), itemDto);
            Booking booking = createBooking(item, user);
            bookingRepository.save(booking);
            itemService.createComment(user.getId(), item.getId(), "");
        };
    }

    private Executable generateExecutableForCreateCommentNotFoundItem() {
        return () -> itemService.createComment(10, 10, "comment");
    }

    private ItemDto createItemDto(User user) {
        ItemDto itemDto = new ItemDto();
        itemDto.setAvailable(true);
        itemDto.setName("name");
        itemDto.setDescription("desc");
        itemDto.setOwner(user);
        return itemDto;
    }

    private Booking createBooking(Item item, User user) {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2023, 1, 5, 15, 15, 15));
        booking.setEnd(LocalDateTime.of(2023, 1, 6, 15, 15, 15));
        booking.setItemId(item.getId());
        booking.setBookerId(user.getId());
        booking.setStatus(Status.APPROVED);
        return booking;
    }
}