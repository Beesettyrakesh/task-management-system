package com.rakesh.taskmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

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

import com.rakesh.taskmanagement.dto.TagResponseDto;
import com.rakesh.taskmanagement.entity.Tag;
import com.rakesh.taskmanagement.service.TagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(@Valid @RequestBody Tag tag) {
        Tag createdTag = tagService.createTag(tag);
        TagResponseDto responseDto = TagResponseDto.from(createdTag);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagResponseDto> responseDtos = tags.stream()
            .map(TagResponseDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        TagResponseDto responseDto = TagResponseDto.from(tag);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(@PathVariable Long id, @Valid @RequestBody Tag tag) {
        Tag updatedTag = tagService.updateTag(id, tag);
        TagResponseDto responseDto = TagResponseDto.from(updatedTag);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

}
