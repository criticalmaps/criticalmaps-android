package de.stephanlindauer.criticalmaps.model

import kotlin.random.Random

class PermissionRequest(
    val permissions: Array<String>,
    val rationale: String,
    onGrantedCallback: Runnable? = null,
    onDeniedCallback: Runnable? = null,
    onPermanentlyDeniedCallback: Runnable? = null
) {
    val onGrantedCallback: Runnable = onGrantedCallback ?: Runnable { }
    val onDeniedCallback: Runnable = onDeniedCallback ?: Runnable { }
    val onPermanentlyDeniedCallback: Runnable = onPermanentlyDeniedCallback ?: Runnable { }

    // Can only use lower 16 bits for requestCode --> short
    val requestCode: Int = Random.nextInt(Short.MAX_VALUE.toInt())
}
