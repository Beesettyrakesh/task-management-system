package com.rakesh.taskmanagement.dto;

import java.time.LocalDateTime;

import com.rakesh.taskmanagement.entity.Tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagResponseDto {
    private Long id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TagResponseDto from(Tag tag) {
        return new TagResponseDto(
            tag.getId(),
            tag.getName(),
            tag.getColor(),
            tag.getCreatedAt(),
            tag.getUpdatedAt()
        );
    }
}
