package com.preview.mousemacroapp.ui;

import com.preview.mousemacroapp.domain.timing.DelayPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DelayPolicyInputParserTest {

    @Test
    void parse_empty_returns_defaults() {
        UiParseResult<DelayPolicy> r = DelayPolicyInputParser.parse("", "", "");

        assertTrue(r.isOk());
        DelayPolicy p = r.value().orElseThrow();
        assertEquals(300, p.baseIntervalMillis());
        assertEquals(0, p.minRandomMillis());
        assertEquals(0, p.maxRandomMillis());
    }

    @Test
    void parse_min_only_sets_max_same_as_min() {
        UiParseResult<DelayPolicy> r = DelayPolicyInputParser.parse("100", "50", "");

        assertTrue(r.isOk());
        DelayPolicy p = r.value().orElseThrow();
        assertEquals(100, p.baseIntervalMillis());
        assertEquals(50, p.minRandomMillis());
        assertEquals(50, p.maxRandomMillis());
    }

    @Test
    void parse_min_greater_than_max_returns_error() {
        UiParseResult<DelayPolicy> r = DelayPolicyInputParser.parse("100", "60", "50");

        assertFalse(r.isOk());
        assertTrue(r.errorMessage().orElse("").contains("랜덤 최소"));
    }

    @Test
    void parse_negative_returns_error() {
        UiParseResult<DelayPolicy> r = DelayPolicyInputParser.parse("-1", "0", "0");

        assertFalse(r.isOk());
    }

    @Test
    void parse_non_number_returns_error() {
        UiParseResult<DelayPolicy> r = DelayPolicyInputParser.parse("abc", "0", "0");

        assertFalse(r.isOk());
    }
}
