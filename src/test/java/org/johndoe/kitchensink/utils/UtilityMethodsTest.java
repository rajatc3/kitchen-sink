package org.johndoe.kitchensink.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilityMethodsTest {

    @Test
    void maskEmail_ShouldMaskProperly() {
        assertEquals("j****e@email.com", UtilityMethods.maskEmail("john.doe@email.com"));
        assertEquals("a****b@xyz.com", UtilityMethods.maskEmail("aliceb@xyz.com"));
        assertEquals("a****@xyz.com", UtilityMethods.maskEmail("ab@xyz.com")); // Short username
        assertEquals("x****@domain.com", UtilityMethods.maskEmail("x@domain.com")); // Single-char username
        assertEquals("*****", UtilityMethods.maskEmail(null)); // Null email
        assertEquals("*****", UtilityMethods.maskEmail("invalidemail")); // Missing '@'
    }

    @Test
    void maskPhone_ShouldMaskProperly() {
        assertEquals("98****10", UtilityMethods.maskPhone("9876543210"));
        assertEquals("12****89", UtilityMethods.maskPhone("123456789"));
        assertEquals("****", UtilityMethods.maskPhone("123")); // Short number
        assertEquals("****", UtilityMethods.maskPhone(null)); // Null phone
    }

    @Test
    void paginateResponse_ShouldReturnCorrectMetadata() {
        List<String> data = List.of("Item1", "Item2");
        Page<String> page = new PageImpl<>(data, PageRequest.of(1, 2), 5);

        Map<String, Object> response = UtilityMethods.paginateResponse(page);

        assertEquals(data, response.get("content"));
        assertEquals(1, response.get("currentPage"));
        assertEquals(3, response.get("totalPages")); // Total 5 elements, page size 2 => 3 pages
        assertEquals(5L, response.get("totalElements"));
        assertEquals(2, response.get("pageSize"));
        assertEquals(false, response.get("isLast"));
    }

    @Test
    void paginateResponse_ShouldHandleEmptyPage() {
        Page<String> emptyPage = Page.empty();

        Map<String, Object> response = UtilityMethods.paginateResponse(emptyPage);

        assertTrue(((List<?>) response.get("content")).isEmpty());
        assertEquals(0, response.get("currentPage"));
        assertEquals(1, response.get("totalPages"));
        assertEquals(0L, response.get("totalElements"));
        assertEquals(0, response.get("pageSize"));
        assertEquals(true, response.get("isLast"));
    }
}
