package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Comment entity.
 */
@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    /**
     * Finds all comments on a specific post.
     *
     * @param post the post
     * @return a list of comments
     */
    List<Comment> findByPost(Post post);

    /**
     * Finds all comments made by a specific user.
     *
     * @param author the comment author (Member)
     * @return a list of comments
     */
    List<Comment> findByAuthor(Member author);

    /**
     * Counts the number of comments made by a user.
     *
     * @param author the comment author (Member)
     * @return the number of comments
     */
    long countByAuthor(Member author);

    /**
     * Counts the number of comments on a post.
     *
     * @param post the post
     * @return the number of comments
     */
    int countByPost(Post post);
}
