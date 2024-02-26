package com.onlysaints.prayforme.database

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.onlysaints.prayforme.classes.Prayer
import java.io.EOFException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID

class LocalStorage(private val context: Context) {
    fun addPrayerId(prayerId: String) {
        println("ADDING PRAYER")
        try {
            val fos = context.openFileOutput("prayers", Context.MODE_APPEND)
            fos.write("$prayerId\n".toByteArray())
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writePrayer(prayer: Prayer) {
        try {
             val oos = ObjectOutputStream(
                context.openFileOutput(
                    prayer.id(),
                    Context.MODE_PRIVATE
                )
            )
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
            val text = br.readText()
            br.close()
            fis.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readPrayer(filename: String): Prayer? {
        return try {
            val fis = context.openFileInput(filename)
            val ois = ObjectInputStream(fis)
            val prayer = ois.readObject() as Prayer
            ois.close()
            fis.close()
            prayer
        } catch (e: EOFException) {
            // end of file reached unexpectedly
            e.printStackTrace()
            null
        }
    }

    fun has(filename: String): Boolean {
        return try {
            val fis = context.openFileInput(filename)
            fis.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun remove(filename: String) {
        try {
            context.deleteFile(filename)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun removePrayer(prayerId: String) {
        remove(prayerId)
        removePrayerId(prayerId)
    }

    fun removePrayerId(filename: String) {
        // remove id from id's file
        try {
            val fis = context.openFileInput("prayers")
            val br = fis.bufferedReader()
            val prayers = br.readText()
            val newPrayers = prayers.split("\n").toHashSet()
            newPrayers.remove(filename)
            br.close()
            fis.close()

            remove("prayers")
            try {
                val fos = context.openFileOutput("prayers", Context.MODE_APPEND)
                val newPrayersFile = newPrayers.joinToString("\n") + "\n"
                fos.write(newPrayersFile.toByteArray())
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
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