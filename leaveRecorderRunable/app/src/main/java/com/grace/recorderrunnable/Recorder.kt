package com.grace.recorderrunnable

import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

class Recorder : Thread() {

    var USE_FOLDER_NAME = Environment.getExternalStorageDirectory()
        .absolutePath + "/Voice Blackbox/"

    var mediaRecorder: MediaRecorder? = null
    var serviceState: Boolean = false
    var fileFullName: String? = null
    var fileList= mutableListOf<String>()
    var recordingState: Boolean = false

    var resultCheckTone = false
    var resultCheckWords = false

    var recorderHandler = RecorderHandler()

    inner class RecorderHandler: Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                // 녹음 버튼 눌렸는지 안눌렸는지.
                if (msg.arg1 == 1) {
                    serviceState = true
                } else {
                    serviceState = false
                }

                // 언어 폭력 단어 있는지 없는지
                if (msg.arg2 == 1) {
                    resultCheckWords = true
                } else {
                    resultCheckWords = false
                }
            }
    }


    override fun run() {
        Looper.prepare()
        Looper.loop()
        do {
            Log.d("milkTea","시작했다.")
            Log.d("milkTea","${serviceState}")
            startRecording()
            if (resultCheckTone || resultCheckWords) {
                Log.d("milkTea","길게 시작했다.")
                Thread.sleep(300000)
                Log.d("milkTea","길게 종료했다.")
                stopRecording()
            } else {
                Log.d("milkTea","짧게 시작했다.")
                Thread.sleep(10000)
                Log.d("milkTea","짧게 종료했다.")
                stopRecording()
            }
            Log.d("milkTea","종료했다.")
        } while (serviceState)
        Log.d("milkTea","진짜 종료했다.${serviceState}")
        Looper.myLooper()?.quitSafely()
    }

    fun startRecording() {
        Log.d("milkTea","startRecording() 시작.")
        recordingState = true
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
        } catch (e: IllegalStateException){
            e.printStackTrace()
        } catch (e: IOException){
            e.printStackTrace()
        }
        Log.d("milkTea","startRecording() 끝.")
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
        Log.d("milkTea","stopRecording() 시작")
        if(recordingState){
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            recordingState = false
            checkTone()
        } else {
            Log.d("milkTea","녹음중이 아닙니다.")
        }
        Log.d("milkTea","stopRecording() 끝")
    }

    fun checkTone() {
        Log.d("milkTea","checkTone() 시작")
        var fileToCheck = fileList.get(0)
        //var result = false // 기본값은 저장을 위한 true 즉, "언어폭력"이다.

        // 딥러닝 코드 들어갈 자리
        if (!resultCheckTone ) {
            val fileToDel = File(fileToCheck)
            fileToDel.delete()
            fileList.removeAt(0)
            Log.d("milkTea","어조가 욕 아니라 파일 삭제")
        } else {
            Log.d("milkTea","어조가 욕이라 파일 유지")
        }
        Log.d("milkTea","checkTone() 종료")
        Log.d("milkTea","checkTone() 결과 ${resultCheckTone}")
    }
}

