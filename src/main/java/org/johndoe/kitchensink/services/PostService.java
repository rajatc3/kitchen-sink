package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.johndoe.kitchensink.dtos.CommentDto;
import org.johndoe.kitchensink.dtos.PostDto;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing posts.
 */
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
    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream().map(post -> {
            List<CommentDto> comments = commentRepository.findByPostId(post.getId())
                    .stream().map(CommentDto.Mapper::fromEntity).collect(Collectors.toList());
            return new PostDto(post.getId(), post.getTitle(), post.getContent(), post.getMember(), post.getCreatedAt(), comments);
        }).collect(Collectors.toList());
    }

    /**
     * Fetches a single post by its ID.
     *
     * @param postId the unique identifier of the post
     * @return Optional containing the post if found, else empty
     */
    public Optional<PostDto> getPostById(String postId) {
        return postRepository.findById(postId).map(post -> {
            List<CommentDto> comments = commentRepository.findByPostId(postId)
                    .stream().map(CommentDto.Mapper::fromEntity).collect(Collectors.toList());
            return new PostDto(post.getId(), post.getTitle(), post.getContent(), post.getMember(), post.getCreatedAt(), comments);
        });
    }

    /**
     * Creates a new post.
     *
     * @param memberId  the memberId of the person creating the post
     * @param title   the title of the post
     * @param content the content of the post
     * @return the created post
     */
    public PostDto createPost(Long memberId, String title, String content) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));
        Post post = new Post(null, member, title, content, new ArrayList<>());
        return PostDto.Mapper.fromEntity(postRepository.save(post), new ArrayList<>());
    }

    /**
     * Adds a comment to an existing post and fetches updated comments.
     *
     * @param postId  the ID of the post
     * @param member  the member of the comment
     * @param content the content of the comment
     * @return the updated PostDto with all comments
     */
    @Transactional
    public Optional<PostDto> addComment(String postId, Member member, String content) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return Optional.empty();
        }
        Post post = postOpt.get();
        Comment comment = new Comment(null, member, post.getPostId(), content);
        commentRepository.save(comment);

        // Fetch updated comments
        List<CommentDto> updatedComments = commentRepository.findByPostId(postId)
                .stream().map(CommentDto.Mapper::fromEntity).collect(Collectors.toList());

        return Optional.of(new PostDto(post.getId(), post.getTitle(), post.getContent(), post.getMember(), post.getCreatedAt(), updatedComments));
    }
}