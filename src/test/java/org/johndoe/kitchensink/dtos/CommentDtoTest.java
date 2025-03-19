package org.johndoe.kitchensink.dtos;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoTest {

    private CommentDto commentDto;
    private Member member;
    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "testuser", "John", "Doe", "john.doe@example.com", "9876543210", "USER");
        createdAt = LocalDateTime.now();
        commentDto = new CommentDto("123", "This is a test comment", member, "456", createdAt);
    }

    @Test
    void testCommentDtoConstructor() {
        assertNotNull(commentDto);
        assertEquals("123", commentDto.getId());
        assertEquals("This is a test comment", commentDto.getContent());
        assertNotNull(commentDto.getMember());
        assertEquals("456", commentDto.getPostId());
        assertEquals(createdAt, commentDto.getCreatedAt());
    }

    @Test
    void testToEntity() {
        Comment comment = CommentDto.Mapper.toEntity(commentDto, member);
        assertNotNull(comment);
        assertEquals(commentDto.getId(), comment.getId());
        assertEquals(commentDto.getContent(), comment.getContent());
        assertEquals(commentDto.getPostId(), comment.getPostId());
    }

    @Test
    void testToEntity_NullDto() {
        assertNull(CommentDto.Mapper.toEntity(null, member));
    }

    @Test
    void testFromEntity() {
        Comment comment = new Comment("123", member, "456", "This is a test comment");
        CommentDto dto = CommentDto.Mapper.fromEntity(comment);
        assertNotNull(dto);
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getContent(), dto.getContent());
        assertEquals(comment.getPostId(), dto.getPostId());
    }

    @Test
    void testFromEntity_NullComment() {
        assertNull(CommentDto.Mapper.fromEntity(null));
    }

    @Test
    void testFromEntity_WithoutMember() {
        CommentDto dto = CommentDto.Mapper.fromEntity("123", "Test content", createdAt);
        assertNotNull(dto);
        assertEquals("123", dto.getId());
        assertEquals("Test content", dto.getContent());
        assertNull(dto.getMember());
        assertNull(dto.getPostId());
        assertEquals(createdAt, dto.getCreatedAt());
    }
}
