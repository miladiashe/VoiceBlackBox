package com.grace.onlyrecorder

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

class MyService : Service() {

    var USE_FOLDER_NAME = Environment.getExternalStorageDirectory()
                            .absolutePath + "/Voice Blackbox/"

    var mediaRecorder: MediaRecorder? = null
    var state: Boolean = false
    var fileFullName: String? = null
    var fileList= mutableListOf<String>()

    var resultCheckTone = true


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
                startRecording()


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopRecording()
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
            toast("녹음을 시작합니다.")
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
            toast("녹음을 중지합니다.")
            checkTone()
        } else {
            toast("녹음중이 아닙니다.")
        }
    }

    fun checkTone() {
        var fileToCheck = fileList.get(0)
        //var result = false // 기본값은 저장을 위한 true 즉, "언어폭력"이다.

        // 딥러닝 코드 들어갈 자리

        if (!resultCheckTone ) {
            val fileToDel = File(fileToCheck)
            fileToDel.delete()
            fileList.removeAt(0)
        }
    }

    fun toast(message: String, duration: Int = 0) {
        var durationTime = Toast.LENGTH_SHORT
        if (duration != 0) durationTime = Toast.LENGTH_LONG
        Toast.makeText(MainApplication.applicationContext(), message, durationTime).show()
    }
}