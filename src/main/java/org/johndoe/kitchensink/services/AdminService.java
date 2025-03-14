package org.johndoe.kitchensink.services;

import lombok.AllArgsConstructor;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.dtos.CommentDto;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.dtos.PostDto;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.toEntity;

@Service
@AllArgsConstructor
public class AdminService {

    private MemberService memberService;
    private PostRepository postRepository;
    private CommentRepository commentRepository;

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total counts
        long totalUsers = memberService.countUsers();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalPosts", totalPosts);
        stats.put("totalComments", totalComments);

        // Per-Member Breakdown
        List<Map<String, Object>> memberStats = memberService.findAllMembersAsEntity().stream().map(member -> {
            Map<String, Object> memberData = new HashMap<>();
            memberData.put("username", member.getUsername());

            // Get all posts by this member
            List<Post> posts = postRepository.findByAuthor(member).stream().collect(Collectors.toList());
            memberData.put("totalPosts", posts.size());

            // Per-Post Breakdown
            List<Map<String, Object>> postStats = posts.stream().map(post -> {
                Map<String, Object> postData = new HashMap<>();
                postData.put("postTitle", post.getTitle());
                postData.put("postId", post.getId());

                // Get all comments for this post
                List<Comment> comments = commentRepository.findByPost(post).stream().collect(Collectors.toList());
                postData.put("totalComments", comments.size());

                return postData;
            }).collect(Collectors.toList());

            memberData.put("posts", postStats);
            return memberData;
        }).collect(Collectors.toList());

        stats.put("members", memberStats);

        // Top Post Analytics (Post with Most Comments)
        Optional<Post> topPost = postRepository.findAll().stream()
                .max(Comparator.comparingInt(post -> commentRepository.findByPost(post).size()));

        if (topPost.isPresent()) {
            Post post = topPost.get();
            long topPostCommentCount = commentRepository.findByPost(post).size();

            Map<String, Object> topPostData = new HashMap<>();
            topPostData.put("postTitle", post.getTitle());
            topPostData.put("postId", post.getPostId());
            topPostData.put("author", post.getAuthor().getUsername());
            topPostData.put("totalComments", topPostCommentCount);

            stats.put("topPost", topPostData);
        }

        return stats;
    }

}
