package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.exceptions.ValidationException;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.fromEntity;
import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.toEntity;

/**
 * Service class for managing members.
 */
@Service
@EnableCaching
public class MemberService {

    /**
     * Message for member not found.
     */
    public static final String MEMBER_NOT_FOUND = "Member not yet present in database. Please come back later!!";

    /**
     * Repository for member data.
     */
    private final MemberRepository memberRepository;

    /**
     * Constructs a new MemberService with the given MemberRepository.
     *
     * @param memberRepository the member repository
     */
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Finds a member by their ID.
     *
     * @param id the member ID
     * @return the member DTO
     * @throws UserNotFoundException if the member is not found
     */
    @Cacheable(value = "members", key = "#id", unless = "#result == null")
    public MemberDto findMemberById(Long id) {
        return fromEntity(memberRepository.findByMemberId(id).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND)));
    }

    /**
     * Creates a new member.
     *
     * @param memberDto the member DTO
     * @return the created member DTO
     * @throws ValidationException if validation fails
     */
    @CacheEvict(value = "members", allEntries = true)
    public MemberDto createMember(MemberDto memberDto) {
        validateMember(memberDto);

        Member lastMember = memberRepository.findTopByOrderByMemberIdDesc().orElse(new Member());
        memberDto.setMemberId((lastMember.getMemberId() == null) ? 1 : lastMember.getMemberId() + 1);
        return fromEntity(memberRepository.save(toEntity(memberDto)));
    }

    /**
     * Validates the member DTO.
     *
     * @param memberDto the member DTO
     * @throws ValidationException if validation fails
     */
    private void validateMember(MemberDto memberDto) {
        List<String> validationErrors = new ArrayList<>();
        if (emailAlreadyExists(memberDto.getEmail())) {
            validationErrors.add("email should be unique");
        }

        if (phoneAlreadyExists(memberDto.getPhoneNumber())) {
            validationErrors.add("phone number should be unique");
        }
        if (!memberDto.getPasswordAsString().equals(memberDto.getRepeatPasswordAsString())) {
            validationErrors.add("Passwords do not match");
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }

    /**
     * Checks if the email already exists.
     *
     * @param email the email
     * @return true if the email exists, false otherwise
     */
    private boolean emailAlreadyExists(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    /**
     * Checks if the phone number already exists.
     *
     * @param phoneNumber the phone number
     * @return true if the phone number exists, false otherwise
     */
    private boolean phoneAlreadyExists(String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    /**
     * Finds all members.
     *
     * @return the list of member DTOs
     */
    public List<MemberDto> findAllMembers() {
        return findAllMembersAsEntity().stream().map((MemberDto.Mapper::fromEntity)).toList();
    }

    public Page<MemberDto> findAllMembers(Pageable page) {
        return memberRepository.findAll(page).map(MemberDto.Mapper::fromEntity);
    }

    /**
     * Finds all members as entities.
     *
     * @return the list of member entities
     */
    public List<Member> findAllMembersAsEntity() {
        return memberRepository.findAll();
    }

    /**
     * Finds a member by their email.
     *
     * @param email the email
     * @return the member DTO
     * @throws UserNotFoundException if the member is not found
     */
    @Cacheable(value = "members", key = "#email")
    public MemberDto findMemberByEmail(String email) {
        return fromEntity(memberRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND)));
    }

    /**
     * Finds a member by their phone number.
     *
     * @param phoneNumber the phone number
     * @return the member DTO
     * @throws UserNotFoundException if the member is not found
     */
    public MemberDto findMemberByPhoneNumber(String phoneNumber) {
        return fromEntity(memberRepository.findByPhoneNumber(phoneNumber).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND)));
    }

    /**
     * Finds a member by their username.
     *
     * @param name the username of the user
     * @return the member DTO
     * @throws UserNotFoundException if the member is not found
     */
    @Cacheable(value = "members", key = "#name")
    public MemberDto findMemberByName(String name) {
        return fromEntity(findMemberEntityByName(name));
    }

    public Member findMemberEntityByName(String name) {
        return memberRepository.findByUsername(name).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND));
    }

    /**
     * Updates a member.
     *
     * @param id     the member ID
     * @param member the member DTO
     * @return the updated member DTO
     * @throws UserNotFoundException if the member is not found
     */
    @CacheEvict(value = "members", key = "#id")
    @Transactional
    public MemberDto updateMember(Long id, MemberDto member) {

        validateEmailOrPhoneNumberForAnotherUser(member);
        Member memberEntity = memberRepository.findByMemberId(id).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND));
        return saveMember(member, memberEntity);
    }

    /**
     * Updates a member.
     *
     * @param username the username of the member
     * @param member   the member DTO
     * @return the updated member DTO
     * @throws UserNotFoundException if the member is not found
     */
    @CacheEvict(value = "members", allEntries = true)
    public MemberDto updateMember(String username, MemberDto member) {

        validateEmailOrPhoneNumberForAnotherUser(member);
        Member memberEntity = memberRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND));
        return saveMember(member, memberEntity);
    }

    /**
     * Validates the email or phone number for another user.
     *
     * @param member the member DTO
     */
    public void validateEmailOrPhoneNumberForAnotherUser(MemberDto member) {
        Optional<Member> existingMember = memberRepository.findRecordsWithConflictingEmailOrPhoneNumber(member.getEmail(), member.getPhoneNumber(), member.getUsername());

        if (existingMember.isPresent()) {
            throw new ValidationException("email or phone number already tagged to another user");
        }
    }

    /**
     * Saves a member.
     *
     * @param member       the member DTO
     * @param memberEntity the member entity
     * @return the saved member DTO
     */
    private MemberDto saveMember(MemberDto member, Member memberEntity) {
        if (member.getFirstName() != null) memberEntity.setFirstName(member.getFirstName());
        if (member.getLastName() != null) memberEntity.setLastName(member.getLastName());
        if (member.getEmail() != null) memberEntity.setEmail(member.getEmail());
        if (member.getPhoneNumber() != null) memberEntity.setPhoneNumber(member.getPhoneNumber());

        return fromEntity(memberRepository.save(memberEntity));
    }

    /**
     * Deletes a member by their ID.
     *
     * @param id the member ID
     */
    @CacheEvict(value = "members", key = "#id")
    public void deleteMember(Long id) {
        memberRepository.deleteByMemberId(id);
    }

    /**
     * Checks if a value exists as an email or phone number.
     *
     * @param value the value
     * @return true if the value exists, false otherwise
     */
    public boolean doesValueExistAsEmailOrPhoneNumber(String value) {
        return memberRepository.findByEmail(value).isPresent() || memberRepository.findByPhoneNumber(value).isPresent();
    }

    /**
     * Checks if a value exists as a username.
     *
     * @param value the value
     * @return true if the value exists, false otherwise
     */
    public boolean doesValueExistsAsUsername(String value) {
        try {
            findMemberByName(value);
        } catch (UserNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Finds a member by their email or username.
     *
     * @param identifier the email or username
     * @return the member ID
     */
    public Long findMemberIdByEmailOrUsername(String identifier) {
        return memberRepository.findByEmailOrUsername(identifier).orElseThrow(() -> new RuntimeException(MEMBER_NOT_FOUND)).getMemberId();
    }

    /**
     * Counts the number of users.
     *
     * @return the number of users
     */
    public Long countUsers() {
        return memberRepository.count();
    }

    public MemberDto assignAdminRoleToUser(Member member) {
        member.setUserRole(ApplicationConstants.ROLES.ADMIN.name().toLowerCase());
        return fromEntity(memberRepository.save(member));
    }

    public Mono<Boolean> checkUsernameAvailability(String username) {
        return Mono.just(memberRepository.findByUsername(username).isEmpty());
    }
}