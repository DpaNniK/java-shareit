package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    //Проверка на существование юзера происходит в getUserById
    @Override
    public RequestDto createItemRequest(Integer userId, ItemRequest request) {
        User creator = userService.getUserById(userId);
        request.setRequester(creator);
        log.info("Создан новый запрос на вещь от пользователя {}", creator);
        ItemRequest itemRequest = itemRequestRepository.save(request);
        return RequestMapper.toRequestDto(itemRequest, new ArrayList<>());
    }

    @Override
    public Collection<RequestDto> getItemRequestForUser(Integer userId) {
        User creator = userService.getUserById(userId);
        log.info("Пользователь {} просмотрел список своих запросов", creator);
        Collection<ItemRequest> itemRequests = itemRequestRepository
                .getDistinctByRequesterOrderByCreatedDesc(creator);
        return getItemsRequest(itemRequests);
    }

    //Пагинация для вещей Пользователя
    @Override
    public Collection<RequestDto> getItemsWithPagination(Integer userId, Integer from, Integer size) {
        User user = userService.getUserById(userId);
        if (size <= 0 || from < 0 || size < from) {
            log.warn("Пользователь ввел неправильные границы для пагинации");
            throw new RequestError(HttpStatus.BAD_REQUEST, "Неверные границы пагинации");
        }
        from = from / size;
        log.info("Пользователь {} просматривает список запросов вещей", user);
        Page<ItemRequest> itemRequests = itemRequestRepository
                .getDistinctByRequesterNotContainingOrderByCreatedDesc(user, PageRequest.of(from, size));
        return getItemsRequest(itemRequests.stream().collect(Collectors.toList()));
    }

    //Метод будет вызван, если не будут переданы параметры from и size
    @Override
    public Collection<RequestDto> getAllRequestItems() {
        log.info("Просмотрен список всех запросов");
        Collection<ItemRequest> allRequests = itemRequestRepository
                .findAll(Sort.by("created").descending());
        return getItemsRequest(allRequests);
    }

    @Override
    public RequestDto getRequestById(Integer requestId, Integer userId) {
        User user = userService.getUserById(userId);
        ItemRequest request = itemRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            log.warn("Пользователь {} запрос несуществующий запрос под id {}", user, requestId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Запрос под таким ID не найден");
        }
        Collection<Item> itemRequestCollection = itemRepository.getItemsByRequestId(requestId);
        return RequestMapper.toRequestDto(request, itemRequestCollection);
    }

    //Здесь каждому запросу request присваиваю коллекцию ответов. Метод универсальный,
    //зависит лишь от входящего списка, который формируется в зависимости от запроса к бд
    private Collection<RequestDto> getItemsRequest(Collection<ItemRequest> itemRequests) {
        Collection<RequestDto> itemRequestDtos = new ArrayList<>();
        Collection<Integer> requestIds = itemRequests.stream().map((ItemRequest::getId)).collect(Collectors.toList());
        Map<Integer, Collection<Item>> requestMapWithAnswer = itemRepository
                .getItemsByRequestIds(requestIds).stream().collect(Collectors.toMap(Item::getRequestId, List::of));
        itemRequests.forEach(request -> {
                    RequestDto requestDto = RequestMapper
                            .toRequestDto(request, requestMapWithAnswer.get(request.getId()));
                    itemRequestDtos.add(requestDto);
                }
        );
        return itemRequestDtos;
    }
}
