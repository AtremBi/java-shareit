package ru.practicum.shareit.Pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class Pagination extends PageRequest {

    protected Pagination(int from, int size, Sort sort) {
        super(from / size, size, sort);
    }

    public static PageRequest of(int from, int size, Sort.Direction direction, String... properties) {
        return of(from / size, size, Sort.by(direction, properties));
    }
}
