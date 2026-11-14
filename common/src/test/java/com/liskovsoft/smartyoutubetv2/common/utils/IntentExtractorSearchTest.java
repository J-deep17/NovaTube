package com.liskovsoft.smartyoutubetv2.common.utils;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class IntentExtractorSearchTest {
    private static final String MEDIA_PLAY_FROM_SEARCH = "android.media.action.MEDIA_PLAY_FROM_SEARCH";

    // ---- new: query text in extras ----

    @Test
    public void actionSearchQueryExtraIsExtracted() {
        Intent intent = new Intent(Intent.ACTION_SEARCH).putExtra(SearchManager.QUERY, "linkin park");

        assertEquals("linkin park", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void mediaPlayFromSearchQueryExtraIsExtracted() {
        Intent intent = new Intent(MEDIA_PLAY_FROM_SEARCH).putExtra(SearchManager.QUERY, "numb");

        assertEquals("numb", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void queryIsTrimmed() {
        Intent intent = new Intent(Intent.ACTION_SEARCH).putExtra(SearchManager.QUERY, "  numb \n");

        assertEquals("numb", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void nonStringCharSequenceQueryIsAccepted() {
        Intent intent = new Intent(Intent.ACTION_SEARCH).putExtra(SearchManager.QUERY, new SpannableString("numb"));

        assertEquals("numb", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void userQueryExtraIsUsedWhenQueryIsMissingOrBlank() {
        Intent intent = new Intent(MEDIA_PLAY_FROM_SEARCH)
                .putExtra(SearchManager.QUERY, "   ")
                .putExtra("android.intent.extra.USER_QUERY", "in the end");

        assertEquals("in the end", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void searchIntentWithoutUsableTextGivesNull() {
        assertNull(IntentExtractor.extractSearchText(new Intent(Intent.ACTION_SEARCH)));
        assertNull(IntentExtractor.extractSearchText(new Intent(Intent.ACTION_SEARCH).putExtra(SearchManager.QUERY, " ")));
        assertNull(IntentExtractor.extractSearchText(new Intent(MEDIA_PLAY_FROM_SEARCH)));
    }

    @Test
    public void queryExtraOfOtherActionsIsIgnored() {
        // Only search actions are trusted to carry the query in extras
        Intent intent = new Intent(Intent.ACTION_MAIN).putExtra(SearchManager.QUERY, "numb");

        assertNull(IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void searchIntentIsNeverMistakenForAVideo() {
        Intent intent = new Intent(Intent.ACTION_SEARCH).putExtra(SearchManager.QUERY, "dQw4w9WgXcQ");

        assertNull(IntentExtractor.extractVideoId(intent));
    }

    @Test
    public void nullIntentGivesNull() {
        assertNull(IntentExtractor.extractSearchText(null));
    }

    // ---- unchanged behaviour that must keep working ----

    @Test
    public void browserResultsUrlStillWorks() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=twice"));

        assertEquals("twice", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void amazonYoutubeSearchUriStillWorks() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("youtube://search?query=linkin+park&isVoice=true"));

        assertEquals("linkin park", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void mediaPlayFromSearchWithUriAndNoExtrasStillUsesTheUri() {
        Intent intent = new Intent(MEDIA_PLAY_FROM_SEARCH, Uri.parse("youtube://search?query=numb"));

        assertEquals("numb", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void extrasWinOverUriWhenBothArePresent() {
        Intent intent = new Intent(MEDIA_PLAY_FROM_SEARCH, Uri.parse("youtube://search?query=old"))
                .putExtra(SearchManager.QUERY, "new");

        assertEquals("new", IntentExtractor.extractSearchText(intent));
    }

    @Test
    public void plainVideoLinkIsNotASearch() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));

        assertNull(IntentExtractor.extractSearchText(intent));
        assertEquals("dQw4w9WgXcQ", IntentExtractor.extractVideoId(intent));
    }
}
