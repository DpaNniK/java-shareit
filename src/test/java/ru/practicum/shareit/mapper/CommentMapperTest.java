package ru.practicum.shareit.mapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentMapperTest {
    public Comment comment;
    public User user;
    public Item item;

    @BeforeEach
    void setValues() {
        this.comment = new Comment();
        comment.setId(1);
        comment.setText("text");
        comment.setCreated(LocalDateTime.now());
        comment.setAuthorId(1);
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
    public void commentMapperToCommentDtoTest() {
        CommentDto commentDto = CommentMapper.commentDto(comment, user, item);
        assertEquals(commentDto.getId(), comment.getId());
        assertEquals(commentDto.getText(), comment.getText());
        assertEquals(commentDto.getCreated(), comment.getCreated());
        assertEquals(commentDto.getAuthor().getId(), comment.getAuthorId());
    }
}
