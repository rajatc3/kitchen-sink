/**
 * Represents a member document in the MongoDB collection.
 */
package org.johndoe.kitchensink.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Member class extends BaseDocument and represents a member entity with various attributes.
 */
@Data
@Document(collection = "members")
@EqualsAndHashCode(callSuper = false)
public class Member extends BaseDocument {

    /**
     * The unique identifier for the member.
     */
    @Field("member_id")
    Long memberId;

    /**
     * The username of the member.
     */
    @Field("username")
    String username;

    /**
     * The first name of the member.
     */
    @Field("first_name")
    String firstName;

    /**
     * The last name of the member.
     */
    @Field("last_name")
    String lastName;

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

    @Field("user_role")
    String userRole;

    /**
     * The MongoDB document ID.
     */
    @Id
    public String id;

    /**
     * Default constructor for Member.
     */
    public Member() {
    }

    /**
     * Constructs a Member with the given attributes.
     *
     * @param memberId    the unique identifier for the member
     * @param username    the username of the member
     * @param firstName   the first name of the member
     * @param lastName    the last name of the member
     * @param email       the email address of the member
     * @param phoneNumber the phone number of the member
     * @param userRole    the role of the member
     */
    public Member(Long memberId, String username, String firstName, String lastName, String email, String phoneNumber, String userRole) {
        this.memberId = memberId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
    }
}