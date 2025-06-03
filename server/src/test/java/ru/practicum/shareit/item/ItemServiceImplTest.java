package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingValidationService;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.CommentNotAllowedException;
import ru.practicum.shareit.item.exceptions.ItemAccessDeniedException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingValidationService bookingValidationService;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test User", "test@test.com");
        item = new Item(1L, "Test Item", "Test Description", true, user, null);
        itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null, null, null, null);
        comment = new Comment(1L, "Test Comment", item, user, LocalDateTime.now());
        commentDto = new CommentDto(1L, "Test Comment", "Test User", LocalDateTime.now());
        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
    }

    @Test
    void create_ShouldCreateItem() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = itemService.create(item, user.getId());

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_ShouldUpdateItem() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item updatedItem = new Item();
        updatedItem.setName("Updated Name");
        updatedItem.setDescription("Updated Description");

        Item result = itemService.update(updatedItem, item.getId(), user.getId());

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_ShouldThrowException_WhenUserIsNotOwner() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Item updatedItem = new Item();
        updatedItem.setName("Updated Name");

        assertThrows(ItemAccessDeniedException.class, () ->
                itemService.update(updatedItem, item.getId(), 999L)
        );
    }

    @Test
    void update_ShouldThrowException_WhenItemNotFound() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Item updatedItem = new Item();
        updatedItem.setName("Updated Name");

        assertThrows(ItemNotFoundException.class, () ->
                itemService.update(updatedItem, 999L, user.getId())
        );
    }

    @Test
    void findById_ShouldReturnItem() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Item result = itemService.findById(item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
    }

    @Test
    void findById_ShouldThrowException_WhenItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () ->
                itemService.findById(999L)
        );
    }

    @Test
    void findAllByUser_ShouldReturnItems() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item));

        List<Item> result = itemService.findAllByUser(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void search_ShouldReturnItems() {
        when(itemRepository.search(anyString())).thenReturn(List.of(item));

        List<Item> result = itemService.search("test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void search_ShouldReturnEmptyList_WhenTextIsBlank() {
        List<Item> result = itemService.search("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_ShouldAddComment() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingValidationService.hasUserBookedItem(anyLong(), anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(List.of(booking));
        when(commentMapper.toComment(any(CommentDto.class))).thenReturn(comment);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(item.getId(), user.getId(), commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_ShouldThrowException_WhenUserHasNotBookedItem() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingValidationService.hasUserBookedItem(anyLong(), anyLong())).thenReturn(false);

        assertThrows(CommentNotAllowedException.class, () ->
                itemService.addComment(item.getId(), user.getId(), commentDto)
        );
    }

    @Test
    void addComment_ShouldThrowException_WhenItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () ->
                itemService.addComment(999L, user.getId(), commentDto)
        );
    }

    @Test
    void addComment_ShouldThrowException_WhenUserNotFound() {
        lenient().when(userService.findById(anyLong())).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () ->
                itemService.addComment(item.getId(), 999L, commentDto)
        );
    }

    @Test
    void getItemComments_ShouldReturnComments() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);

        List<CommentDto> result = itemService.getItemComments(item.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(commentDto.getText(), result.get(0).getText());
    }

    @Test
    void getEnrichedItemDto_ShouldReturnEnrichedItemDto() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdsAndStatusOrderByStartAsc(anyList(), any())).thenReturn(List.of(booking));
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);

        ItemDto result = itemService.getEnrichedItemDto(item.getId(), user.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
    }

    @Test
    void getEnrichedItemDto_ShouldThrowException_WhenItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () ->
                itemService.getEnrichedItemDto(999L, user.getId())
        );
    }

    @Test
    void findAllEnrichedByUser_ShouldReturnEnrichedItemDtos() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemIdsAndStatusOrderByStartAsc(anyList(), any())).thenReturn(List.of(booking));
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);

        List<ItemDto> result = itemService.findAllEnrichedByUser(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        ItemDto itemDto = result.get(0);
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertNotNull(itemDto.getComments());
        assertEquals(1, itemDto.getComments().size());
    }

    @Test
    void findAllEnrichedByUser_ShouldThrowException_WhenUserNotFound() {
        when(userService.findById(anyLong())).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () ->
                itemService.findAllEnrichedByUser(999L)
        );
    }

    @Test
    void findAllByRequestId_ShouldReturnItems() {
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(item));

        List<Item> result = itemService.findAllByRequestId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
        assertEquals(item.getName(), result.get(0).getName());
        assertEquals(item.getDescription(), result.get(0).getDescription());
        assertEquals(item.getAvailable(), result.get(0).getAvailable());
    }

    @Test
    void findAllByRequestId_ShouldReturnEmptyList_WhenNoItemsFound() {
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of());

        List<Item> result = itemService.findAllByRequestId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEnrichedItemDto_ShouldReturnItemDtoWithNoBookings_WhenNoBookingsFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdsAndStatusOrderByStartAsc(anyList(), any())).thenReturn(List.of());
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        ItemDto result = itemService.getEnrichedItemDto(item.getId(), user.getId());

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getEnrichedItemDto_ShouldReturnItemDtoWithNoComments_WhenNoCommentsFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdsAndStatusOrderByStartAsc(anyList(), any())).thenReturn(List.of(booking));
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        ItemDto result = itemService.getEnrichedItemDto(item.getId(), user.getId());

        assertNotNull(result);
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void findAllEnrichedByUser_ShouldReturnEmptyList_WhenNoItemsFound() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of());

        List<ItemDto> result = itemService.findAllEnrichedByUser(user.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllEnrichedByUser_ShouldReturnItemsWithNoBookings_WhenNoBookingsFound() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemIdsAndStatusOrderByStartAsc(anyList(), any())).thenReturn(List.of());
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemDto> result = itemService.findAllEnrichedByUser(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getLastBooking());
        assertNull(result.get(0).getNextBooking());
    }

    @Test
    void findAllEnrichedByUser_ShouldReturnItemsWithNoComments_WhenNoCommentsFound() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemIdsAndStatusOrderByStartAsc(anyList(), any())).thenReturn(List.of(booking));
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemDto> result = itemService.findAllEnrichedByUser(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getComments().isEmpty());
    }

    @Test
    void addComment_WhenUserHasBookedItem_ShouldAddComment() {
        UserDto userDto = new UserDto(1L, "Test User", "test@test.com");
        when(userService.findById(anyLong())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingValidationService.hasUserBookedItem(anyLong(), anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(List.of(booking));
        when(commentMapper.toComment(any(CommentDto.class))).thenReturn(comment);
        when(commentRepository.save(any())).thenReturn(comment);
        when(commentMapper.toDto(any())).thenReturn(commentDto);

        CommentDto inputCommentDto = new CommentDto();
        inputCommentDto.setText("Test Comment");
        CommentDto result = itemService.addComment(1L, 1L, inputCommentDto);

        assertNotNull(result);
        assertEquals(commentDto.getId(), result.getId());
        assertEquals(commentDto.getText(), result.getText());
        verify(commentRepository).save(any());
    }

    @Test
    void addComment_WhenUserHasNotBookedItem_ShouldThrowException() {
        UserDto userDto = new UserDto(1L, "Test User", "test@test.com");
        when(userService.findById(anyLong())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingValidationService.hasUserBookedItem(anyLong(), anyLong())).thenReturn(false);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test Comment");
        assertThrows(CommentNotAllowedException.class, () ->
                itemService.addComment(1L, 1L, commentDto)
        );
    }

    @Test
    void addComment_WhenItemNotFound_ShouldThrowException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test Comment");
        assertThrows(ItemNotFoundException.class, () ->
                itemService.addComment(1L, 1L, commentDto)
        );
    }

    @Test
    void addComment_WhenNoCompletedBooking_ShouldThrowException() {
        UserDto userDto = new UserDto(1L, "Test User", "test@test.com");
        when(userService.findById(anyLong())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingValidationService.hasUserBookedItem(anyLong(), anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(List.of());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test Comment");
        assertThrows(CommentNotAllowedException.class, () ->
                itemService.addComment(1L, 1L, commentDto)
        );
    }

    @Test
    void getComments_ShouldReturnAllCommentsForItem() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any())).thenReturn(commentDto);

        List<CommentDto> result = itemService.getItemComments(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(commentDto.getId(), result.get(0).getId());
    }
}