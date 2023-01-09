package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;

public class ItemMapper {

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        if (itemDto.getOwner() != null) item.setOwnerId(itemDto.getOwner().getId());
        item.setRequestId(itemDto.getRequestId());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public static ItemDto toItemDto(Item item, User owner, Collection<Booking> bookingList
            , Collection<CommentResponseDto> commentResponseList) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setId(item.getId());
        itemDto.setOwner(owner);
        itemDto.setAvailable(item.isAvailable());
        itemDto.setRequestId(item.getRequestId());
        if (bookingList.size() >= 2) {
            itemDto.setLastBooking(new ArrayList<>(bookingList).get(0));
            itemDto.setNextBooking(new ArrayList<>(bookingList).get(1));
        }
        if(commentResponseList.size()!= 0) {
            itemDto.setComments(commentResponseList);
        } else {
            itemDto.setComments(new ArrayList<>());
        }
        return itemDto;
    }
}
