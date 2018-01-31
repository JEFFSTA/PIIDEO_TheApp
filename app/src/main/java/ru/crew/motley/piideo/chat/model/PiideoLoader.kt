package ru.crew.motley.piideo.chat.model

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.chat.db.PiideoRow
import ru.crew.motley.piideo.chat.db.PiideoSchema
import ru.crew.motley.piideo.fcm.FcmMessage
import ru.crew.motley.piideo.fcm.MessagingService
import ru.crew.motley.piideo.piideo.service.Recorder
import ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH
import java.io.File
import java.io.FileInputStream
import java.util.*


/**
 * Created by vas on 1/27/18.
 */
private data class SubjectItem(val piideoName: String, val from: String, val to: String)

class PiideoLoader(val context: Context) {

    private val sendPiideoSubject: ReplaySubject<SubjectItem> = ReplaySubject.create()
    private val uploaderAsync = sendPiideoSubject.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map {
                val storage = FirebaseStorage.getInstance("gs://piideo-e0fc2.appspot.com").reference
                val imageRef = storage.child(it.piideoName + ".jpg")
                val audioRef = storage.child(it.piideoName + ".mp4")
                val imageStream = FileInputStream(File(Recorder.HOME_PATH + it.piideoName + ".jpg"))
                val audioStream = FileInputStream(File(Recorder.HOME_PATH + it.piideoName + ".mp4"))
                val imageTask = imageRef.putStream(imageStream)
                val audioTask = audioRef.putStream(audioStream)
                Tasks.await(imageTask)
                Tasks.await(audioTask)
                sendMessage(it.piideoName, it.from, it.to)
                syncLocalDB(it.piideoName)
                0
            }
            .cache()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())


    fun send(piideoName: String, from: String, to: String): io.reactivex.Observable<Int> {
        return if (checkSynced(piideoName))
            io.reactivex.Observable.just(0)
                    .subscribeOn(Schedulers.io())
        else {
            uploaderAsync.subscribe()
            sendPiideoSubject.onNext(SubjectItem(piideoName, from, to))
            uploaderAsync
        }
    }

    private val receivePiideoSubjet: ReplaySubject<SubjectItem> = ReplaySubject.create()
    private val downloaderAsync = receivePiideoSubjet.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map {
                val storage = FirebaseStorage.getInstance("gs://piideo-e0fc2.appspot.com").reference
                val imageRef = storage.child(it.piideoName + ".jpg")
                val audioRef = storage.child(it.piideoName + ".mp4")
                val imageFile = File(HOME_PATH + it.piideoName + ".jpg")
                val audioFile = File(HOME_PATH + it.piideoName + ".mp4")
                Tasks.await(imageRef.getFile(imageFile))
                Tasks.await(audioRef.getFile(audioFile))
                saveLocalDb(it.piideoName)
                0
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .cache()

    fun receive(piideoName: String, from: String, to: String): io.reactivex.Observable<Int> {
        return if (checkExisted(piideoName))
            io.reactivex.Observable.just(0)
                    .subscribeOn(Schedulers.io())
        else {
            downloaderAsync.subscribe()
            receivePiideoSubjet.onNext(SubjectItem(piideoName, from, to))
            downloaderAsync
        }
    }

    fun checkSynced(piideoName: String): Boolean {
        val lab = ChatLab.get(context)
        val dbRow = lab.searchBy(piideoName) ?: return false
        return dbRow.piideoState == PiideoSchema.ChatTable.PIIDEO_STATE_DONE
    }

    fun checkExisted(piideoName: String): Boolean {
        val lab = ChatLab.get(context)
        return lab.searchBy(piideoName) != null
    }

    fun syncLocalDB(piideoName: String) {
        val lab = ChatLab.get(context)
        lab.setPiideoDone(piideoName)
    }

    fun saveLocalDb(piideoName: String) {
        val lab = ChatLab.get(context)
        val dbRow = PiideoRow()
        dbRow.piideoFileName = piideoName
        dbRow.piideoState = PiideoSchema.ChatTable.PIIDEO_STATE_DONE
        lab.addPiideo(dbRow)
    }

    fun sendMessage(piideoName: String, from: String, to: String) {
        val timestamp = timeInMillisGmt()
        val message = FcmMessage(
                timestamp,
                -timestamp,
                getDayTimestamp(timestamp),
                from,
                to,
                piideoName,
                MessagingService.PDO,
                from + "_" + to)

        FirebaseDatabase.getInstance()
                .reference
                .child("notifications")
                .child("messages")
                .push()
                .setValue(message)

        FirebaseDatabase.getInstance()
                .reference
                .child("messages")
                .child(to)
                .child(from)
                .push()
                .setValue(message)
    }


    private fun getDayTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        return calendar.timeInMillis
    }

    private fun timeInMillisGmt(): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT")).timeInMillis
    }

}