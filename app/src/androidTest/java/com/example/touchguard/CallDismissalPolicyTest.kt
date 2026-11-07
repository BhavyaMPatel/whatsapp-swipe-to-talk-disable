package com.example.touchguard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallDismissalPolicyTest {

    @Test
    fun shouldDismissWhenLeaveCallIsPresentInsideGroupChatContext() {
        val visibleTexts = setOf("Message", "Attach", "Connecting")
        val shouldDismiss = CallDismissalPolicy.shouldDismissCall(
            hasLeaveCallNode = true,
            visibleTexts = visibleTexts
        )
        assertTrue(shouldDismiss)
    }

    @Test
    fun shouldNotDismissWhenScreenIsLegitimateVoipPhoneCall() {
        val visibleTexts = setOf("Minimise call", "Turn speaker phone on", "Add people to this call")
        val shouldDismiss = CallDismissalPolicy.shouldDismissCall(
            hasLeaveCallNode = true,
            visibleTexts = visibleTexts
        )
        assertFalse(shouldDismiss)
    }

    @Test
    fun shouldNotDismissIfScreenHasLegitimateCallIndicatorsEvenWithChatElements() {
        // Defensive check: Safety indicators always override chat indicators
        val visibleTexts = setOf("Minimise call", "Message")
        val shouldDismiss = CallDismissalPolicy.shouldDismissCall(
            hasLeaveCallNode = true,
            visibleTexts = visibleTexts
        )
        assertFalse(shouldDismiss)
    }

    @Test
    fun shouldNotDismissIfLeaveCallButtonIsAbsent() {
        val visibleTexts = setOf("Message", "Attach")
        val shouldDismiss = CallDismissalPolicy.shouldDismissCall(
            hasLeaveCallNode = false,
            visibleTexts = visibleTexts
        )
        assertFalse(shouldDismiss)
    }

    @Test
    fun shouldNotDismissIfLeaveCallIsPresentInUnknownScreenWithoutChatContext() {
        val visibleTexts = setOf("Settings", "Account", "Chats")
        val shouldDismiss = CallDismissalPolicy.shouldDismissCall(
            hasLeaveCallNode = true,
            visibleTexts = visibleTexts
        )
        assertFalse(shouldDismiss)
    }
}