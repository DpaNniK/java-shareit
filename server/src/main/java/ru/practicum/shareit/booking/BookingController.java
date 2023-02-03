package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestBody BookItemRequestDto bookingDto,
                                    @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        Booking booking = new Booking();
        booking.setBookerId(Math.toIntExact(bookerId));
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItemId((int) bookingDto.getItemId());
        return bookingService.createBooking(booking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto replyToBooking(@RequestHeader("X-Sharer-User-Id") Integer ownerId,
                                     @RequestParam(name = "approved") boolean approved,
                                     @PathVariable Integer bookingId) {
        return bookingService.replyToBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingByIdForOwnerOrBooker(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                     @PathVariable Integer bookingId) {
        return bookingService.getBookingByIdForOwnerOrBooker(bookingId, userId);
    }

    @GetMapping()
    public Collection<BookingDto> getAllBookingsForUser(@RequestParam(value = "from", required = false) Integer from,
                                                        @RequestParam(value = "size", required = false) Integer size,
                                                        @RequestParam(value = "state", required = false,
                                                                defaultValue = "ALL") BookingState state,
                                                        @RequestHeader("X-Sharer-User-Id") Integer userId) {
        if (from == null) return bookingService.getAllBookingsForUser(state, userId);
        return bookingService.getAllBookingsForUserWithPagination(state, userId, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getAllBookingForOwner(@RequestParam(value = "from", required = false) Integer from,
                                                        @RequestParam(value = "size", required = false) Integer size,
                                                        @RequestParam(value = "state", required = false,
                                                                defaultValue = "ALL") BookingState state,
                                                        @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        if (from == null) return bookingService.getAllBookingForOwner(state, ownerId);
        return bookingService.getAllBookingForOwnerWithPagination(state, ownerId, from, size);
    }
}