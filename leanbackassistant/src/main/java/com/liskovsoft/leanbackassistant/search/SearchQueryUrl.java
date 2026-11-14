package com.liskovsoft.leanbackassistant.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Builds the URL used to hand a plain-text search query to the app's main activity.
 * <p>
 * The app already understands {@code https://www.youtube.com/results?search_query=...}
 * (see {@code IntentExtractor.SEARCH_KEYS}), so forwarding through it reuses the existing,
 * proven search route instead of introducing a new one.
 * <p>
 * No Android dependencies on purpose: this is unit tested on a plain JVM.
 */
public final class SearchQueryUrl {
    private static final String RESULTS_URL = "https://www.youtube.com/results?search_query=";

    private SearchQueryUrl() {
    }

    /** @return results URL for the query, or null if the query is null or blank */
    public static String build(CharSequence query) {
        if (query == null) {
            return null;
        }

        String trimmed = query.toString().trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            return RESULTS_URL + URLEncoder.encode(trimmed, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported by the JVM
            throw new IllegalStateException(e);
        }
    }
}
