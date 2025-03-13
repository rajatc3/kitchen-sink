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

import java.util.List;
import java.util.Optional;

import static org.johndoe.kitchensink.dtos.MemberDto.Mapper.toEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private MemberDto memberDto;
    private Member member;

    @BeforeEach
    public void setUp() {
        memberDto = new MemberDto(1L, "john.doe", "John", "Doe", "john@example.com", "1234567890", "ADMIN");
        member = toEntity(memberDto);
    }

    @Test
    public void findMemberById_ShouldReturnMember_WhenMemberExists() {
        when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));
        MemberDto result = memberService.findMemberById(1L);
        assertNotNull(result);
        assertEquals("John Doe", result.getFirstName() + " " + result.getLastName());
    }

    @Test
    public void findMemberById_ShouldThrowException_WhenMemberNotFound() {
        when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> memberService.findMemberById(1L));
    }

    @Test
    public void createMember_ShouldSaveAndReturnMember_WhenValid() {
        when(memberRepository.findTopByOrderByMemberIdDesc()).thenReturn(Optional.of(member));
        when(memberRepository.save(any())).thenReturn(member);
        MemberDto result = memberService.createMember(memberDto);
        assertNotNull(result);
        assertEquals("John Doe", result.getFirstName() + " " + result.getLastName());
    }

    @Test
    public void findAllMembers_ShouldReturnListOfMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(member));
        List<MemberDto> result = memberService.findAllMembers();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void updateMember_ShouldUpdateAndReturnMember_WhenValid() {
        when(memberRepository.findByEmailOrPhoneNumberAndIdNot(any(), any(), anyLong())).thenReturn(Optional.empty());
        when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any())).thenReturn(member);
        MemberDto updatedMember = new MemberDto(1L, "jane.doe", "Jane", "Doe", "jane@example.com", "0987654321", "ADMIN");
        MemberDto result = memberService.updateMember(1L, updatedMember);
        assertEquals("Jane Doe", result.getFirstName() + " " + result.getLastName());
    }

    @Test
    public void updateMember_ShouldThrowException_WhenEmailOrPhoneExists() {
        when(memberRepository.findByEmailOrPhoneNumberAndIdNot(any(), any(), anyLong())).thenReturn(Optional.of(member));
        assertThrows(ValidationException.class, () -> memberService.updateMember(1L, memberDto));
    }

    @Test
    public void deleteMember_ShouldCallRepositoryDeleteMethod() {
        doNothing().when(memberRepository).deleteByMemberId(1L);
        memberService.deleteMember(1L);
        verify(memberRepository, times(1)).deleteByMemberId(1L);
    }
}