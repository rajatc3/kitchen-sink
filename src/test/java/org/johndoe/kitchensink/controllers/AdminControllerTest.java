package org.johndoe.kitchensink.controllers;

import org.jetbrains.annotations.NotNull;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.services.AdminService;
import org.johndoe.kitchensink.services.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private AdminService adminService;

    private static @NotNull Map<String, Object> generateAnalyticsData() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", 3);
        stats.put("totalPosts", 4);
        stats.put("totalComments", 20);

        List<Map<String, Object>> memberStats = Arrays.asList(
                Map.of(
                        "username", "john_doe",
                        "totalPosts", 2,
                        "posts", Arrays.asList(
                                Map.of("postTitle", "First Post", "postId", 1, "totalComments", 5),
                                Map.of("postTitle", "Another Post", "postId", 2, "totalComments", 2)
                        )
                ),
                Map.of(
                        "username", "jane_smith",
                        "totalPosts", 1,
                        "posts", List.of(
                                Map.of("postTitle", "Exciting News", "postId", 3, "totalComments", 10)
                        )
                ),
                Map.of(
                        "username", "alice_wonder",
                        "totalPosts", 1,
                        "posts", List.of(
                                Map.of("postTitle", "Learning Java", "postId", 4, "totalComments", 3)
                        )
                )
        );

        stats.put("members", memberStats);

        Map<String, Object> topPost = Map.of(
                "postTitle", "Exciting News",
                "postId", 3,
                "member", "Jane Smith",
                "totalComments", 10
        );

        stats.put("topPost", topPost);
        return stats;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers() throws Exception {
        MemberDto member = new MemberDto(1L, "john.doe", "John", "Doe", "john.doe@email.com", "9876543210", "user");

        PageRequest pageable = PageRequest.of(0, 10); // ✅ Concrete implementation
        Page<MemberDto> page = new PageImpl<>(Collections.singletonList(member), pageable, 1);

        when(memberService.findAllMembers(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")  // ✅ Provide pagination parameters
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.content").isArray());

        verify(memberService, times(1)).findAllMembers(any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAnalytics() throws Exception {
        Map<String, Object> analyticsData = generateAnalyticsData();
        when(adminService.getAnalytics()).thenReturn(analyticsData);

        mockMvc.perform(get("/api/admin/analytics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))
                .andExpect(jsonPath("$.totalPosts").value(4));

        verify(adminService, times(1)).getAnalytics();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAssignAdminRole() throws Exception {
        MemberDto adminMember = new MemberDto(1L, "jane.doe", "Jane", "Doe", "jane.doe@email.com", "9876543211", "admin");
        when(adminService.assignAdminRoles("jane.doe")).thenReturn(adminMember);

        mockMvc.perform(put("/api/admin/elevate/jane.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))  // ✅ Explicitly adds a CSRF token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane.doe"))
                .andExpect(jsonPath("$.userRole").value("admin"));

        verify(adminService, times(1)).assignAdminRoles("jane.doe");
    }
}
