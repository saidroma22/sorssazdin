package music.saidweb.playlist.offline.Ringtone

import android.content.ContentValues
import android.content.Context
import android.content.res.AssetManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.*

class RingToneOperation(val context: Context){

    fun SetAsRingtoneOrNotification(k: File, type: Int): Boolean {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.TITLE, k.name)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
        if (RingtoneManager.TYPE_RINGTONE == type) {
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        } else if (RingtoneManager.TYPE_ALARM == type) {
            values.put(MediaStore.Audio.Media.IS_ALARM, true)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val newUri = context.contentResolver
                .insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            try {
                context.contentResolver.openOutputStream(newUri!!).use { os ->
                    val size = k.length().toInt()
                    val bytes = ByteArray(size)
                    try {
                        val buf = BufferedInputStream(FileInputStream(k))
                        buf.read(bytes, 0, bytes.size)
                        buf.close()
                        os!!.write(bytes)
                        os.close()
                        os.flush()
                    } catch (e: IOException) {
                        return false
                    }
                }
            } catch (ignored: java.lang.Exception) {
                return false
            }
            RingtoneManager.setActualDefaultRingtoneUri(
                context, type,
                newUri
            )
            true
        } else {
            values.put(MediaStore.MediaColumns.DATA, k.absolutePath)
            val uri = MediaStore.Audio.Media.getContentUriForPath(
                k
                    .absolutePath
            )
            context.contentResolver.delete(
                uri!!,
                MediaStore.MediaColumns.DATA + "=\"" + k.absolutePath + "\"",
                null
            )
            val newUri: Uri = context.contentResolver.insert(uri, values)!!
            RingtoneManager.setActualDefaultRingtoneUri(
                context, type,
                newUri
            )
            context.contentResolver
                .insert(
                    MediaStore.Audio.Media.getContentUriForPath(
                        k
                            .absolutePath
                    )!!, values
                )
            true
        }
    }

    fun createRingToneFile(ringtoneName: String): Operation {

        val exStoragePath = Environment.getExternalStorageDirectory().absolutePath
        val path = "$exStoragePath/media/alarms/"

        //Get Audio File From Raw Folder
        var buffer: ByteArray? = null
        //val rawFileInput = context.resources.openRawResource(rawFileId)
        val rawFileInput:InputStream = context.assets.open("$ringtoneName.mp3")
        println("------- File Path: $ringtoneName.mp3")
        var size = 0

        //Convert File Into Buffer
        try {
            size = rawFileInput.available()
            buffer = ByteArray(size)
            rawFileInput.read(buffer)
            rawFileInput.close()
        } catch (e: IOException) {
            println("A")
            e.printStackTrace()
            Log.i("Exception", "createRingToneFile: " + e.message)
            return Operation(false, null)
        }

        //check if the path existed, if not, it'll create the path folders
        val exists: Boolean = File(path).exists()
        if (!exists) {
            File(path).mkdirs()
        }

        // Create/write the RingTone file in external path
        val save: FileOutputStream
        try {
            save = FileOutputStream(path + ringtoneName)
            save.write(buffer)
            save.flush()
            save.close()
        } catch (e: FileNotFoundException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                println("${Environment.isExternalStorageManager()}")
            }
            println("B:")
            e.printStackTrace()
            return Operation(false, null)

        } catch (e: IOException) {
            println("C")
            e.printStackTrace()
            return Operation(false, null)

        }

        val ringToneFile = File(path, ringtoneName)

        return Operation(true, ringToneFile)
    }

}

data class Operation(val isSuccess:Boolean, val file:File?)
