package org.johndoe.kitchensink.services;

import lombok.AllArgsConstructor;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.fromEntity;

@Service
@AllArgsConstructor
@EnableCaching
public class AdminService {

    private MemberService memberService;
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private KeycloakAuthService keycloakService;

    @Cacheable(value = "analytics", key = "'global'")
    public Map<String, Object> getAnalytics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = memberService.countUsers();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalPosts", totalPosts);
        stats.put("totalComments", totalComments);

        List<Map<String, Object>> memberStats = memberService.findAllMembersAsEntity().stream().map(member -> {
            Map<String, Object> memberData = new HashMap<>();
            memberData.put("username", member.getUsername());

            List<Post> posts = postRepository.findByMemberOrderByCreatedAtDesc(member);
            memberData.put("totalPosts", posts.size());

            List<Map<String, Object>> postStats = posts.stream().map(post -> {
                Map<String, Object> postData = new HashMap<>();
                postData.put("postTitle", post.getTitle());
                postData.put("postId", post.getId());
                int commentCount = commentRepository.countByPostId(post.getId());
                postData.put("totalComments", commentCount);
                return postData;
            }).collect(Collectors.toList());

            memberData.put("posts", postStats);
            return memberData;
        }).collect(Collectors.toList());

        stats.put("members", memberStats);

        Optional<Post> topPost = postRepository.findAll().stream()
                .max(Comparator.comparingInt(post -> commentRepository.countByPostId(post.getId())));

        topPost.ifPresent(post -> {
            long topPostCommentCount = commentRepository.countByPostId(post.getId());
            Map<String, Object> topPostData = new HashMap<>();
            topPostData.put("postTitle", post.getTitle());
            topPostData.put("postId", post.getPostId());
            topPostData.put("member", post.getMember().getFirstName() +" "+ post.getMember().getLastName());
            topPostData.put("totalComments", topPostCommentCount);
            stats.put("topPost", topPostData);
        });

        return stats;
    }

    /**
     * Assigns admin roles to a user.
     *
     * @param username the username of the user
     * @return the updated member DTO
     */
    public MemberDto assignAdminRoles(String username) {
        Member member = memberService.findMemberEntityByName(username);
        keycloakService.assignRoleToUser(fromEntity(member), ApplicationConstants.ROLES.ADMIN.name().toLowerCase());
        return memberService.assignAdminRoleToUser(member);
    }
}
