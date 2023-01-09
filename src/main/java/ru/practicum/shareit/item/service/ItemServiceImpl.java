package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private Integer id = 1;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserService userService, BookingRepository bookingRepository,
                           UserRepository userRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Item createItem(Integer userId, ItemDto itemDto) {
        itemDto.setId(id);
        User user = userService.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(user.getId());
        id++;
        log.info("Добавлен новый предмет с ID = {}", item.getId());
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Integer itemId, Integer userId, ItemDto itemDto) {
        itemDto.setId(itemId);
        //Проверяю, что пользователь - владелец вещи
        checkUserIsOwner(itemId, userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(userId);
        if (itemDto.getName() == null && itemDto.getDescription() == null) {
            log.info("Пользователь {} обновил статус предмета {}", userId, itemId);
            ItemDto updateItem = getItemById(itemId, userId);
            updateItem.setAvailable(itemDto.getAvailable());
            return itemRepository.save(ItemMapper.toItem(updateItem));
        }
        if (itemDto.getAvailable() == null && itemDto.getDescription() == null) {
            log.info("Пользователь {} обновил название предмета {}", userId, itemId);
            ItemDto updateItem = getItemById(itemId, userId);
            updateItem.setName(itemDto.getName());
            return itemRepository.save(ItemMapper.toItem(updateItem));
        }
        if (itemDto.getAvailable() == null && itemDto.getName() == null) {
            log.info("Пользователь {} обновил описание о предмете {}", userId, itemId);
            ItemDto updateItem = getItemById(itemId, userId);
            updateItem.setDescription(itemDto.getDescription());
            return itemRepository.save(ItemMapper.toItem(updateItem));
        }
        log.info("Пользователь {} обновил информацию о предмете {}", userId, itemId);
        return itemRepository.save(item);
    }

    @Override
    public ItemDto getItemById(Integer itemId, Integer userId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            log.warn("Пользователь запросил информацию о несуществующем предмете {}", itemId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Предмет не найден");
        }
        log.info("Пользователь посмотрел информацию о предмете {}", itemId);
        User user = userService.getUserById(item.getOwnerId());
        if (Objects.equals(item.getOwnerId(), userId)) {
            return ItemMapper.toItemDto(item, user
                    , bookingRepository.getBookingsByItemIdOrderByStartAsc(itemId), getCommentList(itemId));
        }
        return ItemMapper.toItemDto(item, user, new ArrayList<>(), getCommentList(itemId));
    }

    @Override
    public Collection<ItemDto> getAllItemsOwner(Integer userId) {
        User user = userService.getUserById(userId);
        Collection<ItemDto> itemDtoCollections = new ArrayList<>();
        log.info("Получен запрос на вывод всего списка вещей пользователя {}", user);
        Collection<Item> items = itemRepository.findItemsByOwnerId(userId);
        items.forEach(item -> itemDtoCollections.add(getItemById(item.getId(), user.getId())));
        return getSortedItemsList(itemDtoCollections, userId);
    }

    @Override
    public Collection<Item> searchItemByText(Integer userId, String text) {
        User user = userService.getUserById(userId);
        log.info("Получен запрос на поиск {} от пользователя {}", text, user);
        if (text.isEmpty()) return new ArrayList<>();
        return itemRepository.searchByText(text, true);
    }

    @Override
    public void changeItemStatus(Integer itemId, Integer userId, boolean approved) {
        ItemDto item = getItemById(itemId, userId);
        item.setAvailable(approved);
        log.info("Статус {} изменен на {}", item, approved);
        itemRepository.save(ItemMapper.toItem(item));
    }

    @Override
    public CommentResponseDto createComment(Integer userId, Integer itemId, String text) {
        if (text.isEmpty()) {
            log.warn("Пользователь {} пытался оставить пустой комментарий {}", userId, itemId);
            throw new RequestError(HttpStatus.BAD_REQUEST, "Пустой комментарий");
        }
        Item item = itemRepository.findById(itemId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if (item == null) {
            log.warn("Пользователь {} пытался оставить комментарий ненайденной вещи {}", user, itemId);
            throw new RequestError(HttpStatus.BAD_REQUEST, "Вещь не найдена");
        }
        if (user == null) {
            log.warn("Ошибка при добавлении комментария, пользователь {} не найден", userId);
            throw new RequestError(HttpStatus.BAD_REQUEST, "Вещь не найдена");
        }
        if (!checkUserIsBookerForItem(userId, itemId)) {
            log.warn("Ошибка при добавлении комментария, пользователь {} не брал в аренду вещь {}", user, item);
            throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь не брал вещь в аренду");
        }
        if (!checkCreatedCommentAfterBooking(userId, itemId)) {
            log.warn("Ошибка при добавлении комментария, пользователь {} не закончил аренду {}", user, item);
            throw new RequestError(HttpStatus.BAD_REQUEST, "Вещь все еще находится в аренде у пользователя");
        }
        Comment comment = saveNewComment(itemId, text, userId);
        CommentDto commentDto = CommentMapper.commentDto(comment, user, item);
        return CommentMapper.commentDtoToResponseDto(commentDto);
    }

    @Override
    public Collection<CommentResponseDto> getCommentList(Integer itemId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        Collection<CommentResponseDto> commentResponseList = new ArrayList<>();
        Collection<Comment> comments = commentRepository.getCommentsByItemId(itemId);
        for (Comment comment : comments) {
            User author = userRepository.findById(comment.getAuthorId()).orElse(null);
            if (author != null) {
                CommentDto commentDto = CommentMapper.commentDto(comment, author, item);
                commentResponseList.add(CommentMapper.commentDtoToResponseDto(commentDto));
            }
        }
        return commentResponseList;
    }

    private void checkUserIsOwner(Integer itemId, Integer userId) {
        Item resultItem = itemRepository.findById(itemId).orElse(null);
        if (resultItem == null) {
            log.warn("Невозможно обновить информацию о предмете. Вещь с id = {} не найдена"
                    , itemId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Вещь не найдена");
        }
        if (!Objects.equals(resultItem.getOwnerId(), userId)) {
            log.warn("Невозможно обновить информацию о предмете. Пользователь {} не является владельцем вещи {}"
                    , userId, itemId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Пользователь не является владельцем вещи");
        }
    }

    private Collection<ItemDto> getSortedItemsList(Collection<ItemDto> itemDtoCollection, Integer userId) {
        Comparator<Booking> bookingComparator = Comparator.comparing(Booking::getStart).reversed();
        TreeSet<Booking> bookingSortedSet = new TreeSet<>(bookingComparator);
        Collection<ItemDto> itemSortedCollection = new ArrayList<>();
        Collection<ItemDto> itemWithEmptyStart = new ArrayList<>();

        itemDtoCollection.forEach(itemDto -> {
            Booking booking = itemDto.getLastBooking();
            if (booking != null) {
                bookingSortedSet.add(booking);
            } else {
                itemWithEmptyStart.add(itemDto);
            }
        });

        bookingSortedSet.forEach(booking -> {
            ItemDto itemDto = getItemById(booking.getItemId(), userId);
            if (itemDto != null) itemSortedCollection.add(itemDto);
        });

        if (itemSortedCollection.size() == 0) return itemDtoCollection;
        itemSortedCollection.addAll(itemWithEmptyStart);
        return itemSortedCollection;
    }

    private boolean checkUserIsBookerForItem(Integer userId, Integer itemId) {
        Collection<Booking> bookingList = bookingRepository.getBookingsByItemIdOrderByStartAsc(itemId);
        for (Booking booking : bookingList) {
            if (Objects.equals(booking.getBookerId(), userId)) return true;
        }
        return false;
    }

    private boolean checkCreatedCommentAfterBooking(Integer userId, Integer itemId) {
        LocalDateTime createdTime = LocalDateTime.now();
        Collection<Booking> bookings = bookingRepository.getBookingsByBookerIdAndItemId(userId, itemId);
        for (Booking booking : bookings) {
            if (booking.getEnd().isBefore(createdTime)) return true;
        }
        return false;
    }

    private Comment saveNewComment(Integer itemId, String text, Integer authorId) {
        Comment comment = new Comment();
        comment.setItemId(itemId);
        comment.setText(text);
        comment.setAuthorId(authorId);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}
