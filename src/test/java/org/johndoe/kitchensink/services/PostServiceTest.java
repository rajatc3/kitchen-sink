package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.dtos.CommentDto;
import org.johndoe.kitchensink.dtos.PostDto;
import org.johndoe.kitchensink.exceptions.ApplicationException;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private Member testMember;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testMember = new Member(1L, "john.doe", "John", "Doe", "john.doe@email.com", "9876543210", "USER");
        testPost = new Post("1", testMember, "Test Title", "Test Content", List.of());
        testComment = new Comment("1", testMember, "1", "Test Comment");
    }

    @Test
    void getAllPosts_ShouldReturnPostDtos() {
        Page<Post> postPage = new PageImpl<>(List.of(testPost));
        when(postRepository.findAll(any(Pageable.class))).thenReturn(postPage);
        when(commentRepository.findByPostId(any(), any())).thenReturn(List.of(testComment));

        Page<PostDto> result = postService.getAllPosts(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
        assertEquals("Test Title", result.getContent().get(0).getTitle());
        verify(postRepository).findAll(any(Pageable.class));
    }

    @Test
    void getPostById_ShouldReturnPostDto() {
        when(postRepository.findByPostId("1")).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostId(anyString(), any())).thenReturn(List.of(testComment));

        Optional<PostDto> result = postService.getPostById("1");

        assertTrue(result.isPresent());
        assertEquals("Test Title", result.get().getTitle());
        verify(postRepository).findByPostId("1");
    }

    @Test
    void createPost_ShouldReturnCreatedPost() {
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(testMember));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostDto result = postService.createPost("john.doe", "Test Title", "Test Content");

        assertEquals("Test Title", result.getTitle());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void addComment_ShouldReturnCommentDto() {
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(testMember));
        when(postRepository.findByPostId("1")).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentDto result = postService.addComment("1", "john.doe", "Test Comment");

        assertEquals("Test Comment", result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_ShouldThrowApplicationException_WhenPostNotFound() {
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(testMember));
        when(postRepository.findByPostId("1")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> postService.addComment("1", "john.doe", "Test Comment"));
    }

    @Test
    void deletePost_ShouldDelete_WhenAdmin() {
        testMember.setUserRole(ApplicationConstants.ROLES.ADMIN.name());
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(testMember));

        postService.deletePost("john.doe", "1");

        verify(postRepository).deleteByPostId("1");
    }

    @Test
    void deletePost_ShouldDelete_WhenUserIsOwner() {
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(testMember));

        postService.deletePost("john.doe", "1");

        verify(postRepository).deleteByPostIdAndMember("1", testMember);
    }

    @Test
    void deleteComment_ShouldDelete_WhenAdmin() {
        testMember.setUserRole(ApplicationConstants.ROLES.ADMIN.name());
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(testMember));

        postService.deleteComment("john.doe", "1");

        verify(commentRepository).deleteByCommentId("1");
    }

    @Test
    void deleteComment_ShouldDelete_WhenUserIsOwner() {
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(testMember));

        postService.deleteComment("john.doe", "1");

        verify(commentRepository).deleteByCommentIdAndMember("1", testMember);
    }
}