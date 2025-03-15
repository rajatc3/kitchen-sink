package org.johndoe.kitchensink.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;

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

    private MemberDto member; // Reference to Member
    private String postId; // Store only the post ID
    private LocalDateTime createdAt;

    public CommentDto(String id, String content, Member member, String postId, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.member = MemberDto.Mapper.fromEntity(member, true);
        this.postId = postId;
        this.createdAt = createdAt;
    }

    /**
     * Mapper class for converting between CommentDto and Comment entity.
     */
    public static class Mapper {

        public static Comment toEntity(CommentDto dto, Member member) {
            if (dto == null) {
                return null;
            }
            return new Comment(dto.getId(), member, dto.getPostId(), dto.getContent());
        }

        public static CommentDto fromEntity(Comment comment) {
            if (comment == null) {
                return null;
            }
            return new CommentDto(
                    comment.getId(),
                    comment.getContent(),
                    comment.getMember(),
                    comment.getPostId(), // Fetch only post ID
                    comment.getCreatedAt()
            );
        }

        public static CommentDto fromEntity(String id, String content, LocalDateTime createdAt) {
            return new CommentDto(
                    id, content, null, null, createdAt);
        }
    }
}
