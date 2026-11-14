package com.liskovsoft.smartyoutubetv2.common.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchSubmitGuardTest {
    @Test
    public void firstSubmissionIsAllowed() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        assertTrue(guard.tryAcquire("linkin park", 10_000));
    }

    @Test
    public void blankOrNullQueryIsNeverSubmitted() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        assertFalse(guard.tryAcquire(null, 10_000));
        assertFalse(guard.tryAcquire("", 10_000));
        assertFalse(guard.tryAcquire("   ", 10_000));
    }

    @Test
    public void sameQueryInsideWindowIsDuplicate() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        assertTrue(guard.tryAcquire("linkin park", 10_000));
        // e.g. IME sends an editor action AND an ENTER key event for one press
        assertFalse(guard.tryAcquire("linkin park", 10_050));
        assertFalse(guard.tryAcquire("  linkin park  ", 10_000 + SearchSubmitGuard.DUPLICATE_WINDOW_MS - 1));
    }

    @Test
    public void sameQueryAfterWindowIsAllowedAgain() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        assertTrue(guard.tryAcquire("linkin park", 10_000));
        assertTrue(guard.tryAcquire("linkin park", 10_000 + SearchSubmitGuard.DUPLICATE_WINDOW_MS));
    }

    @Test
    public void differentQueryIsNotADuplicate() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        assertTrue(guard.tryAcquire("linkin park", 10_000));
        assertTrue(guard.tryAcquire("linkin park numb", 10_010));
    }

    @Test
    public void rejectedDuplicateDoesNotExtendTheWindow() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        assertTrue(guard.tryAcquire("a", 10_000));
        assertFalse(guard.tryAcquire("a", 10_900));
        // window is measured from the accepted submission, not from the rejected attempt
        assertTrue(guard.tryAcquire("a", 11_000));
    }

    @Test
    public void notSettlingBeforeAnyTextChange() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        assertFalse(guard.isTextSettling(0));
        assertFalse(guard.isTextSettling(10_000));
    }

    @Test
    public void settlingRightAfterTextChangeOnly() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        guard.onTextChanged(5_000);

        assertTrue(guard.isTextSettling(5_000));
        assertTrue(guard.isTextSettling(5_000 + SearchSubmitGuard.TEXT_SETTLE_WINDOW_MS - 1));
        assertFalse(guard.isTextSettling(5_000 + SearchSubmitGuard.TEXT_SETTLE_WINDOW_MS));
    }

    @Test
    public void everyTextChangeRestartsTheSettleWindow() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        guard.onTextChanged(5_000);
        guard.onTextChanged(5_150); // e.g. a second partial dictation commit

        assertTrue(guard.isTextSettling(5_300));
        assertFalse(guard.isTextSettling(5_150 + SearchSubmitGuard.TEXT_SETTLE_WINDOW_MS));
    }

    @Test
    public void deliberateEnterAfterDictationIsNotBlocked() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        guard.onTextChanged(5_000); // dictation committed the words
        // user looks at the text, edits nothing, presses Enter a moment later
        long enterAt = 5_000 + 1_500;

        assertFalse(guard.isTextSettling(enterAt));
        assertTrue(guard.tryAcquire("linkin park", enterAt));
    }

    @Test
    public void resetForgetsPreviousSubmissionAndTextChange() {
        SearchSubmitGuard guard = new SearchSubmitGuard();

        guard.onTextChanged(5_000);
        assertTrue(guard.tryAcquire("a", 5_000));

        guard.reset();

        assertFalse(guard.isTextSettling(5_001));
        assertTrue(guard.tryAcquire("a", 5_002));
    }
}
