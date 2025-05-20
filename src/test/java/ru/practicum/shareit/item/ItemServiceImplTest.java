package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingValidationService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.exceptions.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {
    private ItemService itemService;
    private ItemRepository itemRepository;
    private CommentRepository commentRepository;
    private UserService userService;
    private BookingValidationService bookingValidationService;
    private CommentMapper commentMapper;
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Comment comment;
    private Booking booking;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        commentRepository = mock(CommentRepository.class);
        userService = mock(UserService.class);
        bookingValidationService = mock(BookingValidationService.class);
        commentMapper = mock(CommentMapper.class);
        bookingRepository = mock(BookingRepository.class);
        itemService = new ItemServiceImpl(itemRepository, commentRepository, userService,
                bookingValidationService, commentMapper, bookingRepository);

        owner = new User(1L, "Owner", "owner@test.com");
        booker = new User(2L, "Booker", "booker@test.com");
        item = new Item(1L, "Test Item", "Description", true, owner, null);
        comment = new Comment(1L, "Test Comment", item, booker, LocalDateTime.now());
        booking = new Booking(1L, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1),
                item, booker, BookingStatus.APPROVED);
    }

    @Test
    void create_ValidItem_ShouldCreateItem() {
        Item newItem = new Item(null, "Test Item", "Description", true, null, null);
        when(userService.findById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@test.com"));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = itemService.create(newItem, 1L);

        assertNotNull(result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(owner, result.getOwner());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_NonExistingUser_ShouldThrowException() {
        Item newItem = new Item(null, "Test Item", "Description", true, null, null);
        when(userService.findById(1L)).thenThrow(new UserNotFoundException("Пользователь не найден"));

        assertThrows(UserNotFoundException.class, () -> itemService.create(newItem, 1L));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ValidItem_ShouldUpdateItem() {
        Item updateData = new Item(null, "Updated Name", "Updated Description", false, null, null);
        Item updatedItem = new Item(1L, "Updated Name", "Updated Description", false, owner, null);

        when(userService.findById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@test.com"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item result = itemService.update(updateData, 1L, 1L);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_NonExistingItem_ShouldThrowException() {
        Item updateData = new Item(null, "Updated Name", "Updated Description", false, null, null);
        when(userService.findById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@test.com"));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.update(updateData, 1L, 1L));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ItemNotOwnedByUser_ShouldThrowException() {
        Item updateData = new Item(null, "Updated Name", "Updated Description", false, null, null);
        when(userService.findById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@test.com"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ItemAccessDeniedException.class, () -> itemService.update(updateData, 1L, 2L));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void findById_ExistingItem_ShouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = itemService.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
    }

    @Test
    void findById_NonExistingItem_ShouldThrowException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.findById(999L));
    }

    @Test
    void findAllByUser_ShouldReturnUserItems() {
        List<Item> items = List.of(item);
        when(userService.findById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@test.com"));
        when(itemRepository.findAllByOwnerId(1L)).thenReturn(items);

        List<Item> result = itemService.findAllByUser(1L);
        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
    }

    @Test
    void findAllByUser_NonExistingUser_ShouldThrowException() {
        when(userService.findById(1L)).thenThrow(new UserNotFoundException("Пользователь не найден"));

        assertThrows(UserNotFoundException.class, () -> itemService.findAllByUser(1L));
    }

    @Test
    void search_ShouldReturnMatchingItems() {
        List<Item> items = List.of(item);
        when(itemRepository.search("test")).thenReturn(items);

        List<Item> result = itemService.search("test");
        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyList() {
        List<Item> result = itemService.search("");
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void addComment_ValidComment_ShouldAddComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test Comment");
        when(userService.findById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@test.com"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingValidationService.hasUserBookedItem(2L, 1L)).thenReturn(true);
        when(commentMapper.toComment(commentDto)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        CommentDto expectedDto = new CommentDto();
        expectedDto.setId(1L);
        expectedDto.setText("Test Comment");
        expectedDto.setAuthorName("Booker");
        expectedDto.setCreated(LocalDateTime.now());
        when(commentMapper.toDto(comment)).thenReturn(expectedDto);

        CommentDto result = itemService.addComment(1L, 2L, commentDto);

        assertNotNull(result);
        assertEquals("Test Comment", result.getText());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_UserNotBookedItem_ShouldThrowException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test Comment");
        when(userService.findById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@test.com"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingValidationService.hasUserBookedItem(2L, 1L)).thenReturn(false);

        assertThrows(CommentNotAllowedException.class, () -> itemService.addComment(1L, 2L, commentDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getItemComments_ShouldReturnComments() {
        List<Comment> comments = List.of(comment);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(comments);
        when(commentMapper.toDto(comment)).thenReturn(new CommentDto(1L, "Test Comment", "Booker", LocalDateTime.now()));

        List<CommentDto> result = itemService.getItemComments(1L);

        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getText());
    }

    @Test
    void getLastBooking_OwnerRequest_ShouldReturnLastBooking() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                eq(1L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(Optional.of(booking));

        BookingDto result = itemService.getLastBooking(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getLastBooking_NonOwnerRequest_ShouldReturnNull() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingDto result = itemService.getLastBooking(1L, 2L);

        assertNull(result);
    }

    @Test
    void getNextBooking_OwnerRequest_ShouldReturnNextBooking() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                eq(1L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(Optional.of(booking));

        BookingDto result = itemService.getNextBooking(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getNextBooking_NonOwnerRequest_ShouldReturnNull() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingDto result = itemService.getNextBooking(1L, 2L);

        assertNull(result);
    }

    @Test
    void delete_ExistingItem_ShouldDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(item);

        verify(itemRepository).delete(item);
    }
}