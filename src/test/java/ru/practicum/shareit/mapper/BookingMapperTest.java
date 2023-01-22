package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingMapperTest {
    public Booking booking;
    public Item item;
    public User user;

    @BeforeEach
    void setValues() {
        this.booking = new Booking();
        booking.setId(1);
        booking.setStatus(Status.APPROVED);
        booking.setItemId(1);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusDays(5));
        booking.setBookerId(1);
        this.item = new Item();
        item.setRequestId(1);
        item.setId(1);
        item.setName("name");
        item.setDescription("desc");
        item.setAvailable(true);
        this.user = new User();
        user.setId(1);
        user.setEmail("mail@mail.ru");
        user.setName("name");
    }

    @Test
    public void bookingMapperToBookingDtoTest() {
        BookingDto bookingDtoResult = BookingMapper.toBookingDto(booking, item, user);

        assertEquals(bookingDtoResult.getId(), booking.getId()
                , "Неверно присвоен ID");
        assertEquals(bookingDtoResult.getBooker().getId(), booking.getBookerId()
                , "Неверно присвоен ID букера");
        assertEquals(bookingDtoResult.getItem().getId(), booking.getItemId()
                , "Неверно присвоен ID для вещи");
        assertEquals(bookingDtoResult.getStatus(), booking.getStatus()
                , "Неверно присвоен статус");
        assertEquals(bookingDtoResult.getStart(), booking.getStart()
                , "Неверно присвоено время начала аренды");
        assertEquals(bookingDtoResult.getEnd(), booking.getEnd()
                , "Неверно присвоено время окончания аренды");
    }

    @Test
    public void bookingMapper() {
        BookingDto bookingDtoResult = BookingMapper.toBookingDto(booking, item, user);
        Booking bookingResult = BookingMapper.toBooking(bookingDtoResult);

        assertEquals(bookingResult.getId(), bookingDtoResult.getId()
                , "Неверно присвоен ID");
        assertEquals(bookingResult.getBookerId(), bookingDtoResult.getBooker().getId()
                , "Неверно присвоен ID букера");
        assertEquals(bookingResult.getItemId(), bookingDtoResult.getItem().getId(),
                "Неверно присвоен ID для вещи");
        assertEquals(bookingResult.getStatus(), bookingDtoResult.getStatus()
                , "Неверно присвоен статус");
        assertEquals(bookingResult.getStart(), bookingDtoResult.getStart()
                , "Неверно присвоено время начала аренды");
        assertEquals(bookingResult.getEnd(), bookingDtoResult.getEnd()
                , "Неверно присвоено время окончания аренды");
    }
}

