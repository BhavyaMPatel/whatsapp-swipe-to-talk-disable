package com.example.touchguard

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppScrollHelperService : AccessibilityService() {

    private val TAG = "TouchGuardAccessibility"
    private var lastActionTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "TouchGuard Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName != "com.whatsapp") return

        val now = SystemClock.uptimeMillis()
        if (now - lastActionTime < 500) return

        val rootNode = rootInActiveWindow ?: return

        if (scanAndDefuseVoiceSessions(rootNode)) {
            lastActionTime = now
        }

        rootNode.recycle()
    }

    private fun scanAndDefuseVoiceSessions(node: AccessibilityNodeInfo): Boolean {
        val desc = node.contentDescription?.toString()?.trim() ?: ""
        val text = node.text?.toString()?.trim() ?: ""

        val isVoiceChatTrigger = desc.equals("Leave call", ignoreCase = true) ||
                text.equals("Leave call", ignoreCase = true)

        val isVoiceNoteCancel = desc.equals("Cancel recording", ignoreCase = true) ||
                desc.equals("Delete recording", ignoreCase = true) ||
                desc.contains("Cancel voice message", ignoreCase = true) ||
                text.equals("Slide to cancel", ignoreCase = true)

        if (isVoiceChatTrigger || isVoiceNoteCancel) {
            Log.i(TAG, "Terminating voice session: desc='$desc', text='$text'")

            if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }

            val parent = node.parent
            if (parent != null) {
                if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    parent.recycle()
                    return true
                }
                parent.recycle()
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val handled = scanAndDefuseVoiceSessions(child)
            child.recycle()
            if (handled) return true
        }

        return false
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
    }
}