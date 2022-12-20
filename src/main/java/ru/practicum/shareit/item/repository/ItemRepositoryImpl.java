package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;


@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {

    HashMap<Integer, Item> itemHashMap = new HashMap<>();
    private final UserRepository userRepository;

    @Autowired
    public ItemRepositoryImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Item createItem(Integer userId, Item item) {
        User user = userRepository.getUserById(userId);
        item.setOwner(user);
        log.info("Добавлен новый предмет с ID = {}", item.getId());
        itemHashMap.put(item.getId(), item);
        return itemHashMap.get(item.getId());
    }

    @Override
    public Item updateItem(Integer itemId, Integer userId, Item item) {
        checkUserIsOwner(itemId, userId);
        item.setOwner(userRepository.getUserById(userId));
        log.info("Пользователь {} обновил информацию о предмете {}", userId, itemId);
        itemHashMap.put(itemId, item);
        return itemHashMap.get(item.getId());
    }

    @Override
    public Item updateNameItem(Integer itemId, Integer userId, Item item) {
        checkUserIsOwner(itemId, userId);
        log.info("Пользователь {} обновил название предмета {}", userId, itemId);
        itemHashMap.get(itemId).setName(item.getName());
        return itemHashMap.get(itemId);
    }

    @Override
    public Item updateDescriptionItem(Integer itemId, Integer userId, Item item) {
        checkUserIsOwner(itemId, userId);
        log.info("Пользователь {} обновил описание о предмете {}", userId, itemId);
        itemHashMap.get(itemId).setDescription(item.getDescription());
        return itemHashMap.get(itemId);
    }

    @Override
    public Item updateStatusItem(Integer itemId, Integer userId, Item item) {
        checkUserIsOwner(itemId, userId);
        log.info("Пользователь {} обновил статус предмета {}", userId, itemId);
        itemHashMap.get(itemId).setAvailable(item.isAvailable());
        return itemHashMap.get(itemId);
    }

    @Override
    public Item getItemById(Integer userId, Integer itemId) {
        if (itemHashMap.get(itemId) == null) {
            log.warn("Пользователь {} запросил информацию о несуществующем предмете {}", userId, itemId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Предмет не найден");
        }
        log.info("Пользователь {} посмотрел информацию о предмете {}", userId, itemId);
        return itemHashMap.get(itemId);
    }

    @Override
    public Collection<Item> getAllItemsOwner(Integer userId) {
        User user = userRepository.getUserById(userId);
        Collection<Item> itemsOwner = new ArrayList<>();
        log.info("Получен запрос на вывод всего списка вещей пользователя {}", user);
        for (Item item : itemHashMap.values()) {
            if (Objects.equals(item.getOwner().getId(), userId)) itemsOwner.add(item);
        }
        return itemsOwner;
    }

    @Override
    public Collection<Item> searchItemByText(Integer userId, String text) {
        User user = userRepository.getUserById(userId);
        Collection<Item> itemsOwner = new ArrayList<>();
        log.info("Получен запрос на поиск {} от пользователя {}", text, user);
        if (text.isEmpty()) return new ArrayList<>();

        for (Item item : itemHashMap.values()) {
            if (item.getName().toLowerCase().contains(text) && item.isAvailable()) {
                log.info("Найдено совпадение по запросу пользователя - {} в названии предмета {}", text, item);
                itemsOwner.add(item);
            } else {
                if (item.getDescription().toLowerCase().contains(text) && item.isAvailable()) {
                    log.info("Найдено совпадение по запросу пользователя - {} в описании предмета {}", text, item);
                    itemsOwner.add(item);
                }
            }
        }
        return itemsOwner;
    }

    @Override
    public void deleteAllItem() {
        itemHashMap.clear();
    }

    private void checkUserIsOwner(Integer itemId, Integer userId) {
        Item resultItem = itemHashMap.get(itemId);
        if (!Objects.equals(resultItem.getOwner().getId(), userId)) {
            log.warn("Невозможно обновить информацию о предмете. Пользователь {} не является владельцем вещи {}"
                    , userId, itemId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Пользователь не является владельцем вещи");
        }
    }
}
