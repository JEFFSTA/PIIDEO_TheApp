package ru.crew.motley.piideo.fcm

import android.os.Environment
import ru.crew.motley.piideo.piideo.service.Recorder.HOME_PATH
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * Created by vas on 2/16/18.
 */
class MessageLogger {

    private val logFileName = HOME_PATH + "piideo.log"

    fun saveToLogFile(source: String, time: Long) {
        if (!isExternalStorageWritable()) {
            throw RuntimeException("Log couldn't be saved due to writing problems")
            return
        }
        if (!File(logFileName).exists()) {
            File(logFileName).createNewFile()
        }
        try {
            val writer = FileOutputStream(File(logFileName), true)
            val stream = OutputStreamWriter(writer)
            val logString = String.format("%1$40s : %2$10d", source, time)
            stream.append(logString)
            stream.flush()
            stream.close()
            writer.close()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

}