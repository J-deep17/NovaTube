package com.liskovsoft.smartyoutubetv2.common.utils;

import com.liskovsoft.leanbackassistant.search.SearchQueryUrl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SearchQueryUrlTest {
    private static final String BASE = "https://www.youtube.com/results?search_query=";

    @Test
    public void nullAndBlankQueriesGiveNoUrl() {
        assertNull(SearchQueryUrl.build(null));
        assertNull(SearchQueryUrl.build(""));
        assertNull(SearchQueryUrl.build("   "));
    }

    @Test
    public void plainQueryIsEncoded() {
        assertEquals(BASE + "linkin+park", SearchQueryUrl.build("linkin park"));
    }

    @Test
    public void queryIsTrimmed() {
        assertEquals(BASE + "linkin+park", SearchQueryUrl.build("  linkin park \n"));
    }

    @Test
    public void reservedCharactersCannotInjectParameters() {
        // '&' and '#' must not terminate the search_query parameter
        assertEquals(BASE + "a%26b%3Dc%23d", SearchQueryUrl.build("a&b=c#d"));
    }

    @Test
    public void nonLatinSpeechIsUtf8Encoded() {
        // Hindi: "गाने" (songs)
        assertEquals(BASE + "%E0%A4%97%E0%A4%BE%E0%A4%A8%E0%A5%87", SearchQueryUrl.build("गाने"));
    }

    @Test
    public void acceptsAnyCharSequence() {
        assertEquals(BASE + "numb", SearchQueryUrl.build(new StringBuilder("numb")));
    }
}
