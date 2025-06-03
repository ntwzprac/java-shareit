package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exceptions.NotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest request;
    private ItemRequestDto requestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test User", "test@test.com");
        request = ItemRequest.builder()
                .id(1L)
                .description("Test Request")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        requestDto = ItemRequestDto.builder()
                .description("Test Request")
                .build();
        item = new Item(1L, "Test Item", "Test Description", true, user, request);
    }

    @Test
    void createRequest_ShouldCreateRequest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.createRequest(user.getId(), requestDto);

        assertNotNull(result);
        assertEquals(requestDto.getDescription(), result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.createRequest(999L, requestDto)
        );
    }

    @Test
    void getUserRequests_ShouldReturnRequests() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterIdOrderByCreatedDesc(anyLong())).thenReturn(List.of(request));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getUserRequests(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(request.getDescription(), result.get(0).getDescription());
    }

    @Test
    void getUserRequests_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getUserRequests(999L)
        );
    }

    @Test
    void getAllRequests_ShouldReturnRequests() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(request.getDescription(), result.get(0).getDescription());
    }

    @Test
    void getAllRequests_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getAllRequests(999L, 0, 10)
        );
    }

    @Test
    void getRequestById_ShouldReturnRequest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getRequestById(user.getId(), request.getId());

        assertNotNull(result);
        assertEquals(request.getDescription(), result.getDescription());
    }

    @Test
    void getRequestById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(999L, request.getId())
        );
    }

    @Test
    void getRequestById_ShouldThrowException_WhenRequestNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(user.getId(), 999L)
        );
    }

    @Test
    void getAllRequests_WithPagination_ShouldReturnPaginatedResults() {
        User user1 = new User(1L, "User 1", "user1@test.com");
        User user2 = new User(2L, "User 2", "user2@test.com");
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("Request 1")
                .requester(user2)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        ItemRequest request2 = ItemRequest.builder()
                .id(2L)
                .description("Request 2")
                .requester(user2)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(request2, request1));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L, 0, 2);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(request2.getId(), result.get(0).getId());
        assertEquals(request1.getId(), result.get(1).getId());
    }

    @Test
    void getAllRequests_WithEmptyPage_ShouldReturnEmptyList() {
        User user = new User(1L, "Test User", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L, 0, 2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRequests_WithInvalidPagination_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getAllRequests(1L, -1, 2)
        );
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getAllRequests(1L, 0, 0)
        );
    }
}