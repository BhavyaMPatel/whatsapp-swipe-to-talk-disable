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
        Log.i(TAG, "TouchGuard Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName != "com.whatsapp") return

        val now = SystemClock.uptimeMillis()
        if (now - lastActionTime < 300) return

        val rootNode = rootInActiveWindow ?: return

        // Check if "Leave call" is present on screen
        val leaveNodes = rootNode.findAccessibilityNodeInfosByText("Leave call")
        if (leaveNodes.isNotEmpty()) {
            val visibleTexts = extractKeyIndicators(rootNode)

            if (CallDismissalPolicy.shouldDismissCall(hasLeaveCallNode = true, visibleTexts = visibleTexts)) {
                for (node in leaveNodes) {
                    if (performFastClick(node)) {
                        Log.i(TAG, "Auto-dismissed accidental Group Voice Chat")
                        lastActionTime = now
                        recycleNodes(leaveNodes)
                        rootNode.recycle()
                        return
                    }
                }
            }
            recycleNodes(leaveNodes)
        }

        rootNode.recycle()
    }

    private fun extractKeyIndicators(root: AccessibilityNodeInfo): Set<String> {
        val candidates = listOf(
            "Minimise call", "Turn speaker phone on", "Add people to this call", "Start screen sharing",
            "Message", "Wave all", "Connecting", "No one else is here yet", "Attach"
        )
        val found = mutableSetOf<String>()
        for (candidate in candidates) {
            val nodes = root.findAccessibilityNodeInfosByText(candidate)
            if (nodes.isNotEmpty()) {
                found.add(candidate)
                recycleNodes(nodes)
            }
        }
        return found
    }

    private fun performFastClick(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true
        }
        val parent = node.parent
        if (parent != null) {
            val clicked = parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            parent.recycle()
            return clicked
        }
        return false
    }

    private fun recycleNodes(nodes: List<AccessibilityNodeInfo>) {
        for (node in nodes) {
            node.recycle()
        }
    }

    override fun onInterrupt() {}
}