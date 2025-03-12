package org.johndoe.kitchensink;

import org.johndoe.kitchensink.advices.GlobalExceptionHandlerTest;
import org.johndoe.kitchensink.controllers.MemberControllerTest;
import org.johndoe.kitchensink.services.MemberServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;


@Suite
@SuiteDisplayName("Run All Tests in the Project")
@SelectPackages("org.johndoe.kitchensink")
@SelectClasses({
        MemberControllerTest.class,
        MemberServiceTest.class,
        GlobalExceptionHandlerTest.class
})
public class AllTests {
}
