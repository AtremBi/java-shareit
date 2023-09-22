package ru.practicum.shareit.Item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.Pagination.Pagination;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(null, TestUtil.getRandomString(), TestUtil.getRandomEmail());
        userRepository.save(user);
        itemRepository.save(new Item(null, TestUtil.getRandomString(), "item 1 Oh"
                , true, user.getId(), null));
        itemRepository.save(new Item(null, TestUtil.getRandomString(), "Soha",
                true, user.getId(), null));
    }

    @Test
    void testFindAllByOwnerOrderById() {
        PageRequest pageRequest = Pagination.of(0, 20, Sort.Direction.ASC, "name");
        List<Item> itemList = itemRepository.findItemByOwnerId(user.getId(), pageRequest)
                .stream().collect(Collectors.toList());

        assertNotNull(itemList);
        assertEquals(2, itemList.size());
    }

    @Test
    void testSearchItemsByText() {
        PageRequest pageRequest = Pagination.of(0, 20, Sort.Direction.ASC, "name");
        List<Item> itemList =
                itemRepository.searchByQuery("oh", pageRequest).stream().collect(Collectors.toList());
        assertNotNull(itemList);
        assertEquals(2, itemList.size());
    }
}
