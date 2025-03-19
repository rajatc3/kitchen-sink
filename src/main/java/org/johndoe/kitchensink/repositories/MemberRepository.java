package org.johndoe.kitchensink.repositories;

import org.johndoe.kitchensink.documents.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("{ 'email' : { $regex: ?0, $options: 'i' } }")
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
     * @param username the name
     * @return an Optional containing the found member, or empty if not found
     */
    @Query("{ 'username' : { $regex: ?0, $options: 'i' } }")
    Optional<Member> findByUsername(String username);

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

    /**
     * Finds a member by their email or username.
     *
     * @param identifier the email or username
     * @return an Optional containing the found member, or empty if not found
     */
    @Query(value = "{ $or: [ { 'email': ?0 }, { 'username': ?0 } ] }",
            collation = "{ 'locale': 'en', 'strength': 2 }")
    Optional<Member> findByEmailOrUsername(String identifier);

    /**
     * Finds a member by their email or phone number, excluding the given username.
     *
     * @param email       the email address
     * @param phoneNumber the phone number
     * @param username    the username
     * @return an Optional containing the found member, or empty if not found
     */
    @Query("{ '$and': [ { '$or': [ { 'email': { '$regex': ?0, '$options': 'i' } }, { 'phoneNumber': ?1 } ] }, { 'username': { '$not': { '$regex': ?2, '$options': 'i' } } } ] }")
    Optional<Member> findRecordsWithConflictingEmailOrPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber, @Param("username") String username);

}