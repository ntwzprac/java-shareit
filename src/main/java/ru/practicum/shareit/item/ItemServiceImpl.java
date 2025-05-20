package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingValidationService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.ItemAccessDeniedException;
import ru.practicum.shareit.item.exceptions.CommentNotAllowedException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BookingValidationService bookingValidationService;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;

    private User getUserOrThrow(Long userId) {
        return UserMapper.toUser(userService.findById(userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Предмет с id %d не найден", itemId)));
    }

    private void checkItemOwnership(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException(
                    String.format("Пользователь с id %d не является владельцем предмета с id %d", userId, item.getId())
            );
        }
    }

    @Override
    public Item create(Item item, Long userId) {
        User user = getUserOrThrow(userId);
        item.setOwner(user);
        return itemRepository.save(item);
    }

    @Override
    public Item update(Item item, Long itemId, Long userId) {
        User user = getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);
        checkItemOwnership(existingItem, userId);
        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }
        return itemRepository.save(existingItem);
    }

    @Override
    public Item findById(Long id) {
        return getItemOrThrow(id);
    }

    @Override
    public List<Item> findAllByUser(Long userId) {
        getUserOrThrow(userId);
        return itemRepository.findAllByOwnerId(userId);
    }

    @Override
    public void delete(Item item) {
        itemRepository.delete(item);
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text);
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = getItemOrThrow(itemId);
        User user = getUserOrThrow(userId);

        if (!bookingValidationService.hasUserBookedItem(userId, itemId)) {
            throw new CommentNotAllowedException(
                    String.format("Пользователь с id %d не может оставить комментарий к предмету с id %d", userId, itemId)
            );
        }

        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getItemComments(Long itemId) {
        getItemOrThrow(itemId);
        return commentRepository.findAllByItemId(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto getLastBooking(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            return null;
        }

        return bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                itemId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        ).map(BookingMapper::toBookingDto).orElse(null);
    }

    @Override
    public BookingDto getNextBooking(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            return null;
        }

        return bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                itemId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        ).map(BookingMapper::toBookingDto).orElse(null);
    }
}