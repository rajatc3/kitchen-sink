package org.johndoe.kitchensink.controllers;

import jakarta.validation.Valid;
import org.johndoe.kitchensink.dtos.CommentDto;
import org.johndoe.kitchensink.dtos.PostDto;
import org.johndoe.kitchensink.security.config.JwtAuthConverter;
import org.johndoe.kitchensink.services.PostService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

import static org.johndoe.kitchensink.utils.UtilityMethods.paginateResponse;

@RestController
@RequestMapping("/api/posts")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class PostsController {

    private final PostService postService;

    public PostsController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(@PageableDefault(size = 10) Pageable pageInput) {
        return ResponseEntity.ok(paginateResponse(postService.getAllPosts(pageInput)));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(Principal principal, @Valid @RequestBody PostDto postDto) {
        return ResponseEntity.ok(postService.createPost(JwtAuthConverter.getUsernameFromPrincipal(principal), postDto.getTitle(), postDto.getContent()));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(Principal principal, @PathVariable("postId") String postId) {
        postService.deletePost(JwtAuthConverter.getUsernameFromPrincipal(principal), postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(Principal principal, @PathVariable("commentId") String commentId) {
        postService.deleteComment(JwtAuthConverter.getUsernameFromPrincipal(principal), commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{postId}/comments")
    public ResponseEntity<CommentDto> addComment(Principal principal, @PathVariable("postId") String postId, @RequestBody CommentDto commentDto) {
        return ResponseEntity.ok(postService.addComment(postId, JwtAuthConverter.getUsernameFromPrincipal(principal), commentDto.getContent()));
    }

}
