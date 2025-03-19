package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.documents.Comment;
import org.johndoe.kitchensink.documents.Member;
import org.springframework.data.domain.Sort;
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
     * @param postId    the ID of the post
     * @param createdAt sort comments by creation date
     * @return a list of comments
     */
    List<Comment> findByPostId(String postId, Sort createdAt);

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

    /**
     * Deletes a comment by its ID and the member who created it.
     *
     * @param commentId the ID of the post
     * @param member    the member who created the post
     */
    void deleteByCommentIdAndMember(String commentId, Member member);

    /**
     * Deletes a comment by its ID.
     *
     * @param commentId the ID of the post
     */
    void deleteByCommentId(String commentId);
}
