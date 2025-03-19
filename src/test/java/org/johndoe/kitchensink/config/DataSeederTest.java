package org.johndoe.kitchensink.config;

import org.johndoe.kitchensink.documents.Member;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.repositories.CommentRepository;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.repositories.PostRepository;
import org.johndoe.kitchensink.services.KeycloakAuthService;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KeycloakAuthService keycloakAuthService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private Logger log;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    private MemberDto adminDto;
    private MemberDto userDto;
    private Member adminMember;
    private Member userMember;

    @BeforeEach
    void setUp() {
        adminDto = new MemberDto("john.doe", "John", "Doe", "john.doe@email.com", "9876543210",
                "admin".toCharArray(), "admin".toCharArray(), ApplicationConstants.ROLES.ADMIN.name().toLowerCase());

        userDto = new MemberDto("jane.doe", "Jane", "Doe", "jane.doe@email.com", "9876543211",
                "user".toCharArray(), "user".toCharArray(), ApplicationConstants.ROLES.USER.name().toLowerCase());

        adminMember = new Member(1L, "john.doe", "John", "Doe", "john.doe@email.com", "9876543210", ApplicationConstants.ROLES.ADMIN.name().toLowerCase());
        userMember = new Member(2L, "jane.doe", "Jane", "Doe", "jane.doe@email.com", "9876543211", ApplicationConstants.ROLES.USER.name().toLowerCase());
    }

    @Test
    void testSeedMemberData_ExistingMembers() {
        // Given
        dataSeeder.refreshDatabase = false;
        when(memberRepository.findByUsername(adminDto.getUsername())).thenReturn(Optional.of(adminMember));
        when(memberRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.of(userMember));

        // When
        dataSeeder.seedMemberData();

        // Then
        verify(memberRepository, never()).save(any(Member.class));
        verify(keycloakAuthService, never()).register(any(MemberDto.class));
    }

    @Test
    void testGenerateDummyPostsAndComments() {
        // Given
        when(memberRepository.findByUsername(adminDto.getUsername())).thenReturn(Optional.of(adminMember));
        when(postRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataSeeder.generateDummyPostsAndComments(adminDto);

        // Then
        verify(postRepository, times(1)).saveAll(anyList());
    }
}
