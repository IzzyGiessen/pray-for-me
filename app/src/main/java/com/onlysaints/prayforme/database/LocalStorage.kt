package com.onlysaints.prayforme.database

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID

class LocalStorage(private val context: Context) {
    fun addPrayerId(prayerId: String) {
        try {
            val fos = context.openFileOutput("prayers", Context.MODE_APPEND)
            fos.write("$prayerId\n".toByteArray())
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writePrayer(prayerId: String, prayer: HashMap<String, String>) {
        try {
            val oos = ObjectOutputStream(context.openFileOutput(prayerId, Context.MODE_PRIVATE))
            oos.writeObject(prayer)
            oos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readPrayerIds(): String? {
        return try {
            val fis = context.openFileInput("prayers")
            val br = fis.bufferedReader()
            br.readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readPrayer(filename: String): HashMap<String, String>? {
        return try {
            val ois = ObjectInputStream(context.openFileInput(filename))
            val prayer = ois.readObject() as HashMap<String, String>
            ois.close()
            prayer
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun has(filename: String): Boolean {
        return try {
            context.openFileOutput(filename, Context.MODE_APPEND)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun remove(filename: String) {
        try {
            context.getFileStreamPath(filename).delete()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun downloadImage(image: Bitmap) {
        val resolver = context.contentResolver
        val values = ContentValues()
        // save to a folder
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${UUID.randomUUID()}.jpg")
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/prayforme")
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
        println(uri)
        // You can use this outputStream to write whatever file you want:
        val outputStream = resolver.openOutputStream(uri!!)

        try {
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
            outputStream.close()
            outputStream.flush()
            val toast = Toast(context)
            toast.setText("Download successful")
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}