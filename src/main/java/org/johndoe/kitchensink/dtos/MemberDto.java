package org.johndoe.kitchensink.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.johndoe.kitchensink.documents.Member;

/**
 * Data Transfer Object for Member.
 */
public class MemberDto {

    /**
     * The unique identifier for the member.
     */
    private Long memberId;

    /**
     * The name of the member.
     * Must be between 3 and 50 characters.
     */
    @NotBlank(message = "Name is mandatory")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    /**
     * The email address of the member.
     * Must be a valid email format.
     */
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Email should be valid"
    )
    @NotBlank(message = "Email is mandatory")
    private String email;

    /**
     * The phone number of the member.
     * Must be 10-13 digits and can optionally start with +.
     */
    @NotBlank(message = "Phone number is mandatory")
    @Pattern(
            regexp = "^\\+?[0-9]{10,13}$",
            message = "Phone number must be 10-13 digits and can optionally start with +"
    )
    private String phoneNumber;

    /**
     * Constructs a MemberDto with the specified details.
     *
     * @param memberId    the unique identifier for the member
     * @param name        the name of the member
     * @param email       the email address of the member
     * @param phoneNumber the phone number of the member
     */
    public MemberDto(Long memberId, String name, String email, String phoneNumber) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Default constructor for MemberDto.
     */
    public MemberDto() {
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

    /**
     * Mapper class for converting between MemberDto and Member entities.
     */
    public static class Mapper {

        /**
         * Default constructor for MemberDto.Mapper.
         */
        public Mapper() {
        }

        /**
         * Converts a MemberDto to a Member entity.
         *
         * @param dto the MemberDto to convert
         * @return the converted Member entity
         */
        public static Member toEntity(MemberDto dto) {
            if (dto == null) {
                return null;
            }
            return new Member(
                    dto.getMemberId(),
                    dto.getName(),
                    dto.getEmail(),
                    dto.getPhoneNumber()
            );
        }

        /**
         * Converts a Member entity to a MemberDto.
         *
         * @param member the Member entity to convert
         * @return the converted MemberDto
         */
        public static MemberDto fromEntity(Member member) {
            if (member == null) {
                return null;
            }
            return new MemberDto(
                    member.getMemberId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhoneNumber()
            );
        }
    }
}
