package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.fromEntity;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private KeycloakAuthService keycloakService;

    @InjectMocks
    private AdminService adminService;

    private Member testMember;
    private Post post1, post2;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setUsername("john_doe");
        testMember.setFirstName("John");
        testMember.setLastName("Doe");

        post1 = new Post();
        post1.setPostId("post1");
        post1.setTitle("First Post");
        post1.setMember(testMember);

        post2 = new Post();
        post2.setPostId("post2");
        post2.setTitle("Second Post");
        post2.setMember(testMember);
    }

    @Test
    void testGetAnalytics() {
        when(memberService.countUsers()).thenReturn(10L);
        when(postRepository.count()).thenReturn(20L);
        when(commentRepository.count()).thenReturn(50L);

        when(memberService.findAllMembersAsEntity()).thenReturn(List.of(testMember));
        when(postRepository.findByMemberOrderByCreatedAtDesc(testMember)).thenReturn(List.of(post1, post2));

        when(commentRepository.countByPostId("post1")).thenReturn(5);
        when(commentRepository.countByPostId("post2")).thenReturn(10);
        when(postRepository.findAll()).thenReturn(List.of(post1, post2));

        Map<String, Object> analytics = adminService.getAnalytics();

        assertThat(analytics).containsKeys("totalUsers", "totalPosts", "totalComments", "members", "topPost");
        assertThat(analytics.get("totalUsers")).isEqualTo(10L);
        assertThat(analytics.get("totalPosts")).isEqualTo(20L);
        assertThat(analytics.get("totalComments")).isEqualTo(50L);

        Map<String, Object> topPost = (Map<String, Object>) analytics.get("topPost");
        assertThat(topPost).containsEntry("postId", "post2");
    }

    @Test
    void testGetAnalytics_NoUsersFound() {
        when(memberService.countUsers()).thenReturn(0L);
        when(postRepository.count()).thenReturn(0L);
        when(commentRepository.count()).thenReturn(0L);

        when(memberService.findAllMembersAsEntity()).thenReturn(Collections.emptyList());
        when(postRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> analytics = adminService.getAnalytics();

        assertThat(analytics).containsKeys("totalUsers", "totalPosts", "totalComments", "members");
        assertThat(analytics.get("totalUsers")).isEqualTo(0L);
        assertThat(analytics.get("totalPosts")).isEqualTo(0L);
        assertThat(analytics.get("totalComments")).isEqualTo(0L);
        assertThat((List<?>) analytics.get("members")).isEmpty();
        assertThat(analytics).doesNotContainKey("topPost");
    }

    @Test
    void testGetAnalytics_NoPostsFound() {
        when(memberService.countUsers()).thenReturn(5L);
        when(postRepository.count()).thenReturn(0L);
        when(commentRepository.count()).thenReturn(0L);

        when(memberService.findAllMembersAsEntity()).thenReturn(List.of(testMember));
        when(postRepository.findByMemberOrderByCreatedAtDesc(testMember)).thenReturn(Collections.emptyList());
        when(postRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> analytics = adminService.getAnalytics();

        assertThat(analytics).containsKeys("totalUsers", "totalPosts", "totalComments", "members");
        assertThat(analytics.get("totalUsers")).isEqualTo(5L);
        assertThat(analytics.get("totalPosts")).isEqualTo(0L);
        assertThat(analytics.get("totalComments")).isEqualTo(0L);
        assertThat((List<?>) analytics.get("members")).isNotEmpty();
        assertThat(analytics).doesNotContainKey("topPost");
    }

    @Test
    void testAssignAdminRoles() {
        when(memberService.findMemberEntityByName("john_doe")).thenReturn(testMember);
        when(memberService.assignAdminRoleToUser(testMember)).thenReturn(fromEntity(testMember));

        MemberDto updatedMember = adminService.assignAdminRoles("john_doe");

        verify(keycloakService).assignRoleToUser(fromEntity(testMember), ApplicationConstants.ROLES.ADMIN.name().toLowerCase());
        verify(memberService).assignAdminRoleToUser(testMember);

        assertThat(updatedMember).isNotNull();
        assertThat(updatedMember.getUsername()).isEqualTo("john_doe");
    }

    @Test
    void testAssignAdminRoles_UserNotFound() {
        when(memberService.findMemberEntityByName("unknown_user")).thenThrow(new UserNotFoundException("User not found"));

        assertThatThrownBy(() -> adminService.assignAdminRoles("unknown_user"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(keycloakService, never()).assignRoleToUser(any(), any());
        verify(memberService, never()).assignAdminRoleToUser(any());
    }
}
