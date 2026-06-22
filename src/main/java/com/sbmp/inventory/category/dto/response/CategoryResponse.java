package com.sbmp.inventory.category.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CategoryResponse {

    private Long id;

    private String name;

    private String description;

    private Boolean status;

    private LocalDateTime createdAt;
}