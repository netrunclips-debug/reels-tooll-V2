package com.example.gesturetiktok.vision

/**
 * Sealed class representing various gesture events detected by the vision module.
 */
sealed class GestureEvent {
    data object SwipeDown : GestureEvent()
    data object SwipeUp : GestureEvent()
    data object Like : GestureEvent()
    data object None : GestureEvent()
    
    override fun toString(): String {
        return when (this) {
            is SwipeDown -> "SWIPE_DOWN"
            is SwipeUp -> "SWIPE_UP"
            is Like -> "LIKE"
            is None -> "NONE"
        }
    }
}
