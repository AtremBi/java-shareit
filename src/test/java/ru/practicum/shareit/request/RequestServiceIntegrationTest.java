package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class RequestServiceIntegrationTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserService userService;

    @Test
    void shouldException_whenGetItemRequest_withWrongId() {
        ItemRequestService itemRequestServiceWith = new ItemRequestService(itemRequestRepository,
                null, new ServiceUtil(null, userService, null));

        when(userService.findUserById(any(Long.class)))
                .thenReturn(new User());
        when(itemRequestRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestServiceWith.getRequestById(-1L, 1L));
        assertEquals("Запрос не найден", exception.getMessage());
    }
}
