package org.johndoe.kitchensink.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * Post class represents a user-created post with associated metadata.
 */
@Data
@Document(collection = "posts")
@EqualsAndHashCode(callSuper = false)
public class Post extends BaseDocument {

    /**
     * The unique identifier for the post.
     */
    @Id
    String postId;

    /**
     * The member who created the post.
     */
    @DBRef
    @Field("author")
    private Member author;

    /**
     * The title of the post.
     */
    @Field("title")
    private String title;

    /**
     * The content of the post.
     */
    @Field("content")
    private String content;

    /**
     * The list of comments associated with this post.
     */
    @DBRef
    @Field("comments")
    private List<Comment> comments;

    /**
     * Default constructor for Post.
     */
    public Post() {
    }

    /**
     * Constructs a Post with the given attributes.
     *
     * @param postId  the unique identifier for the post
     * @param author  the member who created the post
     * @param title   the title of the post
     * @param content the content of the post
     */
    public Post(String postId, Member author, String title, String content) {
        this.postId = postId;
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public String getId() {
        return postId;
    }
}
