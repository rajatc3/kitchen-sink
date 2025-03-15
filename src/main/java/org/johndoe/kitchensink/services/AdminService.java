package org.johndoe.kitchensink.services;

import lombok.AllArgsConstructor;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminService {

    private MemberService memberService;
    private PostRepository postRepository;
    private CommentRepository commentRepository;

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

            List<Post> posts = postRepository.findByMember(member);
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
            topPostData.put("member", post.getMember().getUsername());
            topPostData.put("totalComments", topPostCommentCount);
            stats.put("topPost", topPostData);
        });

        return stats;
    }

}
