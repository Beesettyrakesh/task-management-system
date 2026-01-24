package com.rakesh.taskmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.rakesh.taskmanagement.dto.ErrorResponseDto;
import com.rakesh.taskmanagement.dto.TagResponseDto;
import com.rakesh.taskmanagement.entity.Tag;
import com.rakesh.taskmanagement.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag Management", description = "Tag CRUD operations for organizing tasks with labels and colors")
public class TagController {

    private final TagService tagService;

    @Operation(
            summary = "Create a new tag",
            description = "Create a new tag with name and color for organizing tasks"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tag created successfully",
                    content = @Content(schema = @Schema(implementation = TagResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid tag data or validation errors",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(@Valid @RequestBody Tag tag) {
        Tag createdTag = tagService.createTag(tag);
        TagResponseDto responseDto = TagResponseDto.from(createdTag);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
            summary = "Get all user tags",
            description = "Retrieve all tags belonging to the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TagResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagResponseDto> responseDtos = tags.stream()
            .map(TagResponseDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(
            summary = "Get tag by ID",
            description = "Retrieve a specific tag by its ID. User can only access their own tags."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag found and returned",
                    content = @Content(schema = @Schema(implementation = TagResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Tag not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTagById(
            @Parameter(description = "Tag ID", example = "1")
            @PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        TagResponseDto responseDto = TagResponseDto.from(tag);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Update an existing tag",
            description = "Update tag details including name and color"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag updated successfully",
                    content = @Content(schema = @Schema(implementation = TagResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid tag data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Tag not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(
            @Parameter(description = "Tag ID", example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody Tag tag) {
        Tag updatedTag = tagService.updateTag(id, tag);
        TagResponseDto responseDto = TagResponseDto.from(updatedTag);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Delete a tag",
            description = "Permanently delete a tag. User can only delete their own tags."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "Tag ID", example = "1")
            @PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

}
