package org.johndoe.kitchensink.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.johndoe.kitchensink.advices.GlobalExceptionHandler;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.repositories.MemberRepository;
import org.johndoe.kitchensink.services.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc
public class MemberControllerTest {

    public static final String MEMBER_NOT_PRESENT = "Member not yet present in database. Please come back later!!";
    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private MemberRepository memberRepository;
    @MockitoBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private MemberController memberController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(memberController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetMembers_Success() throws Exception {
        when(memberService.findAllMembers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetMemberById_Success() throws Exception {
        MemberDto mockMember = new MemberDto();
        mockMember.setMemberId(1L);
        mockMember.setUsername("john.doe");
        mockMember.setFirstName("John");
        mockMember.setLastName("Doe");
        mockMember.setEmail("john.doe@example.com");
        mockMember.setPhoneNumber("123-456-7890");

        when(memberService.findMemberById(anyLong())).thenReturn(mockMember);

        mockMvc.perform(get("/api/members/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetMemberById_NotFound() throws Exception {
        when(memberService.findMemberById(anyLong())).thenThrow(new UserNotFoundException(MEMBER_NOT_PRESENT));

        mockMvc.perform(get("/api/members/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteMember_Success() throws Exception {
        mockMvc.perform(delete("/api/members/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
