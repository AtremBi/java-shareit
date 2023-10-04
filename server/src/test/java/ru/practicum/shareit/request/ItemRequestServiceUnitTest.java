package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    private ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private ServiceUtil serviceUtil;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        serviceUtil = new ServiceUtil(null, new UserService(userRepository), null);
        itemRequestService = new ItemRequestService(itemRequestRepository, itemRequestMapper, serviceUtil);
    }

    @Test
    void getRequestById() {
        ItemRequestService itemRequestServiceWith = new ItemRequestService(itemRequestRepository,
                null, new ServiceUtil(null, new UserService(userRepository), null));

        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(new User()));
        when(itemRequestRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestServiceWith.getRequestById(-1L, 1L));
        assertEquals("Запрос не найден", exception.getMessage());
    }


    @Test
    void getAllRequests() {
        when(userRepository.findById((any(Long.class))))
                .thenReturn(Optional.of(new User(1L, getRandomString(), getRandomEmail())));
        when(itemRequestRepository.findAllByRequestorIdNot(any(Long.class), any()))
                .thenReturn(new PageImpl<>(List.of(new ItemRequest(1L, getRandomString(),
                        new User(1L, getRandomString(), getRandomEmail()),
                        LocalDateTime.of(2022, 1, 2, 3, 4, 5)))));

        List<ItemRequestDto> listItemRequest = itemRequestService.getAllRequests(0, 10, 1L);
        assertThat(listItemRequest.size(), equalTo(1));
    }

    @Test
    void getRequests() {
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(new User(1L, getRandomString(), getRandomEmail())));
        when(itemRequestRepository.findByRequestorId(any(Long.class)))
                .thenReturn(List.of(new ItemRequest(1L, getRandomString(),
                        new User(1L, getRandomString(), getRandomEmail()),
                        LocalDateTime.of(2022, 1, 2, 3, 4, 5))));

        List<ItemRequestDto> listItemRequest = itemRequestService.getRequests(1L);
        assertThat(listItemRequest.size(), equalTo(1));
    }
}
