package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create(Item item, Long userId);

    Item update(Item item, Long itemId, Long userId);

    Item findById(Long id);

    List<Item> findAllByUser(Long userId);

    void delete(Item item);

    List<Item> search(String text);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
    
    List<CommentDto> getItemComments(Long itemId);

    BookingDto getLastBooking(Long itemId, Long userId);
    
    BookingDto getNextBooking(Long itemId, Long userId);
}
