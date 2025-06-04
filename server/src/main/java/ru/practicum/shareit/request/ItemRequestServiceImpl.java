package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = findUserById(userId);

        ItemRequest request = ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        ItemRequest savedRequest = requestRepository.save(request);
        return ItemRequestMapper.toDto(savedRequest, List.of());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        findUserById(userId);

        List<ItemRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        return convertRequestsToDto(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Параметры пагинации некорректны");
        }

        findUserById(userId);

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId, pageRequest);
        return convertRequestsToDto(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        findUserById(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);
        return ItemRequestMapper.toDto(request, items);
    }

    private List<ItemRequestDto> convertRequestsToDto(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }
}