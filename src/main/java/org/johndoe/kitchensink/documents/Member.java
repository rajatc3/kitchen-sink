/**
 * Represents a member document in the MongoDB collection.
 */
package org.johndoe.kitchensink.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Member class extends BaseDocument and represents a member entity with various attributes.
 */
@Document(collection = "members")
public class Member extends BaseDocument {
    /**
     * The unique identifier for the member.
     */
    @Field("member_id")
    Long memberId;
    /**
     * The name of the member.
     */
    @Field("name")
    String name;
    /**
     * The email address of the member.
     */
    @Field("email")
    String email;
    /**
     * The phone number of the member.
     */
    @Field("phone_number")
    String phoneNumber;
    /**
     * The MongoDB document ID.
     */
    @Id
    private String id;

    /**
     * Default constructor for Member.
     */
    public Member() {
    }

    /**
     * Constructs a Member with the specified details.
     *
     * @param memberId    the unique identifier for the member
     * @param name        the name of the member
     * @param email       the email address of the member
     * @param phoneNumber the phone number of the member
     */
    public Member(Long memberId, String name, String email, String phoneNumber) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the MongoDB document ID.
     *
     * @return the document ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB document ID.
     *
     * @param id the document ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the unique identifier for the member.
     *
     * @return the member ID
     */
    public Long getMemberId() {
        return memberId;
    }

    /**
     * Sets the unique identifier for the member.
     *
     * @param memberId the member ID to set
     */
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    /**
     * Gets the name of the member.
     *
     * @return the name of the member
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the member.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the member.
     *
     * @return the email address of the member
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the member.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the member.
     *
     * @return the phone number of the member
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the member.
     *
     * @param phoneNumber the phone number to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}