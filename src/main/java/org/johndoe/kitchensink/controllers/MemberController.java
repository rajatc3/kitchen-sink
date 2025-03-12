package org.johndoe.kitchensink.controllers;

import jakarta.validation.Valid;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * MemberController handles member-related endpoints.
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    /**
     * Service for member operations.
     */
    private final MemberService service;

    /**
     * Constructs a MemberController with the given MemberService.
     *
     * @param service the MemberService to use for member operations
     */
    public MemberController(MemberService service) {
        this.service = service;
    }

    /**
     * Retrieves all members.
     *
     * @return a ResponseEntity containing the list of all members
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<Object> getMembers() {
        return ResponseEntity.ok(service.findAllMembers());
    }

    /**
     * Retrieves a member by their ID.
     *
     * @param id the ID of the member to retrieve
     * @return a ResponseEntity containing the member with the given ID
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Object> getMembersById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findMemberById(id));
    }

    /**
     * Retrieves a member by their email.
     *
     * @param email the email of the member to retrieve
     * @return a ResponseEntity containing the member with the given email
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/email/{email}", produces = "application/json")
    public ResponseEntity<Object> getMembersByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(service.findMemberByEmail(email));
    }

    /**
     * Retrieves a member by their phone number.
     *
     * @param phoneNumber the phone number of the member to retrieve
     * @return a ResponseEntity containing the member with the given phone number
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/phone/{phoneNumber}", produces = "application/json")
    public ResponseEntity<Object> getMembersByPhoneNumber(@PathVariable("phoneNumber") String phoneNumber) {
        return ResponseEntity.ok(service.findMemberByPhoneNumber(phoneNumber));
    }

    /**
     * Retrieves a member by their name.
     *
     * @param name the name of the member to retrieve
     * @return a ResponseEntity containing the member with the given name
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/username/{name}", produces = "application/json")
    public ResponseEntity<Object> getMembersByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(service.findMemberByName(name));
    }

    /**
     * Creates a new member.
     *
     * @param member the member data to create
     * @return a ResponseEntity containing the created member
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Object> createMember(@Valid @RequestBody MemberDto member) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMember(member));
    }

    /**
     * Updates an existing member.
     *
     * @param id     the ID of the member to update
     * @param member the updated member data
     * @return a ResponseEntity containing the updated member
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Object> updateMember(@PathVariable("id") Long id, @Valid @RequestBody MemberDto member) {
        return ResponseEntity.ok(service.updateMember(id, member));
    }

    /**
     * Deletes a member by their ID.
     *
     * @param id the ID of the member to delete
     * @return a ResponseEntity indicating the deletion status
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteMember(@PathVariable("id") Long id) {
        service.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}