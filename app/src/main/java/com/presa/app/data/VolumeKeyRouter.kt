package com.presa.app.data

class VolumeKeyRouter {
    @Volatile
    var handler: ((isNext: Boolean, isDown: Boolean, repeatCount: Int) -> Boolean)? = null

    fun dispatch(isNext: Boolean, isDown: Boolean, repeatCount: Int): Boolean =
        handler?.invoke(isNext, isDown, repeatCount) ?: false
}
