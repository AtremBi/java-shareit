package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ShortBookingDtoTest {
    private JacksonTester<ShortBookingDto> json;
    private ShortBookingDto bookingShortDto;
    private Validator validator;

    public ShortBookingDtoTest(@Autowired JacksonTester<ShortBookingDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        bookingShortDto = new ShortBookingDto(
                1L, 2L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0)
        );
    }

    @Test
    void testJsonBookingShortDto() throws Exception {
        JsonContent<ShortBookingDto> result = json.write(bookingShortDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-12-26T12:00:00");
    }
}
