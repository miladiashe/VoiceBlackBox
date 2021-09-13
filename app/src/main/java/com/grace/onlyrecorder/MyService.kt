package com.grace.onlyrecorder

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
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
    private var myMLcode = MLcode(this)
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
            checkTone("${fileFullName}")
        } else {
            Toast.makeText(this, "녹음중이 아닙니다.", Toast.LENGTH_SHORT).show()
        }
    }



    /*여기부터 내가 쓰는 것*/


    private var mlcode = MLcode(this)
    //임시저장한 파일을 check 하는거로 해줘
    fun checkTone(fileName: String) {
        var fileToCheck = fileList.get(0)
        var result = false // 어조가 언어 폭력이 아니라면
        myMLcode.initialize()

        // 여기에 판별 코드 넣어줘~

        //0. 내가만든 처리기를 연다
        //1. 파일을 연다
        //2. 음성파일을 전처리한다
        //3. 넣어서 물어본다
        //4. 50% 이상이면 true를 반환한다


        result = mlcode.classify(fileName)

       /* if ((fileName != null) && (myMLcode.isInitialized)) {
            myMLcode
                .classifyAsync(fileName)
        }*/


    }


}