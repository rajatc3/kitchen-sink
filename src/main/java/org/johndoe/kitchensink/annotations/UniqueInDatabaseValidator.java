package org.johndoe.kitchensink.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.johndoe.kitchensink.services.MemberService;
import org.springframework.stereotype.Component;

@Component
public class UniqueInDatabaseValidator implements ConstraintValidator<UniqueInDatabase, String> {
    private final MemberService memberService;

    public UniqueInDatabaseValidator(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !memberService.doesValueExistAsEmailOrPhoneNumber(value);
    }
}
