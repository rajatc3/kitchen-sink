package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Post entity.
 */
@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    /**
     * Finds posts containing a keyword in the title.
     *
     * @param keyword the keyword
     * @return a list of matching posts
     */
    List<Post> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Deletes a post by its ID and the member who created it.
     *
     * @param postId the ID of the post
     * @param member the member who created the post
     */
    void deleteByPostIdAndMember(String postId, Member member);

    /**
     * Deletes a post.
     *
     * @param postId the ID of the post
     */
    void deleteByPostId(String postId);

    /**
     * Finds a post by its ID.
     *
     * @param postId the ID of the post
     * @return the post
     */
    Optional<Post> findByPostId(String postId);

    /**
     *
     * @param member
     * @return list of posts for a given member
     */
    List<Post> findByMemberOrderByCreatedAtDesc(Member member);
}
