package ru.crew.motley.piideo.chat.model

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.chat.db.PiideoRow
import ru.crew.motley.piideo.chat.db.PiideoSchema
import ru.crew.motley.piideo.fcm.FcmMessage
import ru.crew.motley.piideo.fcm.MessagingService
import ru.crew.motley.piideo.piideo.service.Recorder
import ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH
import ru.crew.motley.piideo.util.TimeUtils
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * Created by vas on 1/27/18.
 */
private data class SubjectItem(val piideoName: String, val from: String, val to: String)

class PiideoLoader(val context: Context) {

    companion object {
        val TAG = PiideoLoader::class.java.simpleName!!
    }

    private val tasks = ConcurrentHashMap<String, io.reactivex.Single<Int>>()

    private val sendPiideoSubject: BehaviorSubject<SubjectItem> = BehaviorSubject.create()
    private val uploaderAsync = sendPiideoSubject.cache()
            .observeOn(Schedulers.io())
            .map {
                val storage = FirebaseStorage.getInstance("gs://piideo-e0fc2.appspot.com").reference
                val imageRef = storage.child(it.piideoName + ".jpg")
                val audioRef = storage.child(it.piideoName + ".mp4")
                val imageStream = FileInputStream(File(Recorder.HOME_PATH + it.piideoName + ".jpg"))
                val audioStream = FileInputStream(File(Recorder.HOME_PATH + it.piideoName + ".mp4"))
                val imageTask = imageRef.putStream(imageStream)
                val audioTask = audioRef.putStream(audioStream)
                Log.d(TAG, "upload start " + it.piideoName)
                Tasks.await(imageTask)
                Tasks.await(audioTask)
                sendMessage(it.piideoName, it.from, it.to)
                syncLocalDB(it.piideoName)
                Log.d(TAG, "upload end " + it.piideoName)
                0
            }
            .cache()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())


    fun send(piideoName: String, from: String, to: String): io.reactivex.Observable<Int> {
        Log.d(TAG, "download msg send " + piideoName + " " + from + " " + to)
        return if (checkSynced(piideoName))
            io.reactivex.Observable.just(0)
                    .subscribeOn(Schedulers.io())
        else {
//            uploaderAsync.subscribe()
            sendPiideoSubject.onNext(SubjectItem(piideoName, from, to))
            uploaderAsync
        }
    }

    fun send0(piideoName: String, from: String, to: String): io.reactivex.Single<Int> {
        createRootFolder()
        Log.d(TAG, "send " + piideoName)
        if (checkSynced(piideoName)) {
            Log.d(TAG, " return just ");
            return io.reactivex.Single.just(0)
        } else {
            if (tasks.contains(piideoName)) {
                Log.d(TAG, " return existed ")
                return tasks[piideoName]!!
            }
            val sendTask = io.reactivex.Single.just(SubjectItem(piideoName, from, to))
                    .map {
                        val storage = FirebaseStorage.getInstance("gs://piideo-e0fc2.appspot.com").reference
                        val imageRef = storage.child(it.piideoName + ".jpg")
                        val audioRef = storage.child(it.piideoName + ".mp4")
                        val imageStream = FileInputStream(File(Recorder.HOME_PATH + it.piideoName + ".jpg"))
                        val audioStream = FileInputStream(File(Recorder.HOME_PATH + it.piideoName + ".mp4"))
                        val imageTask = imageRef.putStream(imageStream)
                        val audioTask = audioRef.putStream(audioStream)
                        Log.d(TAG, "upload start " + it.piideoName)
                        syncLocalDB(it.piideoName)
                        Tasks.await(imageTask)
                        Tasks.await(audioTask)
                        sendMessage(it.piideoName, it.from, it.to)
                        Log.d(TAG, "upload end " + it.piideoName)
                        tasks.remove(piideoName)
                        0
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache()
            tasks.put(piideoName, sendTask)
            return sendTask
        }
    }

    private val receivePiideoSubjet: BehaviorSubject<SubjectItem> = BehaviorSubject.create()
    private val downloaderAsync = receivePiideoSubjet.cache().subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map {
                val storage = FirebaseStorage.getInstance("gs://piideo-e0fc2.appspot.com").reference
                val imageRef = storage.child(it.piideoName + ".jpg")
                val audioRef = storage.child(it.piideoName + ".mp4")
                val imageFile = File(HOME_PATH + it.piideoName + ".jpg")
                val audioFile = File(HOME_PATH + it.piideoName + ".mp4")
                Log.d(TAG, "download start " + it.piideoName)
                saveLocalDb(it.piideoName)
                Tasks.await(imageRef.getFile(imageFile))
                Tasks.await(audioRef.getFile(audioFile))
                Log.d(TAG, "download end " + it.piideoName)
                0
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .cache()

    fun receive(piideoName: String, from: String, to: String): io.reactivex.Observable<Int> {
        createRootFolder()
        Log.d(TAG, "download msg receive " + piideoName + " " + from + " " + to)
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
        if (from == to) {
            throw RuntimeException("From To equal to each other");
        }
        if (from != FirebaseAuth.getInstance().uid) {
            throw RuntimeException("From is not equal to auth uid");
        }
        if (from != FirebaseAuth.getInstance().currentUser?.uid) {
            throw RuntimeException("From is not equal to auth user uid")
        }
        val timestamp = TimeUtils.gmtTimeInMillis()
        val message = FcmMessage(
                timestamp,
                -timestamp,
                TimeUtils.Companion.gmtDayTimestamp(timestamp),
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

    private fun createRootFolder() {
        val piideoFolder = File(Recorder.HOME_PATH)
        if (!piideoFolder.exists()) {
            piideoFolder.mkdir()
        }
    }

}