package org.johndoe.kitchensink.dtos;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostDtoTest {

    private PostDto postDto;
    private Member member;
    private LocalDateTime createdAt;
    private List<CommentDto> comments;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "testuser", "John", "Doe", "john.doe@example.com", "9876543210", "USER");
        createdAt = LocalDateTime.now();
        comments = Collections.singletonList(new CommentDto("789", "Sample comment", member, "123", createdAt));
        postDto = new PostDto("123", "Test Title", "Test Content", member, createdAt, comments);
    }

    @Test
    void testPostDtoConstructor() {
        assertNotNull(postDto);
        assertEquals("123", postDto.getId());
        assertEquals("Test Title", postDto.getTitle());
        assertEquals("Test Content", postDto.getContent());
        assertNotNull(postDto.getMember());
        assertEquals(createdAt, postDto.getCreatedAt());
        assertEquals(1, postDto.getComments().size());
    }

    @Test
    void testToEntity() {
        Post post = PostDto.Mapper.toEntity(postDto, member, Collections.emptyList());
        assertNotNull(post);
        assertEquals(postDto.getId(), post.getId());
        assertEquals(postDto.getTitle(), post.getTitle());
        assertEquals(postDto.getContent(), post.getContent());
        assertNotNull(post.getMember());
        assertTrue(post.getCommentIds().isEmpty());
    }

    @Test
    void testToEntity_NullDto() {
        assertNull(PostDto.Mapper.toEntity(null, member, Collections.emptyList()));
    }

    @Test
    void testFromEntity() {
        Comment comment = new Comment("789", member, "123", "Sample comment");
        Post post = new Post("123", member, "Test Title", "Test Content", List.of(comment.getId()));
        PostDto dto = PostDto.Mapper.fromEntity(post, List.of(comment));
        assertNotNull(dto);
        assertEquals(post.getId(), dto.getId());
        assertEquals(post.getTitle(), dto.getTitle());
        assertEquals(post.getContent(), dto.getContent());
        assertNotNull(dto.getMember());
        assertEquals(1, dto.getComments().size());
    }

    @Test
    void testFromEntity_NullPost() {
        assertNull(PostDto.Mapper.fromEntity(null, Collections.emptyList()));
    }
}
