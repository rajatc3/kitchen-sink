package org.johndoe.kitchensink.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.johndoe.kitchensink.annotations.PasswordRules;
import org.johndoe.kitchensink.annotations.UniqueInDatabase;
import org.johndoe.kitchensink.annotations.UniqueUsername;
import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.utils.UtilityMethods;

/**
 * Data Transfer Object for Member.
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberDto {

    /**
     * The unique identifier for the member.
     */
    private Long memberId;

    /**
     * The name of the member.
     * Must be between 3 and 50 characters.
     */
    @NotBlank(message = "Username is mandatory")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @UniqueUsername
    private String username;

    @NotBlank(message = "First Name is mandatory")
    private String firstName;

    @NotBlank(message = "Last Name is mandatory")
    private String lastName;

    /**
     * The email address of the member.
     * Must be a valid email format.
     */
    @Pattern(
            regexp = "^(?!.*\\.\\.)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|edu|gov|mil|info|co|io|biz|in|us|uk|ca|au|de|fr|jp|cn|br|za|ru|eu|[a-zA-Z]{2,})$",
            message = "Email should be valid"
    )
    @NotBlank(message = "Email is mandatory")
    @UniqueInDatabase
    private String email;

    /**
     * The phone number of the member.
     * Must be a valid 10 digit Indian number without country code.
     */
    @NotBlank(message = "Phone number is mandatory")
    @Pattern(
            regexp = "^[6789]\\d{9}$",
            message = "Phone number must be valid 10 digit indian number without country code"
    )
    @UniqueInDatabase
    private String phoneNumber;

    /**
     * The password of the member.
     */
    @PasswordRules
    private char[] password;

    /**
     * The repeated password of the member.
     */
    @PasswordRules
    private char[] repeatPassword;

    private String userRole;

    /**
     * Custom constructor for MemberDto
     */
    public MemberDto(Long memberId, String username, String firstName, String lastName, String email, String phoneNumber, String userRole) {
        this.memberId = memberId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
    }

    /**
     * Custom constructor for MemberDto
     */
    public MemberDto(String username, String firstName, String lastName, String email, String phoneNumber, char[] password, char[] repeatPassword, String userRole) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.userRole = userRole;
    }

    /**
     * Custom constructor for MemberDto
     */
    public MemberDto(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Custom constructor for MemberDto
     */
    public MemberDto(String firstName, String lastName, String username, String userRole, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.userRole = userRole;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the password as a string.
     *
     * @return the password as a string
     */
    public String getPasswordAsString() {
        return password != null ? new String(password) : null;
    }

    /**
     * Gets the repeat password as a string.
     *
     * @return the repeat password as a string
     */
    public String getRepeatPasswordAsString() {
        return repeatPassword != null ? new String(repeatPassword) : null;
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
                    dto.getUsername(),
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getEmail(),
                    dto.getPhoneNumber(),
                    dto.getUserRole()
            );
        }

        /**
         * Converts a Member entity to a MemberDto.
         *
         * @param member the Member entity to convert
         * @return the converted MemberDto
         */
        public static MemberDto fromEntity(Member member) {
            return fromEntity(member, false);
        }

        public static MemberDto fromEntity(Member member , boolean isMasked) {
            if (member == null) {
                return null;
            }

            if(isMasked) {
                member.setEmail(UtilityMethods.maskEmail(member.getEmail()));
                member.setPhoneNumber(UtilityMethods.maskPhone(member.getPhoneNumber()));
            }

            return new MemberDto(
                    member.getMemberId(),
                    member.getUsername(),
                    member.getFirstName(),
                    member.getLastName(),
                    member.getEmail(),
                    member.getPhoneNumber(),
                    member.getUserRole()
            );
        }
    }
}
