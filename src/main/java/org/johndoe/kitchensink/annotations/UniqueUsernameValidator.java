package org.johndoe.kitchensink.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.stereotype.Component;

@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final MemberService memberService;

    public UniqueUsernameValidator(MemberService memberService) {
        this.memberService = memberService;
    }

    public

    @Override
    boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !memberService.doesValueExistsAsUsername(value);
    }
}
