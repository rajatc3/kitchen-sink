package org.johndoe.kitchensink.controllers;

import org.johndoe.kitchensink.config.CorsConfig;
import org.johndoe.kitchensink.dtos.CommentDto;
import org.johndoe.kitchensink.dtos.PostDto;
import org.johndoe.kitchensink.security.SecurityConfig;
import org.johndoe.kitchensink.services.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostsController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class) // Ensure your security configuration is imported
class PostsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CorsConfig corsConfig;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
    void testGetAllPosts() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<PostDto> page = new PageImpl<>(Collections.singletonList(new PostDto("1", "Test Title", "Test Content", null, LocalDateTime.now(), Collections.emptyList())), pageable, 1);

        when(postService.getAllPosts(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/posts").param("page", "0").param("size", "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());

        verify(postService, times(1)).getAllPosts(any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
    void testCreatePost() throws Exception {
        Principal principal = () -> "testuser";
        PostDto postDto = new PostDto("1", "Test Title", "Test Content", null, null, null);

        when(postService.createPost(eq("testuser"), any(), any())).thenReturn(postDto);

        mockMvc.perform(post("/api/posts").principal(principal).contentType(MediaType.APPLICATION_JSON).content("{\"title\": \"Test Title\", \"content\": \"Test Content\"}")).andExpect(status().isOk());

        verify(postService, times(1)).createPost(eq(null), eq("Test Title"), eq("Test Content"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
    void testDeletePost() throws Exception {
        Principal principal = () -> "testuser";

        doNothing().when(postService).deletePost(eq("testuser"), eq("1"));

        mockMvc.perform(delete("/api/posts/1").principal(principal)).andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(eq(null), eq("1"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
    void testDeleteComment() throws Exception {
        Principal principal = () -> "testuser";

        doNothing().when(postService).deleteComment(eq("testuser"), eq("1"));

        mockMvc.perform(delete("/api/posts/comments/1").principal(principal)).andExpect(status().isNoContent()).andExpect(content().string(""));

        verify(postService, times(1)).deleteComment(eq(null), eq("1"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
    void testAddComment() throws Exception {
        Principal principal = () -> "testuser";
        CommentDto commentDto = new CommentDto("1", "Test Comment", null, "postId", LocalDateTime.now());

        when(postService.addComment(eq("1"), any(), eq("Test Comment"))).thenReturn(commentDto);

        mockMvc.perform(post("/api/posts/1/comments").principal(principal).contentType(MediaType.APPLICATION_JSON).content("{\"content\": \"Test Comment\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.content").value("Test Comment"));

        verify(postService, times(1)).addComment(eq("1"), any(), eq("Test Comment"));
    }
}