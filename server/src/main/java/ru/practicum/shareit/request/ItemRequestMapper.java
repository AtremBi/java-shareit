package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserMapper;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ItemRequestMapper {
    private final ServiceUtil serviceUtil;

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long requestorId) {
        return new ItemRequest(
                null,
                itemRequestDto.getDescription(),
                serviceUtil.getUserService().findUserById(requestorId),
                itemRequestDto.getCreated());
    }

    public ItemRequestDto itemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                UserMapper.toUserDto(itemRequest.getRequestor()),
                itemRequest.getCreated(),
                serviceUtil.getItemService().getItemsByRequestId(itemRequest.getId())
        );
    }

    public List<ItemRequestDto> itemRequestDto(List<ItemRequest> itemRequest) {
        List<ItemRequestDto> itemRequestDtos = new ArrayList<>();
        itemRequest.forEach(request -> {
            itemRequestDtos.add(itemRequestDto(request));
        });
        return itemRequestDtos;
    }

}
