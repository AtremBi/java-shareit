package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper mapper;
    private final ServiceUtil serviceUtil;

    public ItemRequestDto createRequest(Long requestorId, ItemRequestDto itemRequestDto, LocalDateTime created) {
        serviceUtil.getUserService().findUserById(requestorId);
        itemRequestDto.setCreated(created);
        ItemRequest itemRequest = mapper.toItemRequest(itemRequestDto, requestorId);
        return mapper.itemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getRequests(Long requestorId) {
        serviceUtil.getUserService().findUserById(requestorId);
        return mapper.itemRequestDto(itemRequestRepository.findByRequestorId(requestorId));
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAllRequests(Integer from, Integer size, Long requestorId) {
        serviceUtil.getUserService().findUserById(requestorId);
        if (from != null && size != null) {
            return itemRequestRepository.findAllByRequestorIdNot(requestorId, PageRequest.of(from, size))
                    .stream().map(mapper::itemRequestDto).collect(Collectors.toList());
        } else {
            return mapper.itemRequestDto(itemRequestRepository.findByRequestorId(requestorId));
        }
    }

    @Transactional(readOnly = true)
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        serviceUtil.getUserService().findUserById(userId);
        return mapper.itemRequestDto(itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден")));
    }
}