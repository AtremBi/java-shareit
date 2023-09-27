package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingInputDtoTest {
    private JacksonTester<BookingInputDto> json;
    private BookingInputDto bookingInputDto;

    public BookingInputDtoTest(@Autowired JacksonTester<BookingInputDto> json) {
        this.json = json;
    }

    @BeforeEach
    void beforeEach() {
        bookingInputDto = new BookingInputDto(
                1L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0)
        );
    }

    @Test
    void testJsonBookingInputDto() throws Exception {
        JsonContent<BookingInputDto> result = json.write(bookingInputDto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-12-26T12:00:00");
    }
}
