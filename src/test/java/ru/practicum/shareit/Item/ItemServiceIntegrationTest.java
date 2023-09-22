package ru.practicum.shareit.Item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ItemServiceIntegrationTest {
    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldException_whenGetItem_withWrongId() {
        ItemService itemService = new ItemService(itemRepository, null, null,
                null);

        when(itemRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(-1L, 1L));
        Assertions.assertEquals("Вещь не найдена", exception.getMessage());
    }
}
