package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    BookingService bookingService;
    @InjectMocks
    BookingController bookingController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .build();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        this.booking = new Booking();
        booking.setStart(LocalDateTime.now());
        booking.setStatus(Status.APPROVED);
        booking.setEnd(LocalDateTime.now().minusDays(5));
        booking.setItemId(1);
        booking.setBookerId(1);
        booking.setId(1);
        this.bookingDto = new BookingDto();
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
    }

    @Test
    public void createBookingTest() throws Exception {
        when(bookingService.createBooking(any())).thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Integer.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    public void getBookingByIdForOwnerOrBookerTest() throws Exception {
        when(bookingService.getBookingByIdForOwnerOrBooker(any(), any())).thenReturn(bookingDto);

        mvc.perform(get("/bookings/1")
                        .content(mapper.writeValueAsString(booking))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Integer.class))
                .andExpect(jsonPath("$.start", is(notNullValue())))
                .andExpect(jsonPath("$.end", is(notNullValue())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    public void getAllBookingsForUserTest() throws Exception {
        when(bookingService.getAllBookingsForUser(any(), any())).thenReturn(List.of(bookingDto));
        BookingState state = BookingState.ALL;
        mvc.perform(get("/bookings?state={state}", state)
                        .content(mapper.writeValueAsString(booking))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(booking.getId())))
                .andExpect(jsonPath("$[*].start", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].end", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder(booking.getStatus().toString())));
    }

    @Test
    public void getAllBookingsForUserWithPagination() throws Exception {
        when(bookingService.getAllBookingsForUserWithPagination(any(), any(), any(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings?from=0&size=10")
                        .content(mapper.writeValueAsString(booking))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(booking.getId())))
                .andExpect(jsonPath("$[*].start", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].end", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder(booking.getStatus().toString())));
    }

    @Test
    public void getAllBookingForOwnerTest() throws Exception {
        when(bookingService.getAllBookingForOwner(any(), any())).thenReturn(List.of(bookingDto));
        BookingState state = BookingState.ALL;

        mvc.perform(get("/bookings/owner?state={state}", state)
                        .content(mapper.writeValueAsString(booking))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(booking.getId())))
                .andExpect(jsonPath("$[*].start", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].end", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder(booking.getStatus().toString())));
    }

    @Test
    public void getAllBookingForOwnerTestWithPagination() throws Exception {
        when(bookingService.getAllBookingForOwnerWithPagination(any(), any(), any(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner?from=0&size=10")
                        .content(mapper.writeValueAsString(booking))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(booking.getId())))
                .andExpect(jsonPath("$[*].start", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].end", containsInAnyOrder(notNullValue())))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder(booking.getStatus().toString())));
    }
}
