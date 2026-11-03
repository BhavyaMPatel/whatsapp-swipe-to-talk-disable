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
        DeadZoneOverlayService.accessibilityInstance = this
        Log.i(TAG, "TouchGuard Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName != "com.whatsapp") return

        val now = SystemClock.uptimeMillis()
        if (now - lastActionTime < 500) return

        val rootNode = rootInActiveWindow ?: return

        // 1. Defuse Group Voice Chat ("Leave call" / "Wave all")
        // 2. Defuse Voice Note recording locks ("Cancel recording" / "Delete")
        if (scanAndDefuseVoiceSessions(rootNode)) {
            lastActionTime = now
        }

        rootNode.recycle()
    }

    /**
     * Recursively searches for WhatsApp Voice Chat or Voice Note elements
     * and performs a click to terminate/leave immediately.
     */
    private fun scanAndDefuseVoiceSessions(node: AccessibilityNodeInfo): Boolean {
        val desc = node.contentDescription?.toString()?.trim() ?: ""
        val text = node.text?.toString()?.trim() ?: ""

        // Target: WhatsApp Group Voice Chat "Leave call" button
        val isVoiceChatTrigger = desc.equals("Leave call", ignoreCase = true) ||
                text.equals("Leave call", ignoreCase = true)

        // Target: WhatsApp 1-on-1 Voice Note cancel/trash button
        val isVoiceNoteCancel = desc.equals("Cancel recording", ignoreCase = true) ||
                desc.equals("Delete recording", ignoreCase = true) ||
                desc.contains("Cancel voice message", ignoreCase = true) ||
                text.equals("Slide to cancel", ignoreCase = true)

        if (isVoiceChatTrigger || isVoiceNoteCancel) {
            Log.i(TAG, "Detected voice trigger: desc='$desc', text='$text'. Terminating...")

            // Try clicking the node directly
            if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Successfully clicked target node")
                return true
            }

            // If the clickable boundary is on the parent wrapper
            val parent = node.parent
            if (parent != null) {
                if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.i(TAG, "Successfully clicked parent wrapper")
                    parent.recycle()
                    return true
                }
                parent.recycle()
            }
        }

        // Recurse through all child nodes
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val handled = scanAndDefuseVoiceSessions(child)
            child.recycle()
            if (handled) return true
        }

        return false
    }

    /**
     * Translates upward flicks on the dead zone into forward scrolling inside chats.
     */
    fun performChatScroll(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(rootNode)
        val scrolled = scrollableNode?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        scrollableNode?.recycle()
        rootNode.recycle()
        return scrolled
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
            child.recycle()
        }
        return null
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        DeadZoneOverlayService.accessibilityInstance = null
    }
}