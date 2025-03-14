package org.johndoe.kitchensink.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Comment.
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDto {

    private String id;

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    private Long memberId; // Reference to Member
    private String postId; // Reference to Post
    private LocalDateTime createdAt;

    public CommentDto(String id, String content, Long memberId, String postId, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.memberId = memberId;
        this.postId = postId;
        this.createdAt = createdAt;
    }

    /**
     * Mapper class for converting between CommentDto and Comment entity.
     */
    public static class Mapper {

        public static Comment toEntity(CommentDto dto, Member author, Post post) {
            if (dto == null) {
                return null;
            }
            return new Comment(dto.getId(), author, post, dto.getContent());
        }

        public static CommentDto fromEntity(Comment comment) {
            if (comment == null) {
                return null;
            }
            return new CommentDto(
                    comment.getId(),
                    comment.getContent(),
                    comment.getAuthor().getMemberId(),
                    comment.getPost().getId(),
                    comment.getCreatedAt()
            );
        }
    }
}
