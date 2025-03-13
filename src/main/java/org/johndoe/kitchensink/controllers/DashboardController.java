package org.johndoe.kitchensink.controllers;

import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final MemberService service;

    public DashboardController(MemberService service) {
        this.service = service;
    }

    @GetMapping("/profile/{id}")
    public MemberDto getUserProfile(@PathVariable("id") Long id) {
        return service.findMemberById(id);
    }
}
