package org.johndoe.kitchensink.controllers;

import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.services.AdminService;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.johndoe.kitchensink.utils.UtilityMethods.maskEmail;
import static org.johndoe.kitchensink.utils.UtilityMethods.maskPhone;

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
     * @param adminService the admin service
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
    public ResponseEntity<List<MemberDto>> getAllUsers() {
        List<MemberDto> users = memberService.findAllMembers().stream()
                .map(user -> new MemberDto(
                        user.getFirstName(),
                        user.getLastName(),
                        maskEmail(user.getEmail()),
                        maskPhone(user.getPhoneNumber())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    /**
     * Gets analytics data.
     *
     * @return a ResponseEntity containing analytics data
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(adminService.getUserStats());
    }
}
