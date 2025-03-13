package org.johndoe.kitchensink.controllers;

import lombok.AllArgsConstructor;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.services.KeycloakAuthService;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for managing the dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
public class DashboardController {

    /**
     * Service for managing members.
     */
    private final MemberService service;

    /**
     * Service for managing Keycloak authentication.
     */
    private final KeycloakAuthService keycloakAuthService;

    /**
     * Gets the profile of a user.
     *
     * @param id the user ID
     * @return the user profile
     */
    @GetMapping("/profile/{id}")
    public MemberDto getUserProfile(@PathVariable("id") Long id) {
        return service.findMemberById(id);
    }

    /**
     * Updates the profile of a user.
     *
     * @param id the user ID
     * @param memberDto the updated user profile
     * @return the updated user profile
     */
    @PutMapping("/profile/{id}")
    public MemberDto updateUserProfile(@PathVariable("id") Long id, @RequestBody MemberDto memberDto) {
        keycloakAuthService.updateUserInKeycloak(memberDto);
        return service.updateMember(id, memberDto);
    }
}
