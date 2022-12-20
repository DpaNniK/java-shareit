package ru.practicum.shareit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;


@SpringBootTest
class ItemServiceTest {

    @Autowired
    ItemService itemService;
    @Autowired
    UserService userService;

    @AfterEach
    void updateRepository() {
        itemService.deleteAllItem();
        userService.deleteAllUser();
    }

    @Test
    public void testCreateItems() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));

        Item item = itemService.createItem(user.getId()
                , new ItemDto("Дрель", "Новая дрель", true, null));
        assertEquals(item.getId(), 1, "Неверно присвоен id добавленной вещи");
        assertEquals(item.getName(), "Дрель", "Неверно присвоено имя добавленной вещи");
        assertEquals(item.getOwner(), user, "Неверно присвоен владелец добавленной вещи");
        assertEquals(item.getDescription(), "Новая дрель", "Неверно присвоено описание добавленной вещи");
    }

    @Test
    public void testUpdateItems() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        itemService.createItem(user.getId()
                , new ItemDto("Дрель", "Новая дрель", true, null));
        Item itemUpdate = itemService.updateItem(1, user.getId()
                , new ItemDto("Пила", "Старая пила", true, null));
        assertEquals(itemUpdate.getId(), 1, "Неверно присвоен id добавленной вещи");
        assertEquals(itemUpdate.getName(), "Пила", "Неверно присвоено имя добавленной вещи");
        assertEquals(itemUpdate.getOwner(), user, "Неверно присвоен владелец добавленной вещи");
        assertEquals(itemUpdate.getDescription(), "Старая пила", "Неверно присвоено описание добавленной вещи");
    }

    @Test
    public void testSearchByText() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        Item item = itemService.createItem(user.getId()
                , new ItemDto("Дрель", "Новая дрель", true, null));
        Collection<Item> items = itemService.searchItemByText(1, "дРеЛь");
        assertEquals(items.size(), 1, "Неверный поиск по слову");
        assertTrue(items.contains(item), "Найден неверный предмет");
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

    private Executable generateExecutableForNotFoundUser() {
        return () -> itemService.createItem(1, new ItemDto("Дрель", "Новая дрель"
                , true, null));
    }

    private Executable generateExecutableForNotFoundItem() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        return () -> itemService.getItemById(user.getId(), 1);
    }
}
