package ru.practicum.shareit.exeptions;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.LocalDateTime;

@Data
@Builder
@Value
public class ResponseError {
    public LocalDateTime timestamp;
    public String error;
    public String exception;
    public int status;
    public String message;
}