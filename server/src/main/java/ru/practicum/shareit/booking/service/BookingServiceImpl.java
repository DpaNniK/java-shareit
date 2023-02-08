package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    //Получаю пользователя и вещь при помощи вспомогательных сервисов.
    //Если пользователь или вещь в методе get будет не найдены, то ошибка обработается внутри
    //этих сервисов. Т.о. мне не нужно в BookingService делать проверки на сущ-е юзеров и вещей
    @Override
    public BookingDto createBooking(Booking booking) {
        User owner = userService.getUserById(booking.getBookerId());
        if (booking.getStart().isAfter(booking.getEnd()) || booking.getStart().isBefore(LocalDateTime.now())) {
            log.warn("Ошибка при аренде вещи {}, неверно указано время аренды", booking);
            throw new RequestError(HttpStatus.BAD_REQUEST, "Некорректно указано время аренды");
        }
        ItemDto item = itemService.getItemById(booking.getItemId(), owner.getId());
        if (!item.getAvailable()) {
            log.warn("Ошибка при аренде вещи {}, вещь не доступна для аренды", item);
            throw new RequestError(HttpStatus.BAD_REQUEST, "Вещь с ID =" + booking.getItemId() + " не доступна для аренды");
        }
        if (Objects.equals(item.getOwner().getId(), owner.getId())) {
            log.warn("Ошибка при аренде вещи {}, пользователь не может арендовать собственную вещь", item);
            throw new RequestError(HttpStatus.NOT_FOUND,
                    "Пользователь не может арендовать собственную вещь");
        }
        log.info("Создан запрос от пользователя {} на аренду вещи {}", owner, item);
        booking.setStatus(Status.WAITING);
        bookingRepository.save(booking);
        return getBookingById(booking.getId());
    }

    //Через getBookingById проверяю, что бронирование с нужным id существует
    @Override
    public BookingDto replyToBooking(Integer ownerId, Integer bookingId, boolean approved) {
        BookingDto bookingDto = getBookingById(bookingId);
        if (!Objects.equals(bookingDto.getItem().getOwnerId(), ownerId)) {
            log.warn("Невозможно принять/отменить аренду. Пользователь не является владельцем вещи");
            throw new RequestError(HttpStatus.NOT_FOUND, "Пользователь с ID = " + ownerId +
                    " не является владельцем вещи с ID = " + bookingDto.getItem().getId());
        }
        if (!Objects.equals(bookingDto.getStatus(), Status.WAITING)) {
            log.warn("Невозможно изменить статус аренды. Статус аренды {}", bookingDto.getStatus());
            throw new RequestError(HttpStatus.BAD_REQUEST,
                    "Невозможно изменить статус аренды. Аренда уже закрыта/отклонена/принята");
        }
        Booking booking = BookingMapper.toBooking(bookingDto);
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        log.info("Статус запроса {} изменен на {}", bookingDto, booking.getStatus());
        itemService.changeItemStatus(bookingDto.getItem().getId(), ownerId, true);
        bookingRepository.save(booking);
        return getBookingById(bookingId);
    }

    @Override
    public BookingDto getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            log.warn("Ошибка. Запрос аренды под id {} не найден", bookingId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Запрос на аренду с ID" + bookingId + " не найден");
        }
        User booker = userService.getUserById(booking.getBookerId());
        ItemDto item = itemService.getItemById(booking.getItemId(), booker.getId());
        log.info("Просмотрен запрос аренды с id = {}", bookingId);
        return BookingMapper.toBookingDto(booking, ItemMapper.toItem(item), booker);
    }

    @Override
    public BookingDto getBookingByIdForOwnerOrBooker(Integer bookingId, Integer userId) {
        BookingDto bookingDto = getBookingById(bookingId);
        if (!Objects.equals(bookingDto.getItem().getOwnerId(), userId)
                && !Objects.equals(bookingDto.getBooker().getId(), userId)) {
            log.warn("Ошибка. Запрос аренды под id {} не найден", bookingId);
            throw new RequestError(HttpStatus.NOT_FOUND, "Запрос на аренду с ID" + bookingId + " не найден");
        }
        log.info("Пользователь с id {} запросил информацию о запросе аренды с id {}",
                userId, bookingId);
        return getBookingById(bookingId);
    }

    @Override
    public Collection<BookingDto> getAllBookingsForUser(BookingState state, Integer userId) {
        Collection<BookingDto> userBooking = new ArrayList<>();
        //Проверка, что юзер существует
        User userSender = userService.getUserById(userId);
        switch (state) {
            case ALL:
                log.info("Пользователь {} запросил список всех арендованных предметов",
                        userSender);
                Collection<Booking> bookings = bookingRepository
                        .getBookingsByBookerIdOrderByStartDesc(userSender.getId());
                userBooking = setBookingsDtoForUser(bookings);
                break;
            case FUTURE:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.FUTURE);
                Collection<Booking> futureBookings = bookingRepository
                        .getFutureBookingsForUser(userSender.getId(), LocalDateTime.now());
                userBooking = setBookingsDtoForUser(futureBookings);
                break;
            case CURRENT:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.CURRENT);
                Collection<Booking> currentBookings = bookingRepository.getCurrentBookingForUser(userSender.getId());
                userBooking = setBookingsDtoForUser(currentBookings);
                break;
            case WAITING:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.WAITING);
                Collection<Booking> waitingBookings = bookingRepository
                        .getBookingsByBookerIdAndStatus(userSender.getId(), Status.WAITING);
                userBooking = setBookingsDtoForUser(waitingBookings);
                break;
            case REJECTED:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.REJECTED);
                Collection<Booking> rejectedBookings = bookingRepository
                        .getBookingsByBookerIdAndStatus(userSender.getId(), Status.REJECTED);
                userBooking = setBookingsDtoForUser(rejectedBookings);
                break;
            case PAST:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.PAST);
                Collection<Booking> pastBookings = bookingRepository
                        .getBookingsByBookerIdAndEndBefore(userSender.getId(), LocalDateTime.now());
                userBooking = setBookingsDtoForUser(pastBookings);
                break;
        }
        return userBooking;
    }

    @Override
    public Collection<BookingDto> getAllBookingsForUserWithPagination(BookingState state, Integer userId, Integer from,
                                                                      Integer size) {
        Collection<BookingDto> userBooking = new ArrayList<>();
        //Проверка, что юзер существует
        User userSender = userService.getUserById(userId);
        from = from / size;
        switch (state) {
            case ALL:
                log.info("Пользователь {} запросил список всех арендованных предметов",
                        userSender);
                Page<Booking> bookings = bookingRepository
                        .getBookingsByBookerIdOrderByStartDesc(userId, PageRequest.of(from, size));
                userBooking = setBookingsDtoForUser(bookings.toList());
                break;
            case FUTURE:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.FUTURE);
                Page<Booking> futureBookings = bookingRepository
                        .getFutureBookingsForUser(userSender.getId(), LocalDateTime.now(), PageRequest.of(from, size));
                userBooking = setBookingsDtoForUser(futureBookings.toList());
                break;
            case CURRENT:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.CURRENT);
                Page<Booking> currentBookings = bookingRepository
                        .getCurrentBookingForUser(userSender.getId(), PageRequest.of(from, size));
                userBooking = setBookingsDtoForUser(currentBookings.toList());
                break;
            case WAITING:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.WAITING);
                Page<Booking> waitingBookings = bookingRepository
                        .getBookingsByBookerIdAndStatus(userSender.getId(), Status.WAITING,
                                PageRequest.of(from, size));
                userBooking = setBookingsDtoForUser(waitingBookings.toList());
                break;
            case REJECTED:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.REJECTED);
                Page<Booking> rejectedBookings = bookingRepository
                        .getBookingsByBookerIdAndStatus(userSender.getId(), Status.REJECTED,
                                PageRequest.of(from, size));
                userBooking = setBookingsDtoForUser(rejectedBookings.toList());
                break;
            case PAST:
                log.info("Пользователь {} запросил список всех арендованных предметов со статусом {}",
                        userSender, BookingState.PAST);
                Page<Booking> pastBookings = bookingRepository
                        .getBookingsByBookerIdAndEndBefore(userSender.getId(), LocalDateTime.now(),
                                PageRequest.of(from, size));
                userBooking = setBookingsDtoForUser(pastBookings.toList());
                break;
        }
        return userBooking;
    }

    @Override
    public Collection<BookingDto> getAllBookingForOwner(BookingState state, Integer ownerId) {
        Collection<BookingDto> ownerBooking = new ArrayList<>();
        User owner = userService.getUserById(ownerId);
        switch (state) {
            case ALL:
                log.info("Владелец {} запросил список всех своих предметов", owner);
                Collection<Booking> bookings = bookingRepository
                        .getBookingsByOwnerId(ownerId);
                ownerBooking = setBookingsDtoForUser(bookings);
                break;
            case FUTURE:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.FUTURE);
                Collection<Booking> futureBookings = bookingRepository
                        .getFutureBookingForOwner(owner.getId());
                ownerBooking = setBookingsDtoForUser(futureBookings);
                break;
            case CURRENT:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.FUTURE);
                Collection<Booking> currentBookings = bookingRepository
                        .getCurrentBookingForOwner(owner.getId());
                ownerBooking = setBookingsDtoForUser(currentBookings);
                break;
            case WAITING:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.WAITING);
                Collection<Booking> waitingBooking = bookingRepository
                        .getBookingForOwnerByStatus(owner.getId(), Status.WAITING);
                ownerBooking = setBookingsDtoForUser(waitingBooking);
                break;
            case REJECTED:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.REJECTED);
                Collection<Booking> rejectedBooking = bookingRepository
                        .getBookingForOwnerByStatus(owner.getId(), Status.REJECTED);
                ownerBooking = setBookingsDtoForUser(rejectedBooking);
                break;
            case PAST:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.PAST);
                Collection<Booking> pastBooking = bookingRepository
                        .getPastBookingForByOwnerId(owner.getId(), LocalDateTime.now());
                ownerBooking = setBookingsDtoForUser(pastBooking);
                break;
        }
        return ownerBooking;
    }

    @Override
    public Collection<BookingDto> getAllBookingForOwnerWithPagination(BookingState state, Integer ownerId, Integer from, Integer size) {
        from = from / size;
        Collection<BookingDto> ownerBooking = new ArrayList<>();
        User owner = userService.getUserById(ownerId);
        switch (state) {
            case ALL:
                log.info("Владелец {} запросил список всех своих предметов", owner);
                Page<Booking> bookings = bookingRepository
                        .getBookingsByOwnerId(ownerId, PageRequest.of(from, size));
                ownerBooking = setBookingsDtoForUser(bookings.toList());
                break;
            case FUTURE:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.FUTURE);
                Page<Booking> futureBookings = bookingRepository
                        .getFutureBookingForOwner(owner.getId(), PageRequest.of(from, size));
                ownerBooking = setBookingsDtoForUser(futureBookings.toList());
                break;
            case CURRENT:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.FUTURE);
                Page<Booking> currentBookings = bookingRepository
                        .getCurrentBookingForOwner(owner.getId(), PageRequest.of(from, size));
                ownerBooking = setBookingsDtoForUser(currentBookings.toList());
                break;
            case WAITING:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.WAITING);
                Page<Booking> waitingBooking = bookingRepository
                        .getBookingForOwnerByStatus(owner.getId(), Status.WAITING, PageRequest.of(from, size));
                ownerBooking = setBookingsDtoForUser(waitingBooking.toList());
                break;
            case REJECTED:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.REJECTED);
                Page<Booking> rejectedBooking = bookingRepository
                        .getBookingForOwnerByStatus(owner.getId(), Status.REJECTED, PageRequest.of(from, size));
                ownerBooking = setBookingsDtoForUser(rejectedBooking.toList());
                break;
            case PAST:
                log.info("Владелец {} запросил список всех своих предметов со статусом {}",
                        owner, BookingState.PAST);
                Page<Booking> pastBooking = bookingRepository
                        .getPastBookingForByOwnerId(owner.getId(), LocalDateTime.now(), PageRequest.of(from, size));
                ownerBooking = setBookingsDtoForUser(pastBooking.toList());
                break;
        }
        return ownerBooking;
    }

    //Получаю из booking DTO, т.к. из репозитория возвращается только booking
    private Collection<BookingDto> setBookingsDtoForUser(Collection<Booking> bookings) {
        Collection<BookingDto> ownerBooking = new ArrayList<>();
        bookings.forEach(booking -> {
            User user = userService.getUserById(booking.getBookerId());
            ItemDto item = itemService.getItemById(booking.getItemId(), user.getId());
            ownerBooking.add(BookingMapper.toBookingDto(booking, ItemMapper.toItem(item), user));
        });
        return ownerBooking;
    }
}
