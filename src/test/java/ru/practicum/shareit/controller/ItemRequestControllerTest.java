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
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {
    @Mock
    private ItemRequestService itemRequestService;
    @InjectMocks
    private ItemRequestController itemRequestController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private RequestDto requestDto;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(itemRequestController)
                .build();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        this.requestDto = new RequestDto();
        requestDto.setId(1);
        requestDto.setDescription("Desc");
        requestDto.setCreated(LocalDateTime.now());
        this.request = new ItemRequest();
        request.setId(requestDto.getId());
        request.setCreated(requestDto.getCreated());
        request.setDescription(requestDto.getDescription());
    }

    @Test
    public void createItemRequestTest() throws Exception {
        when(itemRequestService.createItemRequest(any(), any())).thenReturn(requestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(request.getDescription())))
                .andExpect(jsonPath("$.created", is(notNullValue())));
    }

    @Test
    public void getItemRequestForUserTest() throws Exception {
        when(itemRequestService.getItemRequestForUser(any())).thenReturn(List.of(requestDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(request.getId())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(request.getDescription())))
                .andExpect(jsonPath("$[*].created", containsInAnyOrder(notNullValue())));
    }

    @Test
    public void getItemsTest() throws Exception {
        when(itemRequestService.getAllRequestItems()).thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(request.getId())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(request.getDescription())))
                .andExpect(jsonPath("$[*].created", containsInAnyOrder(notNullValue())));
    }

    @Test
    public void getItemsWithPaginationTest() throws Exception {
        when(itemRequestService.getItemsWithPagination(any(), any(), any())).thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all?from=0&size=10")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(request.getId())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(request.getDescription())))
                .andExpect(jsonPath("$[*].created", containsInAnyOrder(notNullValue())));
    }

    @Test
    public void getRequestByIdTest() throws Exception {
        when(itemRequestService.getRequestById(any(), any())).thenReturn(requestDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request.getId())))
                .andExpect(jsonPath("$.description", is(request.getDescription())))
                .andExpect(jsonPath("$.created", is(notNullValue())));
    }

    @Test
    public void getErrorForNotValidRequest() throws Exception {
        request.setDescription(null);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
