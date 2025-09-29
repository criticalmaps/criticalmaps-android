package de.stephanlindauer.criticalmaps.model

import kotlin.random.Random

class PermissionRequest(
    val permissions: Array<String>,
    val rationale: String,
    val onGrantedCallback: Runnable = Runnable { },
    val onDeniedCallback: Runnable = Runnable { },
    val onPermanentlyDeniedCallback: Runnable = Runnable { }
) {
    // Can only use lower 16 bits for requestCode --> short
    val requestCode: Int = Random.nextInt(Short.MAX_VALUE.toInt())
}