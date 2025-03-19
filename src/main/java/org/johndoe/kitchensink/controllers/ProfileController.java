package org.johndoe.kitchensink.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.security.config.JwtAuthConverter;
import org.johndoe.kitchensink.services.KeycloakAuthService;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller class for managing the dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class ProfileController {

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
    @GetMapping("/profile")
    public ResponseEntity<MemberDto> getUserProfile(Principal principal) {
        return ResponseEntity.ok(service.findMemberByName(JwtAuthConverter.getUsernameFromPrincipal(principal)));
    }

    /**
     * Updates the profile of a user.
     *
     * @param id        the user ID
     * @param memberDto the updated user profile
     * @return the updated user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<MemberDto> updateUserProfile(Principal principal, @RequestBody MemberDto memberDto) {
        String usernameFromPrincipal = JwtAuthConverter.getUsernameFromPrincipal(principal);
        service.validateEmailOrPhoneNumberForAnotherUser(memberDto);
        keycloakAuthService.updateUserInKeycloak(memberDto);

        return ResponseEntity.ok(service.updateMember(usernameFromPrincipal, memberDto));
    }
}
