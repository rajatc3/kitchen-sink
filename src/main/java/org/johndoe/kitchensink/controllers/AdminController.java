package org.johndoe.kitchensink.controllers;

import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.services.AdminService;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.johndoe.kitchensink.utils.UtilityMethods.paginateResponse;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * Service for managing members actions.
     */
    private final MemberService memberService;

    /**
     * Service for managing admins actions.
     */
    private final AdminService adminService;

    /**
     * Constructs an AdminController with the given MemberService.
     *
     * @param memberService the member service
     * @param adminService  the admin service
     */
    public AdminController(MemberService memberService, AdminService adminService) {
        this.memberService = memberService;
        this.adminService = adminService;
    }

    /**
     * Gets all users.
     *
     * @return a ResponseEntity containing a list of all users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(@PageableDefault(size = 10) Pageable pageInput) {
        Page<MemberDto> page = memberService.findAllMembers(pageInput);

        Map<String, Object> response = paginateResponse(page);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets analytics data.
     *
     * @return a ResponseEntity containing analytics data
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }

    /**
     * Assigns admin roles to a user.
     *
     * @param username the username of the user
     * @return a ResponseEntity containing the updated user
     */
    @PutMapping("elevate/{username}")
    public ResponseEntity<MemberDto> assignAdminRole(@PathVariable("username") String username) {
        return ResponseEntity.ok(adminService.assignAdminRoles(username));
    }
}
