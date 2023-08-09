package ru.practicum.shareit.exeptions;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
@Value
public class ResponseError {
    public String error;
    public String exception;
    public int status;
}