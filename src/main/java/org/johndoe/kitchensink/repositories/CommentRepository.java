package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
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
     * @param postId the ID of the post
     * @return a list of comments
     */
    List<Comment> findByPostId(String postId);

    /**
     * Finds all comments made by a specific user.
     *
     * @param member the comment member (Member)
     * @return a list of comments
     */
    List<Comment> findByMember(Member member);

    /**
     * Counts the number of comments made by a user.
     *
     * @param member the comment member (Member)
     * @return the number of comments
     */
    long countByMember(Member member);

    /**
     * Counts the number of comments on a post.
     *
     * @param postId the ID of the post
     * @return the number of comments
     */
    int countByPostId(String postId);
}
