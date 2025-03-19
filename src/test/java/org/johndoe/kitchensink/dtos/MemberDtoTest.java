package org.johndoe.kitchensink.dtos;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.utils.UtilityMethods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class MemberDtoTest {

    private MemberDto memberDto;

    @BeforeEach
    void setUp() {
        memberDto = new MemberDto("testuser", "John", "Doe", "john.doe@example.com", "9876543210", "USER");
    }

    @Test
    void testGetPasswordAsString() {
        char[] password = {'p', 'a', 's', 's'};
        memberDto.setPassword(password);
        assertEquals("pass", memberDto.getPasswordAsString());
    }

    @Test
    void testGetRepeatPasswordAsString() {
        char[] repeatPassword = {'p', 'a', 's', 's'};
        memberDto.setRepeatPassword(repeatPassword);
        assertEquals("pass", memberDto.getRepeatPasswordAsString());
    }

    @Test
    void testToEntity() {
        Member member = MemberDto.Mapper.toEntity(memberDto);
        assertNotNull(member);
        assertEquals(memberDto.getUsername(), member.getUsername());
        assertEquals(memberDto.getEmail(), member.getEmail());
    }

    @Test
    void testToEntity_NullDto() {
        assertNull(MemberDto.Mapper.toEntity(null));
    }

    @Test
    void testFromEntity() {
        Member member = new Member(1L, "testuser", "John", "Doe", "john.doe@example.com", "9876543210", "USER");
        MemberDto dto = MemberDto.Mapper.fromEntity(member);
        assertNotNull(dto);
        assertEquals(member.getUsername(), dto.getUsername());
    }

    @Test
    void testFromEntity_NullMember() {
        assertNull(MemberDto.Mapper.fromEntity(null));
    }

    @Test
    void testFromEntity_WithMasking() {
        Member member = new Member(1L, "testuser", "John", "Doe", "john.doe@example.com", "9876543210", "USER");
        try (MockedStatic<UtilityMethods> utilities = Mockito.mockStatic(UtilityMethods.class)) {
            utilities.when(() -> UtilityMethods.maskEmail("john.doe@example.com")).thenReturn("j****@example.com");
            utilities.when(() -> UtilityMethods.maskPhone("9876543210")).thenReturn("98******10");

            MemberDto dto = MemberDto.Mapper.fromEntity(member, true);
            assertNotNull(dto);
            assertEquals("j****@example.com", dto.getEmail());
            assertEquals("98******10", dto.getPhoneNumber());
        }
    }
}