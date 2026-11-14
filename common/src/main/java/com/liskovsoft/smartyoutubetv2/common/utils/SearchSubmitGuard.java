package com.liskovsoft.smartyoutubetv2.common.utils;

/**
 * Gate for search submissions coming from the search text field.
 * <p>
 * Deliberately free of Android dependencies and of a clock of its own (callers pass the time in),
 * so the behaviour can be unit tested on a plain JVM.
 * <p>
 * It protects against two things that can happen when text arrives through the system keyboard
 * (typing, or dictation started with the mic key of the Fire TV keyboard):
 * <ul>
 *     <li><b>Duplicate submissions</b>: some IMEs deliver both an editor action and an ENTER key
 *     event for one user action, and the search orb / Enter can both fire.</li>
 *     <li><b>Submitting text that is still being committed</b>: an editor action or ENTER that
 *     arrives within a very short window after the text last changed is treated as part of the
 *     same IME burst and is not submitted. A deliberate key press by the user always comes later.</li>
 * </ul>
 * This is a heuristic: nothing in the Android API tells the app that an IME is "still dictating".
 */
public final class SearchSubmitGuard {
    /** The same query submitted again inside this window is considered a duplicate. */
    public static final long DUPLICATE_WINDOW_MS = 1_000L;
    /**
     * A submit request this soon after the last text change is considered part of the IME's own
     * commit burst. Kept short so fast hardware-keyboard typing is not affected.
     */
    public static final long TEXT_SETTLE_WINDOW_MS = 200L;

    private boolean mHasTextChange;
    private long mLastTextChangeMs;
    private boolean mHasSubmitted;
    private String mLastSubmittedQuery;
    private long mLastSubmitMs;

    /** Call from the text field's TextWatcher (every change, typed, dictated or programmatic). */
    public void onTextChanged(long nowMs) {
        mHasTextChange = true;
        mLastTextChangeMs = nowMs;
    }

    /** True while the text changed so recently that a submit would likely race the IME. */
    public boolean isTextSettling(long nowMs) {
        return mHasTextChange && nowMs - mLastTextChangeMs < TEXT_SETTLE_WINDOW_MS;
    }

    /**
     * Records the submission and returns true if the query may be submitted now.
     * Returns false for a blank query or for a duplicate of the previous submission.
     */
    public boolean tryAcquire(String query, long nowMs) {
        String normalized = normalize(query);

        if (normalized.isEmpty()) {
            return false;
        }

        if (mHasSubmitted
                && normalized.equals(mLastSubmittedQuery)
                && nowMs - mLastSubmitMs < DUPLICATE_WINDOW_MS) {
            return false;
        }

        mHasSubmitted = true;
        mLastSubmittedQuery = normalized;
        mLastSubmitMs = nowMs;
        return true;
    }

    /** Forget the previous submission, e.g. when the user starts a new search session. */
    public void reset() {
        mHasTextChange = false;
        mHasSubmitted = false;
        mLastSubmittedQuery = null;
    }

    private static String normalize(String query) {
        return query == null ? "" : query.trim();
    }
}
