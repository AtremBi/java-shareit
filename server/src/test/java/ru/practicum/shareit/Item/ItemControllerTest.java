package ru.practicum.shareit.Item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private static final String USER_ID = "X-Sharer-User-Id";

    private User user = new User(1L, getRandomString(), getRandomEmail());

    private Item item = new Item(1L, getRandomString(), getRandomString(), true, user.getId(), null);
    private ItemDto itemDto = new ItemDto(1L, getRandomString(), getRandomString(), true,
            null, null, null, null);
    private CommentDto commentDto = new CommentDto(1L, getRandomString(), item,
            user.getName(), LocalDateTime.of(2022, 3, 5, 1, 2, 3));

    private List listItemDto = new ArrayList<>();

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(any(Long.class), any()))
                .thenReturn(itemDto);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void getItem() throws Exception {
        when(itemService.getItemById(any(Long.class), any(Long.class)))
                .thenReturn(itemDto);
        mvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void getItems_byOwner() throws Exception {
        when(itemService.getItems(any(Long.class), any(Integer.class), nullable(Integer.class)))
                .thenReturn(List.of(itemDto));
        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(listItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())));
    }

    @Test
    void deleteItem() throws Exception {
        mvc.perform(delete("/items/1")
                        .header(USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(any(Long.class), any(Long.class), any()))
                .thenReturn(itemDto);
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }


    @Test
    void getItems_bySearchQuery() throws Exception {
        when(itemService.searchItems(any(String.class), any(Integer.class), nullable(Integer.class)))
                .thenReturn(List.of(itemDto));
        mvc.perform(get("/items/search?text=description")
                        .content(mapper.writeValueAsString(listItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())));
    }

    @Test
    void createComment() throws Exception {
        when(itemService.addComment(any(), any(Long.class), any(Long.class)))
                .thenReturn(commentDto);
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created",
                        is(commentDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }
}