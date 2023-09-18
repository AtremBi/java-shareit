package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exeptions.ItemUnavailable;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.WrongUserException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class ItemService {
    private final ItemRepository itemStorage;
    private final CommentRepository commentRepository;
    private final ServiceUtil serviceUtil;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemService(ItemRepository itemStorage, CommentRepository commentRepository, @Lazy ServiceUtil serviceUtil,
                       @Lazy ItemMapper itemMapper) {
        this.itemStorage = itemStorage;
        this.commentRepository = commentRepository;
        this.serviceUtil = serviceUtil;
        this.itemMapper = itemMapper;
    }

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Item item = itemMapper.toItem(userId, itemDto);
        serviceUtil.getUserService().getUserById(userId);
        item.setOwnerId(userId);
        return itemMapper.toItemDto(itemStorage.save(item));
    }

    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        if (size == null) {
            size = 20;
        }
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.Direction.DESC, "name");
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemStorage.searchByQuery(text, pageRequest).stream()
                .map(itemMapper::toItemDto).collect(Collectors.toList());
    }

    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = findItemById(itemId);
        ItemDto itemDto;
        if (item.getOwnerId().equals(userId)) {
            itemDto = itemMapper.toItemDtoWithLastAndEndNextBooking(item);
        } else {
            itemDto = itemMapper.toItemDto(item);
        }
        if (itemDto.getLastBooking() != null && serviceUtil.getBookingService().getBookingById(itemDto.getLastBooking().getId(),
                userId).getStatus().equals(BookingStatus.REJECTED)) {
            itemDto.setLastBooking(null);
        }
        if (itemDto.getNextBooking() != null && serviceUtil.getBookingService().getBookingById(itemDto.getNextBooking().getId(),
                userId).getStatus().equals(BookingStatus.REJECTED)) {
            itemDto.setNextBooking(null);
        }
        return itemDto;
    }

    public Item findItemById(Long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }

    public List<ItemDto> getItemsByRequestId(Long requestId) {
        return itemMapper.toItemDto(itemStorage.findByRequestId(requestId));
    }

    public List<ItemDto> getItems(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.Direction.DESC, "name");
        return itemMapper.toItemDtoWithLastAndEndNextBooking(itemStorage.findItemByOwnerId(userId, pageRequest).stream()
                .sorted(Comparator.comparing(Item::getId))
                .collect(toList()));
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        itemDto.setId(itemId);
        Item item = itemMapper.toItem(userId, itemDto);
        validateOwner(userId, itemId);
        serviceUtil.getUserService().getUserById(userId);
        Item oldItem = itemMapper.toItem(userId, itemMapper.toItemDto(findItemById(itemId)));
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        return createItem(oldItem.getOwnerId(), itemMapper.toItemDto(oldItem));
    }

    public void deleteItem(Long itemId) {
        itemStorage.delete(itemStorage.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена")));
    }

    public CommentDto addComment(CommentDto commentDto, Long itemId, Long userId) {
        Comment comment = new Comment();
        Booking booking = serviceUtil.getBookingService().getBookingWithUserBookedItem(itemId, userId);
        if (booking != null) {
            comment.setCreated(LocalDateTime.now());
            comment.setItem(booking.getItem());
            comment.setAuthor(serviceUtil.getUserService().findUserById(userId));
            comment.setText(commentDto.getText());
        } else {
            throw new ItemUnavailable("У пользователя нет забронированных вещей");
        }
        return itemMapper.toCommentDto(commentRepository.save(comment));
    }


    public List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findAllByItemId(itemId,
                        Sort.by(Sort.Direction.DESC, "created")).stream()
                .map(itemMapper::toCommentDto)
                .collect(toList());
    }

    private boolean validateOwner(Long userId, Long itemId) {
        if (userId.equals(itemStorage.findById(itemId).get().getOwnerId())) {
            return true;
        } else {
            throw new WrongUserException("У пользователя нет такой вещи");
        }
    }
}
