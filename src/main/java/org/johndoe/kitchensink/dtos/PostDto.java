package org.johndoe.kitchensink.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Post.
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDto {

    private String id;
    private String title;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    private Long memberId;
    private LocalDateTime createdAt;

    public PostDto(String id, String title, String content, Long memberId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.memberId = memberId;
        this.createdAt = createdAt;
    }

    /**
     * Mapper class for converting between PostDto and Post entity.
     */
    public static class Mapper {

        public static Post toEntity(PostDto dto, Member author) {
            if (dto == null) {
                return null;
            }
            return new Post(dto.getId(), author, dto.getTitle(), dto.getContent());
        }

        public static PostDto fromEntity(Post post) {
            if (post == null) {
                return null;
            }
            return new PostDto(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getAuthor().getMemberId(),
                    post.getCreatedAt()
            );
        }
    }
}
