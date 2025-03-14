package org.johndoe.kitchensink.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Comment class represents a comment on a post.
 */
@Data
@Document(collection = "comments")
@EqualsAndHashCode(callSuper = false)
public class Comment extends BaseDocument {

    /**
     * The unique identifier for the comment.
     */
    @Id
    String commentId;

    /**
     * The member who created the comment.
     */
    @DBRef
    @Field("author")
    private Member author;

    /**
     * The post to which this comment belongs.
     */
    @DBRef
    @Field("post")
    private Post post;

    /**
     * The content of the comment.
     */
    @Field("content")
    private String content;

    /**
     * Default constructor for Comment.
     */
    public Comment() {
    }

    /**
     * Constructs a Comment with the given attributes.
     *
     * @param commentId the unique identifier for the comment
     * @param author    the member who created the comment
     * @param post      the post to which this comment belongs
     * @param content   the content of the comment
     */
    public Comment(String commentId, Member author, Post post, String content) {
        this.commentId = commentId;
        this.author = author;
        this.post = post;
        this.content = content;
    }

    /**
     * Returns the unique identifier for the comment.
     *
     * @return the unique identifier for the comment
     */
    public String getId() {
        return commentId;
    }
}
