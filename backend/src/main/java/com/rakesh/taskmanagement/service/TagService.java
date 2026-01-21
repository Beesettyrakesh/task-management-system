package com.rakesh.taskmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rakesh.taskmanagement.entity.Tag;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final UserService userService;

    public Tag createTag(Tag tag) {
        User currentUser = userService.getCurrentUser();

        Optional<Tag> existingTag = tagRepository.findByNameAndUserId(tag.getName(), currentUser.getId());
        if(existingTag.isPresent()){
            throw new IllegalArgumentException("Tag with name '" + tag.getName() + "' already exists");
        }

        tag.setUser(currentUser);
        return tagRepository.save(tag);
    }

    public List<Tag> getAllTags(){
        User currentUser = userService.getCurrentUser();
        return tagRepository.findByUserId(currentUser.getId());
    }

    public Tag getTagById(Long id) {
        User currentUser = userService.getCurrentUser();
        Tag tag = tagRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        if(tag.getUser().getId() != currentUser.getId()){
            throw new ResourceNotFoundException("Tag not found");
        }
        return tag;
    }

    public Tag updateTag(Long id, Tag tag) {
        User currentUser = userService.getCurrentUser();
        Tag existingTag = tagRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        if(existingTag.getUser().getId() != currentUser.getId()) {
            throw new ResourceNotFoundException("Tag not found");
        }

        if(!existingTag.getName().equals(tag.getName())) {
            Optional<Tag> duplicateTag = tagRepository.findByNameAndUserId(tag.getName(), currentUser.getId());
            if(duplicateTag.isPresent()){
                throw new IllegalArgumentException("Tag with name '" + tag.getName() + "' already exists");
            }
        }

        existingTag.setName(tag.getName());
        existingTag.setColor(tag.getColor());
        return tagRepository.save(existingTag);
    }

    public void deleteTag(Long id) {
        User currentUser = userService.getCurrentUser();
        Tag tag = tagRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        if(tag.getUser().getId() != currentUser.getId()){
            throw new ResourceNotFoundException("Tag not found");
        }
        tagRepository.deleteById(id);
    }
}
