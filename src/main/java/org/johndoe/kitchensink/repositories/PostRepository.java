package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.documents.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Post entity.
 */
@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    /**
     * Finds all posts by a specific member.
     *
     * @param member the member (Member)
     * @return a list of posts
     */
    List<Post> findByMember(Member member);

    /**
     * Finds posts containing a keyword in the title.
     *
     * @param keyword the keyword
     * @return a list of matching posts
     */
    List<Post> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Counts the number of posts made by a user.
     *
     * @param member the member (Member)
     * @return the number of posts
     */
    @Query(value = "{ 'member.memberId' : ?0 }", count = true)
    long countByAuthor(Long memberId);
}
