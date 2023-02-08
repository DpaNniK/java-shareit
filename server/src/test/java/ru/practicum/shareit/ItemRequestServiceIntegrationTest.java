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
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ItemRequestServiceIntegrationTest {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "requests", "bookings", "comments", "items", "users");
    }

    @Test
    public void createItemRequestTest() {
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        ItemDto itemDto = createItemDto(user);
        itemService.createItem(user.getId(), itemDto);
        ItemRequest request = new ItemRequest();
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        request.setDescription("desc");

        RequestDto requestDtoCreated = itemRequestService.createItemRequest(user.getId(), request);
        Optional<RequestDto> requestDtoOptional = Optional.ofNullable(requestDtoCreated);

        assertThat(requestDtoOptional).isPresent()
                .hasValueSatisfying(requestDto -> assertThat(requestDto)
                        .hasFieldOrPropertyWithValue("description", request.getDescription()));

        assertEquals(requestDtoCreated.getCreated().truncatedTo(ChronoUnit.SECONDS),
                requestDtoCreated.getCreated().truncatedTo(ChronoUnit.SECONDS),
                "Неверно присвоено время старта");
    }

    @Test
    public void getItemRequestForUserTest() {
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        ItemDto itemDto = createItemDto(user);
        itemService.createItem(user.getId(), itemDto);
        ItemRequest request = new ItemRequest();
        request.setDescription("desc");
        itemRequestService.createItemRequest(user.getId(), request);

        Collection<RequestDto> requestDtos = itemRequestService.getItemRequestForUser(user.getId());
        assertEquals(requestDtos.size(), 1);
    }

    @Test
    public void getAllRequestItemsTest() {
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        ItemDto itemDto = createItemDto(user);
        itemService.createItem(user.getId(), itemDto);
        ItemRequest request = new ItemRequest();
        request.setDescription("desc");
        RequestDto requestDtoCreated = itemRequestService.createItemRequest(user.getId(), request);

        Collection<RequestDto> requestDtoWithPagination = itemRequestService
                .getAllRequestItems();

        assertEquals(requestDtoWithPagination.size(), 1, "Неверное значение списка пагинации");
        assertEquals(new ArrayList<>(requestDtoWithPagination).get(0).getId(), requestDtoCreated.getId(),
                "Неверное значение id");
        assertEquals(new ArrayList<>(requestDtoWithPagination).get(0).getItems(),
                requestDtoCreated.getItems(), "Неверно присвоен список вещей");
        assertEquals(new ArrayList<>(requestDtoWithPagination).get(0).getDescription(),
                requestDtoCreated.getDescription(), "Неверно присвоено описание");
    }

    @Test
    public void getRequestByIdTest() {
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        ItemDto itemDto = createItemDto(user);
        itemService.createItem(user.getId(), itemDto);
        ItemRequest request = new ItemRequest();
        request.setDescription("desc");
        RequestDto requestDtoCreated = itemRequestService.createItemRequest(user.getId(), request);
        RequestDto resultRequestDto = itemRequestService.getRequestById(request.getId(), user.getId());

        assertEquals(resultRequestDto.getId(), requestDtoCreated.getId(),
                "Неверное значение id");
        assertEquals(resultRequestDto.getItems(), requestDtoCreated.getItems(),
                "Неверно присвоен список вещей");
        assertEquals(resultRequestDto.getDescription(),
                requestDtoCreated.getDescription(), "Неверно присвоено описание");
    }

    @Test
    public void get404NotFoundErrorForRequest() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                getErrorForNotFoundRequest()
        );
        assertEquals(HttpStatus.NOT_FOUND, er.getStatus());
    }

    @Test
    public void getItemsWithPaginationTest() {
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        User userTwo = userService.createUser(createUserDto("илья", "mail@yandex.ru"));
        ItemDto itemDto = createItemDto(user);
        ItemDto itemDtoTwo = createItemDto(userTwo);
        itemService.createItem(user.getId(), itemDto);
        itemService.createItem(userTwo.getId(), itemDtoTwo);
        ItemRequest request = new ItemRequest();
        ItemRequest requestTwo = new ItemRequest();
        request.setDescription("desc");
        requestTwo.setDescription("descTwo");
        itemRequestService.createItemRequest(user.getId(), request);
        itemRequestService.createItemRequest(userTwo.getId(), requestTwo);

        Collection<RequestDto> requestDtoWithPagination = itemRequestService
                .getItemsWithPagination(user.getId(), 0, 10);

        assertEquals(requestDtoWithPagination.size(), 1, "Неверное значение списка пагинации");
    }

    private Executable getErrorForNotFoundRequest() {
        User user = userService.createUser(createUserDto("иван", "yand@yandex.ru"));
        return () -> itemRequestService.getRequestById(1, user.getId());
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
