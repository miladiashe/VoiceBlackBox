package com.grace.onlyrecorder

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

class MyService : Service() {

    var USE_FOLDER_NAME = Environment.getExternalStorageDirectory()
                            .absolutePath + "/Voice Blackbox/"

    var mediaRecorder: MediaRecorder? = null
    var state: Boolean = false
    var fileFullName: String? = null
//    var fileName: String? = null

    var fileList= mutableListOf<String>()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        //Log.d("StartedService", "state = $state")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopRecording()
        //checkTone()
        //Log.d("Service", "서비스가 종료되었습니다.")
        super.onDestroy()
    }

    fun startRecording() {
        fileFullName = newDir()+newFileName()
        fileList.add("${fileFullName}")
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource((MediaRecorder.AudioSource.MIC))
        mediaRecorder?.setOutputFormat((MediaRecorder.OutputFormat.MPEG_4))
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(fileFullName)

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "녹음을 시작합니다.", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException){
            e.printStackTrace()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun newFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())

        return "$filename.mp3"
    }

    fun newDir(): String {
        var sdPath = USE_FOLDER_NAME
        var file = File(sdPath)
        file.mkdirs()

        return "$sdPath"
    }

    fun stopRecording(){
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "녹음을 중지합니다. \n" +
                    "저장경로: ${fileFullName}", Toast.LENGTH_LONG).show()
            checkTone()
        } else {
            Toast.makeText(this, "녹음중이 아닙니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkTone() {
        var fileToCheck = fileList.get(0)
        var result = false // 기본값은 저장을 위한 true 즉, "언어폭력"이다.

        // 딥러닝 코드 들어갈 자리

        if (!result) {
            val fileToDel = File(fileToCheck)
            fileToDel.delete()
            fileList.removeAt(0)
        }
    }
}