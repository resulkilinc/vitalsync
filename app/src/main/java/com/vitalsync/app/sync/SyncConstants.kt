package com.vitalsync.app.sync

object SyncConstants {
    const val KIND_BP = "BP"
    const val KIND_GLUCOSE = "GLUCOSE"
    const val KIND_HEART_RATE = "HEART_RATE"
    const val KIND_OXYGEN = "OXYGEN"

    const val STATUS_PENDING = "PENDING"
    const val STATUS_FAILED = "FAILED"

    const val META_REMOTE_SNIPPET = "remote_post_snippet"
    const val META_LAST_UPLOAD_AT = "last_upload_at_ms"

    const val WORK_UNIQUE_PERIODIC = "vitalsync_periodic_upload"
    const val WORK_UNIQUE_ONCE = "vitalsync_upload_once"

    const val MAX_ATTEMPTS = 5
}
