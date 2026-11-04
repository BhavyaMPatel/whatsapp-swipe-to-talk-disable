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

        // 1. Check if this is a legitimate phone call window
        if (isLegitimateCallScreen(rootNode)) {
            rootNode.recycle()
            return
        }

        // 2. Defuse accidental Group Voice Chat (Leave call inside chat view)
        val leaveNodes = rootNode.findAccessibilityNodeInfosByText("Leave call")
        if (leaveNodes.isNotEmpty()) {
            if (isGroupVoiceChatContext(rootNode)) {
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

    /**
     * Identifies legitimate ongoing 1-on-1 or regular group audio/video calls.
     */
    private fun isLegitimateCallScreen(root: AccessibilityNodeInfo): Boolean {
        val hasMinimise = root.findAccessibilityNodeInfosByText("Minimise call").isNotEmpty()
        val hasSpeaker = root.findAccessibilityNodeInfosByText("Turn speaker phone on").isNotEmpty()
        val hasAddPeople = root.findAccessibilityNodeInfosByText("Add people to this call").isNotEmpty()
        val hasScreenShare = root.findAccessibilityNodeInfosByText("Start screen sharing").isNotEmpty()

        return hasMinimise || hasSpeaker || hasAddPeople || hasScreenShare
    }

    /**
     * Confirms the "Leave call" button belongs to an accidental in-chat Voice Chat room.
     */
    private fun isGroupVoiceChatContext(root: AccessibilityNodeInfo): Boolean {
        val hasMessageBar = root.findAccessibilityNodeInfosByText("Message").isNotEmpty()
        val hasWaveAll = root.findAccessibilityNodeInfosByText("Wave all").isNotEmpty()
        val hasConnecting = root.findAccessibilityNodeInfosByText("Connecting").isNotEmpty()
        val hasNoOneHere = root.findAccessibilityNodeInfosByText("No one else is here yet").isNotEmpty()
        val hasAttach = root.findAccessibilityNodeInfosByText("Attach").isNotEmpty()

        return hasMessageBar || hasWaveAll || hasConnecting || hasNoOneHere || hasAttach
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