package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestBody @Valid Booking booking
            , @RequestHeader("X-Sharer-User-Id") Integer bookerId) {
        booking.setBookerId(bookerId);
        return bookingService.createBooking(booking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto replyToBooing(@RequestHeader("X-Sharer-User-Id") Integer ownerId
            , @RequestParam boolean approved, @PathVariable Integer bookingId) {
        return bookingService.replyToBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingByIdForOwnerOrBooker(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @PathVariable Integer bookingId) {
        return bookingService.getBookingByIdForOwnerOrBooker(bookingId, userId);
    }

    @GetMapping()
    public Collection<BookingDto> getAllBookingsForUser(@RequestParam(value = "state"
            , required = false, defaultValue = "ALL") BookingState state
            , @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return bookingService.getAllBookingsForUser(state, userId);
    }

    @GetMapping( "/owner")
    public Collection<BookingDto> getAllBookingForOwner(@RequestParam(value = "state"
            , required = false, defaultValue = "ALL") BookingState state
            , @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        return bookingService.getAllBookingForOwner(state, ownerId);
    }
}