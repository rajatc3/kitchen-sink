package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.documents.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Member entities.
 */
@Repository
public interface MemberRepository extends MongoRepository<Member, Long> {

    /**
     * Finds a member by their member ID.
     *
     * @param memberId the member ID
     * @return an Optional containing the found member, or empty if not found
     */
    Optional<Member> findByMemberId(Long memberId);

    /**
     * Finds the member with the highest member ID.
     *
     * @return an Optional containing the found member, or empty if not found
     */
    Optional<Member> findTopByOrderByMemberIdDesc();

    /**
     * Finds a member by their email.
     *
     * @param email the email address
     * @return an Optional containing the found member, or empty if not found
     */
    Optional<Member> findByEmail(String email);

    /**
     * Finds a member by their phone number.
     *
     * @param phoneNumber the phone number
     * @return an Optional containing the found member, or empty if not found
     */
    Optional<Member> findByPhoneNumber(String phoneNumber);

    /**
     * Finds a member by their name.
     *
     * @param name the name
     * @return an Optional containing the found member, or empty if not found
     */
    Optional<Member> findByName(String name);

    /**
     * Deletes a member by their member ID.
     *
     * @param id the member ID
     */
    void deleteByMemberId(Long id);

    /**
     * Finds a member by their email or phone number, excluding the given member ID.
     *
     * @param email       the email address
     * @param phoneNumber the phone number
     * @param memberId    the member ID to exclude
     * @return an Optional containing the found member, or empty if not found
     */
    @Query("{ $or: [ { 'email': ?0 }, { 'phoneNumber': ?1 } ], 'member_id': { $ne: ?2 } }")
    Optional<Member> findByEmailOrPhoneNumberAndIdNot(String email, String phoneNumber, Long memberId);
}