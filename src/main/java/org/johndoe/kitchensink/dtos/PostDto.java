package org.johndoe.kitchensink.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Post.
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDto {

    private String id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    private String content;

    private MemberDto member;
    private LocalDateTime createdAt;
    private List<CommentDto> comments; // Now storing full CommentDto instead of IDs

    public PostDto(String id, String title, String content, Member member, LocalDateTime createdAt, List<CommentDto> comments) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.member = MemberDto.Mapper.fromEntity(member, true);
        this.createdAt = createdAt;
        this.comments = comments;
    }

    /**
     * Mapper class for converting between PostDto and Post entity.
     */
    public static class Mapper {

        public static Post toEntity(PostDto dto, Member member, List<Comment> comments) {
            if (dto == null) {
                return null;
            }
            return new Post(dto.getId(), member, dto.getTitle(), dto.getContent(), comments.stream().map(Comment::getId).collect(Collectors.toList()));
        }

        public static PostDto fromEntity(Post post, List<Comment> comments) {
            if (post == null) {
                return null;
            }
            List<CommentDto> commentDtos = comments.stream().map(CommentDto.Mapper::fromEntity).collect(Collectors.toList());
            return new PostDto(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getMember(),
                    post.getCreatedAt(),
                    commentDtos
            );
        }
    }
}
