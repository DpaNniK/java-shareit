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
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    @Mock
    private ItemService itemService;
    @InjectMocks
    private ItemController itemController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        this.item = new Item();
        item.setId(1);
        item.setOwnerId(1);
        item.setAvailable(true);
        item.setName("item");
        item.setDescription("desc");
        item.setRequestId(1);

        this.itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setAvailable(true);
        itemDto.setName("item");
        itemDto.setDescription("desc");
        itemDto.setRequestId(1);
    }

    @Test
    public void createItemTest() throws Exception {
        when(itemService.createItem(any(), any())).thenReturn(item);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    public void createCommentTest() throws Exception {
        Comment comment = new Comment();
        comment.setCreated(LocalDateTime.now());
        comment.setId(1);
        comment.setText("text");
        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(comment.getId());
        commentResponseDto.setCreated(comment.getCreated());
        commentResponseDto.setText(comment.getText());

        when(itemService.createComment(any(), any(), any())).thenReturn(commentResponseDto);

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Integer.class))
                .andExpect(jsonPath("$.created", is(notNullValue())))
                .andExpect(jsonPath("$.text", is(comment.getText())));
    }

    @Test
    public void updateItemTest() throws Exception {
        when(itemService.updateItem(any(), any(), any())).thenReturn(item);

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    public void getItemByIdTest() throws Exception {
        when(itemService.getItemById(any(), any())).thenReturn(itemDto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    public void getAllItemsOwnerTest() throws Exception {
        when(itemService.getAllItemsOwner(any())).thenReturn(List.of(itemDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(itemDto.getId())))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(itemDto.getName())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(itemDto.getDescription())))
                .andExpect(jsonPath("$[*].available", containsInAnyOrder(itemDto.getAvailable())))
                .andExpect(jsonPath("$[*].requestId", containsInAnyOrder(itemDto.getRequestId())));
    }

    @Test
    public void getAllItemsOwnerWithPagination() throws Exception {
        when(itemService.getAllItemsWithPagination(any(), any(), any())).thenReturn(List.of(itemDto));

        mvc.perform(get("/items?from=0&size=20")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(itemDto.getId())))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(itemDto.getName())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(itemDto.getDescription())))
                .andExpect(jsonPath("$[*].available", containsInAnyOrder(itemDto.getAvailable())))
                .andExpect(jsonPath("$[*].requestId", containsInAnyOrder(itemDto.getRequestId())));
    }

    @Test
    public void searchItemByTextTest() throws Exception {
        when(itemService.searchItemByText(any(), any())).thenReturn(List.of(item));

        mvc.perform(get("/items/search?text=text")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(itemDto.getId())))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(itemDto.getName())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(itemDto.getDescription())))
                .andExpect(jsonPath("$[*].available", containsInAnyOrder(itemDto.getAvailable())))
                .andExpect(jsonPath("$[*].requestId", containsInAnyOrder(itemDto.getRequestId())));
    }

    @Test
    public void searchItemByTextTestWithPagination() throws Exception {
        when(itemService.searchItemByTextWithPagination(any(), any(), any(), any())).thenReturn(List.of(item));

        mvc.perform(get("/items/search?text=text&from=0&size=20")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(itemDto.getId())))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(itemDto.getName())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(itemDto.getDescription())))
                .andExpect(jsonPath("$[*].available", containsInAnyOrder(itemDto.getAvailable())))
                .andExpect(jsonPath("$[*].requestId", containsInAnyOrder(itemDto.getRequestId())));
    }
}
