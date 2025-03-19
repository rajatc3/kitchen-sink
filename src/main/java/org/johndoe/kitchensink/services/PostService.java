package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.dtos.CommentDto;
import org.johndoe.kitchensink.dtos.PostDto;
import org.johndoe.kitchensink.exceptions.ApplicationException;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing posts.
 */
@EnableCaching
@Service
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostService(MemberRepository memberRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Fetches all posts along with their associated comments.
     *
     * @return List of PostDto
     */
    public Page<PostDto> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(post -> {
                    List<CommentDto> comments = commentRepository.findByPostId(post.getId(), Sort.by(Sort.Direction.ASC, "createdAt"))
                            .stream()
                            .map(CommentDto.Mapper::fromEntity)
                            .collect(Collectors.toList());

                    return new PostDto(post.getId(), post.getTitle(), post.getContent(), post.getMember(), post.getCreatedAt(), comments);
                });
    }

    /**
     * Fetches a single post by its ID.
     *
     * @param postId the unique identifier of the post
     * @return Optional containing the post if found, else empty
     */
    @Cacheable(value = "posts", key = "#postId")
    public Optional<PostDto> getPostById(String postId) {
        return postRepository.findByPostId(postId).map(post -> {
            List<CommentDto> comments = commentRepository.findByPostId(postId, Sort.by(Sort.Direction.DESC, "createdAt"))
                    .stream().map(CommentDto.Mapper::fromEntity).collect(Collectors.toList());
            return new PostDto(post.getId(), post.getTitle(), post.getContent(), post.getMember(), post.getCreatedAt(), comments);
        });
    }

    /**
     * Creates a new post.
     *
     * @param userName  the identifier of the person creating the post
     * @param title   the title of the post
     * @param content the content of the post
     * @return the created post
     */
    @CachePut(value = "posts", key = "#result.id")
    public PostDto createPost(String userName, String title, String content) {
        Member member = memberRepository.findByUsername(userName).orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));
        Post post = new Post(null, member, title, content, new ArrayList<>());
        return PostDto.Mapper.fromEntity(postRepository.save(post), new ArrayList<>());
    }

    /**
     * Adds a comment to a post.
     *
     * @param postId  the ID of the post
     * @param userName the username of the person adding the comment
     * @param content the content of the comment
     * @return the updated post
     */
    @CacheEvict(value = "posts", key = "#postId")
    @Transactional
    public CommentDto addComment(String postId, String userName, String content) {
        Member member = memberRepository.findByUsername(userName).orElseThrow(() -> new UserNotFoundException("Invalid member ID"));
        Optional<Post> postOpt = postRepository.findByPostId(postId);
        if (postOpt.isEmpty()) {
            throw new ApplicationException("Post not found");
        }
        Post post = postOpt.get();
        Comment comment = new Comment(null, member, post.getPostId(), content);
        return CommentDto.Mapper.fromEntity(commentRepository.save(comment));
    }

    /**
     * Deletes a post by its ID.
     *
     * @param userName the username of the person deleting the post
     * @param postId   the ID of the post
     */
    @CacheEvict(value = "posts", key = "#postId")
    public void deletePost(String userName, String postId) {
        Member member = memberRepository.findByUsername(userName).orElseThrow(() -> new UserNotFoundException("Invalid member ID"));
        if (member.getUserRole().equalsIgnoreCase(ApplicationConstants.ROLES.ADMIN.name())) {
            postRepository.deleteByPostId(postId);
        } else postRepository.deleteByPostIdAndMember(postId, member);
    }

    /**
     * Deletes a comment by its ID.
     *
     * @param userName  the username of the person deleting the comment
     * @param commentId the ID of the comment
     */
    public void deleteComment(String userName, String commentId) {
        Member member = memberRepository.findByUsername(userName).orElseThrow(() -> new UserNotFoundException("Invalid member ID"));
        if (member.getUserRole().equalsIgnoreCase(ApplicationConstants.ROLES.ADMIN.name())) {
            commentRepository.deleteByCommentId(commentId);
        } else commentRepository.deleteByCommentIdAndMember(commentId, member);
    }
}