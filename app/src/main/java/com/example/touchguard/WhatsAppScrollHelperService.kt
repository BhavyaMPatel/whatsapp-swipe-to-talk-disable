package com.example.touchguard

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppScrollHelperService : AccessibilityService() {

    private var lastActionTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName != "com.whatsapp") return

        // Reduced throttle window to prevent double-clicks without delaying the first click
        val now = SystemClock.uptimeMillis()
        if (now - lastActionTime < 150) return

        val rootNode = rootInActiveWindow ?: return

        // 1. Instant check for "Leave call" via native B-tree search (Near 0ms)
        val leaveNodes = rootNode.findAccessibilityNodeInfosByText("Leave call")
        if (leaveNodes.isNotEmpty()) {
            for (node in leaveNodes) {
                if (performFastClick(node)) {
                    lastActionTime = now
                    recycleNodes(leaveNodes)
                    rootNode.recycle()
                    return
                }
            }
            recycleNodes(leaveNodes)
        }

        // 2. Instant check for Voice Note Cancel
        val cancelNodes = rootNode.findAccessibilityNodeInfosByText("Cancel")
        if (cancelNodes.isNotEmpty()) {
            for (node in cancelNodes) {
                val desc = node.contentDescription?.toString()?.trim() ?: ""
                val text = node.text?.toString()?.trim() ?: ""
                if (desc.contains("cancel recording", ignoreCase = true) ||
                    desc.contains("delete recording", ignoreCase = true) ||
                    text.equals("Slide to cancel", ignoreCase = true)) {
                    if (performFastClick(node)) {
                        lastActionTime = now
                        recycleNodes(cancelNodes)
                        rootNode.recycle()
                        return
                    }
                }
            }
            recycleNodes(cancelNodes)
        }

        rootNode.recycle()
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