package com.example.touchguard

object CallDismissalPolicy {

    private val CALL_SCREEN_INDICATORS = setOf(
        "Minimise call",
        "Turn speaker phone on",
        "Add people to this call",
        "Start screen sharing"
    )

    private val GROUP_VOICE_CHAT_INDICATORS = setOf(
        "Message",
        "Wave all",
        "Connecting",
        "No one else is here yet",
        "Attach"
    )

    /**
     * Determines whether the current screen is a legitimate ongoing VoIP call.
     */
    fun isLegitimateCallScreen(visibleTexts: Set<String>): Boolean {
        return visibleTexts.any { it in CALL_SCREEN_INDICATORS }
    }

    /**
     * Confirms the visible context belongs to an accidental in-chat Voice Chat room.
     */
    fun isGroupVoiceChatContext(visibleTexts: Set<String>): Boolean {
        return visibleTexts.any { it in GROUP_VOICE_CHAT_INDICATORS }
    }

    /**
     * Decides whether the 'Leave call' button should be programmatically clicked.
     */
    fun shouldDismissCall(hasLeaveCallNode: Boolean, visibleTexts: Set<String>): Boolean {
        if (!hasLeaveCallNode) return false
        if (isLegitimateCallScreen(visibleTexts)) return false
        return isGroupVoiceChatContext(visibleTexts)
    }
}