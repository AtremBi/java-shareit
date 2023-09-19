package ru.practicum.shareit.UserTest.dto;

import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.Dto.UserDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class UserDtoTest {

    private JacksonTester<UserDto> json;
    private UserDto userDto;
    private Validator validator;

    public UserDtoTest(@Autowired JacksonTester<UserDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        userDto = new UserDto(
                1L,
                getRandomString(),
                getRandomEmail()
        );
    }

    private String getRandomEmail() {
        RandomString randomString = new RandomString();
        return randomString.nextString() + "@" + randomString.nextString() + ".ew";
    }

    private String getRandomString() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }

    @Test
    void jsonUserDtoTest() throws Exception {

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(userDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo(userDto.getEmail());
    }

    @Test
    void whenUserDtoEmailIsBlank_thenViolationsShouldBeReported_mustBeEmailFormat() {
        userDto.setEmail(" ");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("должно иметь формат адреса электронной почты");
    }

    @Test
    void whenUserDtoEmailIsNull_thenViolationsShouldBeReported_notNull() {
        userDto.setEmail(null);
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("не должно равняться null");
    }
}
