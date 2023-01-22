package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {
    UserDto userDto;

    @BeforeEach
    void setValue() {
        this.userDto = new UserDto();
        userDto.setId(1);
        userDto.setEmail("mail@mail.ru");
        userDto.setName("name");
    }

    @Test
    public void toUserTest(){
       User user = UserMapper.toUser(userDto);
       assertEquals(user.getId(), userDto.getId());
       assertEquals(user.getName(), userDto.getName());
       assertEquals(user.getEmail(), userDto.getEmail());
    }
}
