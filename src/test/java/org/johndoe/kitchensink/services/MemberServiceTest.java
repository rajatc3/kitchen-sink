package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.exceptions.ValidationException;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private MemberDto memberDto;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setMemberId(1L);
        member.setUsername("john.doe");
        member.setEmail("john.doe@email.com");
        member.setPhoneNumber("9876543210");

        memberDto = MemberDto.Mapper.fromEntity(member);
        memberDto.setPassword("Querty@1".toCharArray());
        memberDto.setRepeatPassword("Querty@1".toCharArray());
    }

    @Test
    void testFindMemberById_Success() {
        when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));
        MemberDto result = memberService.findMemberById(1L);
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
    }

    @Test
    void testFindMemberById_NotFound() {
        when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> memberService.findMemberById(1L));
    }

    @Test
    void testCreateMember_Success() {
        when(memberRepository.findTopByOrderByMemberIdDesc()).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberDto result = memberService.createMember(memberDto);
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
    }

    @Test
    void testCreateMember_EmailAlreadyExists() {
        when(memberRepository.findByEmail(memberDto.getEmail())).thenReturn(Optional.of(member));
        assertThrows(ValidationException.class, () -> memberService.createMember(memberDto));
    }

    @Test
    void testFindAllMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(member));
        List<MemberDto> members = memberService.findAllMembers();
        assertFalse(members.isEmpty());
    }

    @Test
    void testFindAllMembersWithPagination() {
        Page<Member> page = new PageImpl<>(List.of(member));
        when(memberRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<MemberDto> result = memberService.findAllMembers(PageRequest.of(0, 10));
        assertFalse(result.isEmpty());
    }

    @Test
    void testFindMemberByEmail_Success() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        MemberDto result = memberService.findMemberByEmail(member.getEmail());
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
    }

    @Test
    void testFindMemberByEmail_NotFound() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> memberService.findMemberByEmail(member.getEmail()));
    }

    @Test
    void testFindMemberByPhoneNumber_Success() {
        when(memberRepository.findByPhoneNumber(member.getPhoneNumber())).thenReturn(Optional.of(member));
        MemberDto result = memberService.findMemberByPhoneNumber(member.getPhoneNumber());
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
    }

    @Test
    void testFindMemberByPhoneNumber_NotFound() {
        when(memberRepository.findByPhoneNumber(member.getPhoneNumber())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> memberService.findMemberByPhoneNumber(member.getPhoneNumber()));
    }

    @Test
    void testFindMemberByName_Success() {
        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.of(member));
        MemberDto result = memberService.findMemberByName(member.getUsername());
        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
    }

    @Test
    void testFindMemberByName_NotFound() {
        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> memberService.findMemberByName(member.getUsername()));
    }

    @Test
    void testUpdateMember_Success() {
        when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberDto updatedDto = new MemberDto();
        updatedDto.setEmail("new.email@email.com");

        MemberDto result = memberService.updateMember(1L, updatedDto);
        assertNotNull(result);
    }

    @Test
    void testUpdateMember_NotFound() {
        when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> memberService.updateMember(1L, memberDto));
    }

    @Test
    void testDeleteMember() {
        doNothing().when(memberRepository).deleteByMemberId(1L);
        memberService.deleteMember(1L);
        verify(memberRepository, times(1)).deleteByMemberId(1L);
    }

    @Test
    void testDoesValueExistAsEmailOrPhoneNumber_True() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        assertTrue(memberService.doesValueExistAsEmailOrPhoneNumber(member.getEmail()));
    }

    @Test
    void testDoesValueExistAsEmailOrPhoneNumber_False() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());
        assertFalse(memberService.doesValueExistAsEmailOrPhoneNumber(member.getEmail()));
    }

    @Test
    void testDoesValueExistsAsUsername_True() {
        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.of(member));
        assertTrue(memberService.doesValueExistsAsUsername(member.getUsername()));
    }

    @Test
    void testDoesValueExistsAsUsername_False() {
        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.empty());
        assertFalse(memberService.doesValueExistsAsUsername(member.getUsername()));
    }

    @Test
    void testFindMemberIdByEmailOrUsername_Success() {
        when(memberRepository.findByEmailOrUsername(member.getEmail())).thenReturn(Optional.of(member));
        Long memberId = memberService.findMemberIdByEmailOrUsername(member.getEmail());
        assertEquals(1L, memberId);
    }

    @Test
    void testFindMemberIdByEmailOrUsername_NotFound() {
        when(memberRepository.findByEmailOrUsername(member.getEmail())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> memberService.findMemberIdByEmailOrUsername(member.getEmail()));
    }

    @Test
    void testCountUsers() {
        when(memberRepository.count()).thenReturn(1L);
        Long count = memberService.countUsers();
        assertEquals(1L, count);
    }

    @Test
    void testAssignAdminRoleToUser() {
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        MemberDto result = memberService.assignAdminRoleToUser(member);
        assertNotNull(result);
        assertEquals("admin", result.getUserRole());
    }

    @Test
    void testCheckUsernameAvailability_True() {
        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.empty());
        Mono<Boolean> result = memberService.checkUsernameAvailability(member.getUsername());
        assertTrue(result.block());
    }

    @Test
    void testCheckUsernameAvailability_False() {
        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.of(member));
        Mono<Boolean> result = memberService.checkUsernameAvailability(member.getUsername());
        assertFalse(result.block());
    }

    @Test
    void testUpdateMemberByUsername_Success() {
        Member existingMember = new Member();
        existingMember.setUsername("john.doe");
        existingMember.setEmail("john.doe@email.com");
        existingMember.setPhoneNumber("9876543210");

        MemberDto updatedDto = new MemberDto();
        updatedDto.setFirstName("John");
        updatedDto.setLastName("Doe");
        updatedDto.setEmail("new.email@email.com");
        updatedDto.setPhoneNumber("1234567890");

        when(memberRepository.findRecordsWithConflictingEmailOrPhoneNumber(updatedDto.getEmail(), updatedDto.getPhoneNumber(), updatedDto.getUsername()))
                .thenReturn(Optional.empty());
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        MemberDto result = memberService.updateMember("john.doe", updatedDto);

        assertNotNull(result);
        assertEquals("new.email@email.com", result.getEmail());
        assertEquals("1234567890", result.getPhoneNumber());
        verify(memberRepository, times(1)).save(existingMember);
    }

    @Test
    void testUpdateMemberByUsername_ValidationException() {
        Member existingMember = new Member();
        existingMember.setUsername("john.doe");
        existingMember.setEmail("john.doe@email.com");
        existingMember.setPhoneNumber("9876543210");

        MemberDto updatedDto = new MemberDto();
        updatedDto.setFirstName("John");
        updatedDto.setLastName("Doe");
        updatedDto.setEmail("new.email@email.com");
        updatedDto.setPhoneNumber("1234567890");

        when(memberRepository.findRecordsWithConflictingEmailOrPhoneNumber(updatedDto.getEmail(), updatedDto.getPhoneNumber(), updatedDto.getUsername()))
                .thenReturn(Optional.of(existingMember));

        assertThrows(ValidationException.class, () -> memberService.updateMember("john.doe", updatedDto));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testUpdateMemberByUsername_UserNotFoundException() {
        MemberDto updatedDto = new MemberDto();
        updatedDto.setFirstName("John");
        updatedDto.setLastName("Doe");
        updatedDto.setEmail("new.email@email.com");
        updatedDto.setPhoneNumber("1234567890");

        when(memberRepository.findRecordsWithConflictingEmailOrPhoneNumber(updatedDto.getEmail(), updatedDto.getPhoneNumber(), updatedDto.getUsername()))
                .thenReturn(Optional.empty());
        when(memberRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> memberService.updateMember("john.doe", updatedDto));
        verify(memberRepository, never()).save(any(Member.class));
    }
}