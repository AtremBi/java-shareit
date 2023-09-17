package ru.practicum.shareit.booking.dto;

import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingDtoTest {
    private JacksonTester<BookingDto> json;
    private BookingDto bookingDto;
    private Validator validator;

    public BookingDtoTest(@Autowired JacksonTester<BookingDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private String getRandomEmail() {
        RandomString randomString = new RandomString();
        return randomString.nextString() + "@" + randomString.nextString() + ".ew";
    }

    private String getRandomString() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }

    @BeforeEach
    void beforeEach() {
        bookingDto = new BookingDto(
                1L,
                LocalDateTime.of(2023, 12, 25, 12, 0, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0, 0),
                new Item(1L, getRandomString(), getRandomString(),
                        true, null, null),
                new User(2L, getRandomString(), getRandomEmail()),
                BookingStatus.WAITING);

    }

    @Test
    void testJsonBookingDto() throws Exception {
        JsonContent<BookingDto> result = json.write(bookingDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-12-26T12:00:00");
        assertThat(result).extractingJsonPathValue("$.requestId").isEqualTo(null);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }
}
