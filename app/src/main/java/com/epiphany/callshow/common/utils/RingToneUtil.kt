package com.epiphany.callshow.common.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.epiphany.callshow.App
import java.io.File


object RingToneUtil {

    /**
     * 设置铃声
     * @param type RingtoneManager.TYPE_RINGTONE 来电铃声
     * @param path 下载下来的mp3全路径
     * @param title 铃声的名字
     */
    fun setRing(context: Context, type: Int, path: String, title: String?): Boolean {

        val ringFile = File(path)
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, ringFile.absolutePath)
        values.put(MediaStore.MediaColumns.TITLE, title)
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, title)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*")
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        val uri = MediaStore.Audio.Media.getContentUriForPath(ringFile.absolutePath)
        var newUri: Uri? = null
        var id: String? = null
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri!!,
                null,
                "${MediaStore.MediaColumns.DATA}=?",
                arrayOf(path),
                null,
                null
            )
            cursor?.let {
                newUri = if (it.count > 0) {
                    it.moveToFirst()
                    id = it.getString(it.getColumnIndex(MediaStore.MediaColumns._ID))
                    Uri.withAppendedPath(uri, id)
                } else {
                    context.contentResolver.delete(
                        uri,
                        MediaStore.MediaColumns.DATA + "=?",
                        arrayOf(path)
                    )
                    context.contentResolver.insert(uri!!, values)
                }
            }
            Log.d("ringtone", "id:$id newUri:$newUri uri:$uri")
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )
            Log.d("ringtone", "set end id:$id newUri:$newUri uri:$uri")
            return newUri != null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return false
    }

    fun setSim2Huawei(context: Context, uri: Uri?): Boolean {
        return try {
            Settings.System.putString(context.contentResolver, "ringtone2", uri.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun hasSetRingTone(context: Context, ringPath: String): Boolean {
        val ringtoneUri =
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE) ?: return false
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(ringtoneUri, null, null, null, null)
            if (cursor!!.moveToFirst()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                Log.d(
                    "ringtone",
                    "hasSetRingTone ringtoneUri:$ringtoneUri path:$path ringPath:$ringPath"
                )
                if (ringPath.isNotBlank() && ringPath == path) {
                    return true
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            cursor?.close()
        }
        return false
    }

    fun getCurrentRingTonePath(context: Context): String? {
        val ringtoneUri =
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE) ?: return null
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(ringtoneUri, null, null, null, null)
            if (cursor!!.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            cursor?.close()
        }
        return ""
    }


    /**
     * 设置铃声是否静音
     */
    fun adjustRingAudio(setMute: Boolean) {
        val audioManager = App.getApp().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val adJustMute: Int = if (setMute) {
                AudioManager.ADJUST_MUTE
            } else {
                AudioManager.ADJUST_UNMUTE
            }
            try {
                audioManager.adjustStreamVolume(AudioManager.STREAM_RING, adJustMute, 0)
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, adJustMute, 0)
            } catch (e: Throwable) {
            }
        } else {
            try {
                audioManager.setStreamMute(AudioManager.STREAM_RING, setMute)
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, setMute)
            } catch (e: Throwable) {
            }
        }
    }

    fun adjustMusicAudio(setMute: Boolean) {
        val audioManager = App.getApp().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val adJustMute: Int = if (setMute) {
                AudioManager.ADJUST_MUTE
            } else {
                AudioManager.ADJUST_UNMUTE
            }
            try {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, adJustMute, 0)
            } catch (e: Throwable) {
            }
        } else {
            try {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, setMute)
            } catch (e: Throwable) {
            }
        }

        //判断非静音时，判断音量为0时，则设置声音
        if (!setMute) {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val ring = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            if (current <= 0) {
                if (ring > 0) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ring, AudioManager.FLAG_PLAY_SOUND)
                } else {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        AudioManager.FLAG_PLAY_SOUND
                    )
                }
            }
        }
    }


}
