package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private Integer id = 1;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    //В методе getUserById уже есть проверка на то, что пользователь существует
    @Override
    public Item createItem(Integer userId, ItemDto itemDto) {
        itemDto.setId(id);
        User user = userService.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        id++;
        log.info("Добавлен новый предмет с ID = {}", item.getId());
        return itemRepository.createItem(userId, item);
    }

    @Override
    public Item updateItem(Integer itemId, Integer userId, ItemDto itemDto) {
        itemDto.setId(itemId);
        //Проверяю, что пользователь - владелец вещи
        checkUserIsOwner(itemId, userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userService.getUserById(userId));
        if (itemDto.getName() == null && itemDto.getDescription() == null) {
            log.info("Пользователь {} обновил статус предмета {}", userId, itemId);
            return itemRepository.updateStatusItem(itemId, userId, item);
        }
        if (itemDto.getAvailable() == null && itemDto.getDescription() == null) {
            log.info("Пользователь {} обновил название предмета {}", userId, itemId);
            return itemRepository.updateNameItem(itemId, userId, item);
        }
        if (itemDto.getAvailable() == null && itemDto.getName() == null) {
            log.info("Пользователь {} обновил описание о предмете {}", userId, itemId);
            return itemRepository.updateDescriptionItem(itemId, userId, item);
        }
        log.info("Пользователь {} обновил информацию о предмете {}", userId, itemId);
        return itemRepository.updateItem(itemId, userId, item);
    }

    @Override
    public Item getItemById(Integer userId, Integer itemId) {
        if (itemRepository.getItemsMap().get(itemId) == null) {
            log.warn("Пользователь {} запросил информацию о несуществующем предмете {}", userId, itemId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Предмет не найден");
        }
        log.info("Пользователь {} посмотрел информацию о предмете {}", userId, itemId);
        return itemRepository.getItemById(userId, itemId);
    }

    @Override
    public Collection<Item> getAllItemsOwner(Integer userId) {
        User user = userService.getUserById(userId);
        log.info("Получен запрос на вывод всего списка вещей пользователя {}", user);
        return itemRepository.getAllItemsOwner(userId);
    }

    @Override
    public Collection<Item> searchItemByText(Integer userId, String text) {
        User user = userService.getUserById(userId);
        log.info("Получен запрос на поиск {} от пользователя {}", text, user);
        return itemRepository.searchItemByText(userId, text.toLowerCase());
    }

    @Override
    public void deleteAllItem() {
        id = 1;
        itemRepository.deleteAllItem();
    }

    private void checkUserIsOwner(Integer itemId, Integer userId) {
        Item resultItem = itemRepository.getItemsMap().get(itemId);
        if (!Objects.equals(resultItem.getOwner().getId(), userId)) {
            log.warn("Невозможно обновить информацию о предмете. Пользователь {} не является владельцем вещи {}"
                    , userId, itemId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Пользователь не является владельцем вещи");
        }
    }
}
