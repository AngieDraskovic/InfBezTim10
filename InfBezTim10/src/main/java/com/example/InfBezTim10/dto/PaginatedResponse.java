package com.example.InfBezTim10.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaginatedResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
}
