package com.vitalsync.app.sync

import com.google.gson.Gson
import com.vitalsync.app.data.db.dao.SyncMetaDao
import com.vitalsync.app.data.db.dao.SyncOutboxDao
import com.vitalsync.app.data.db.entities.SyncMetaEntity
import com.vitalsync.app.data.db.entities.SyncOutboxEntity
import com.vitalsync.app.data.remote.VitalUploadRequestDto
import com.vitalsync.app.data.remote.VitalsRemoteApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncFlushOutcome {
    Success,
    Retry,
}

@Singleton
class SyncRepository @Inject constructor(
    private val outboxDao: SyncOutboxDao,
    private val metaDao: SyncMetaDao,
    private val api: VitalsRemoteApi,
    private val gson: Gson,
    private val scheduler: SyncScheduler,
) {

    fun observePendingCount(): Flow<Int> = outboxDao.observePendingCount()

    fun observeRemoteSnippet(): Flow<String?> =
        metaDao.observe(SyncConstants.META_REMOTE_SNIPPET).map { it?.value }

    fun requestImmediateSync() {
        scheduler.enqueueImmediateSync()
    }

    suspend fun enqueueVitalPayload(
        userId: Long,
        vitalKind: String,
        localRecordId: Long,
        payload: Map<String, Any?>,
    ) {
        val json = gson.toJson(payload)
        outboxDao.insert(
            SyncOutboxEntity(
                userId = userId,
                vitalKind = vitalKind,
                localRecordId = localRecordId,
                payloadJson = json,
                status = SyncConstants.STATUS_PENDING,
            ),
        )
        scheduler.enqueueImmediateSync()
    }

    /**
     * WorkManager tarafından çağrılır: bekleyen kayıtları uzak demo API'ye gönderir.
     */
    suspend fun flushPending(batchSize: Int = 15): SyncFlushOutcome {
        val pending = outboxDao.getPending(batchSize)
        if (pending.isEmpty()) {
            refreshRemoteSnippetBestEffort()
            return SyncFlushOutcome.Success
        }

        var needsRetry = false
        val now = System.currentTimeMillis()

        for (row in pending) {
            try {
                val req = buildPlaceholderRequest(row)
                val resp = api.uploadPost(req)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    val remoteId = body?.id
                    outboxDao.deleteById(row.id)
                    metaDao.upsert(
                        SyncMetaEntity(
                            key = SyncConstants.META_LAST_UPLOAD_AT,
                            value = now.toString(),
                            updatedAt = now,
                        ),
                    )
                    val snippet = buildSnippet(remoteId, body?.title, body?.body)
                    metaDao.upsert(
                        SyncMetaEntity(
                            key = SyncConstants.META_REMOTE_SNIPPET,
                            value = snippet,
                            updatedAt = now,
                        ),
                    )
                } else {
                    markFailure(row, "HTTP ${resp.code()}", now)
                    if (resp.code() in 500..599) needsRetry = true
                }
            } catch (_: IOException) {
                needsRetry = true
                markFailure(row, "Ağ hatası — çevrimdışı veya zaman aşımı", now)
            } catch (e: Exception) {
                markFailure(row, e.message ?: "Bilinmeyen hata", now)
            }
        }

        refreshRemoteSnippetBestEffort()

        return if (needsRetry) SyncFlushOutcome.Retry else SyncFlushOutcome.Success
    }

    private suspend fun refreshRemoteSnippetBestEffort() {
        try {
            val resp = api.getPost(1)
            if (!resp.isSuccessful) return
            val p = resp.body() ?: return
            val snippet = buildSnippet(p.id, p.title, p.body)
            val now = System.currentTimeMillis()
            metaDao.upsert(
                SyncMetaEntity(
                    key = SyncConstants.META_REMOTE_SNIPPET,
                    value = snippet,
                    updatedAt = now,
                ),
            )
        } catch (_: Exception) {
            // Demo meta — sessiz geç
        }
    }

    private suspend fun markFailure(row: SyncOutboxEntity, message: String, now: Long) {
        val nextAttempts = row.attempts + 1
        val status =
            if (nextAttempts >= SyncConstants.MAX_ATTEMPTS) SyncConstants.STATUS_FAILED
            else SyncConstants.STATUS_PENDING

        outboxDao.updateAfterAttempt(
            id = row.id,
            attempts = nextAttempts,
            error = message.take(500),
            status = status,
            updatedAt = now,
        )
    }

    private fun buildPlaceholderRequest(row: SyncOutboxEntity): VitalUploadRequestDto {
        val title = "[VitalÖlçüm:${row.vitalKind}] user=${row.userId} local=${row.localRecordId}"
        val body = row.payloadJson
        return VitalUploadRequestDto(
            title = title.take(180),
            body = body.take(4000),
            userId = row.userId.coerceIn(1, 999_999),
        )
    }

    private fun buildSnippet(remoteId: Long?, title: String?, body: String?): String {
        val t = title?.take(80) ?: ""
        val b = body?.take(120)?.replace('\n', ' ') ?: ""
        return "remoteId=$remoteId · $t · $b".trim()
    }
}
