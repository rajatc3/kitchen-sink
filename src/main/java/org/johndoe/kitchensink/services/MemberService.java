package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.exceptions.ValidationException;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.fromEntity;
import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.toEntity;

/**
 * Service class for managing members.
 */
@Service
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
        return memberRepository.findAll().stream().map((MemberDto.Mapper::fromEntity)).toList();
    }

    /**
     * Finds a member by their email.
     *
     * @param email the email
     * @return the member DTO
     * @throws UserNotFoundException if the member is not found
     */
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
     * Finds a member by their name.
     *
     * @param name the name
     * @return the member DTO
     * @throws UserNotFoundException if the member is not found
     */
    public MemberDto findMemberByName(String name) {
        return fromEntity(memberRepository.findByName(name).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND)));
    }

    /**
     * Updates a member.
     *
     * @param id     the member ID
     * @param member the member DTO
     * @return the updated member DTO
     * @throws UserNotFoundException if the member is not found
     */
    @Transactional
    public MemberDto updateMember(Long id, MemberDto member) {

        Optional<Member> existingMember = memberRepository.findByEmailOrPhoneNumberAndIdNot(member.getEmail(), member.getPhoneNumber(), id);

        if (existingMember.isPresent()) {
            throw new ValidationException("email or phone number already tagged to another user");
        }

        Member memberEntity = memberRepository.findByMemberId(id).orElseThrow(() -> new UserNotFoundException(MEMBER_NOT_FOUND));
        if (member.getName() != null) {
            memberEntity.setName(member.getName());
        }
        if (member.getEmail() != null) {
            memberEntity.setEmail(member.getEmail());
        }
        if (member.getPhoneNumber() != null) {
            memberEntity.setPhoneNumber(member.getPhoneNumber());
        }

        return fromEntity(memberRepository.save(memberEntity));
    }

    /**
     * Deletes a member by their ID.
     *
     * @param id the member ID
     */
    public void deleteMember(Long id) {
        memberRepository.deleteByMemberId(id);
    }
}