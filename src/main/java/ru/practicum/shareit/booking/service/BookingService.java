package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.Collection;

public interface BookingService {
    BookingDto createBooking(Booking booking);

    BookingDto replyToBooking(Integer ownerId, Integer bookingId, boolean approved);

    BookingDto getBookingById(Integer bookingId);

    BookingDto getBookingByIdForOwnerOrBooker(Integer bookingId, Integer userId);

    Collection<BookingDto> getAllBookingsForUser(BookingState state, Integer userId);

    Collection<BookingDto> getAllBookingsForUserWithPagination(BookingState state
            , Integer userId, Integer from, Integer size);

    Collection<BookingDto> getAllBookingForOwner(BookingState state, Integer ownerId);

    Collection<BookingDto> getAllBookingForOwnerWithPagination(BookingState state
            , Integer userId, Integer from, Integer size);

}
