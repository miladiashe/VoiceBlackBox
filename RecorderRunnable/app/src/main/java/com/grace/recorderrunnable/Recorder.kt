package com.grace.recorderrunnable

import android.media.MediaRecorder
import android.os.*
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import android.os.Looper

import com.grace.onlyrecorder.MLcode
import com.grace.recorderrunnable.ml.FinalModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer


class Recorder: Thread() {

    var serviceState: Boolean = false           // 사용자로부터 녹음 의사 여부

    var USE_FOLDER_NAME = Environment.getExternalStorageDirectory()
        .absolutePath + "/Voice Blackbox/"      // 파일 경로
    var fileFullName: String? = null            // 파일 이름
    var fileList= mutableListOf<String>()       // 만들어진 파일 목록
    var fileLength = 0                          // 0이면 10초 1이면 3분
    var countSec = 0

    var mediaRecorder: MediaRecorder? = null    // 녹음기
    var recordingState: Boolean = false         // 녹음중 여부

    var resultCheckTone = false                 // 어조 언어폭력 여부
    var resultCheckWords = false                // 단어 언어폭력 여부 (현재 사용X)

    var recorderHandler = RecorderHandler()     // 사용자로부터 녹음 중지 메세지 전달자
    val speechHandler = Handler(Looper.getMainLooper())

    // 사용자가 STOP 버튼을 누르면 지금까지 녹음한 것 저장하고 스레드 강제 종료.
    inner class RecorderHandler: Handler() {
        override fun handleMessage(msg: Message) {
            Log.d("test","Sub: Main으로부터 종료신호를 받았다")
            if (recordingState){
                stopRecording()
                sleep(3000)
                interrupt()
                Log.d("test","Sub: 스레드 3초후에 종료.")
            }
        }
    }

    override fun run() {
        Log.d("test","Sub: 스레드 시작했다.")
        Log.d("test", "Sub: 처음 시작 10초 녹음")
        startRecording()
        sleep(11000)
        fileLength = 0
        countSec++
        stopRecording()
        while (serviceState){
            startRecording()
            if (resultCheckTone || resultCheckWords) {
                Log.d("test", "Sub: 언어 폭력이라 길게 녹음 시작했다.")
                sleep(31000) //Thread.sleep(300000) 원래 3분 시연을 위해 30초
                fileLength = 1
                countSec++
                Log.d("test", "Sub: 긴 녹음 종료했다.")
            } else {
                Log.d("test", "Sub: 언어 폭력이 아니라 짧게 녹음 시작했다.")
                sleep(11000)
                fileLength = 0
                countSec++
                Log.d("test", "Sub: 짧은 녹음 종료했다.")
            }
            stopRecording()
        }
        Log.d("test", "Sub: 스레드 종료했다.${serviceState}")
    }

    fun startRecording() {
        if (!recordingState) {
            recordingState = true
            Log.d("test", "Sub: 녹음 시작.")

            recordingState = true
            fileFullName = newDir() + newFileName()
            fileList.add("${fileFullName}")
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource((MediaRecorder.AudioSource.MIC))
            mediaRecorder?.setOutputFormat((MediaRecorder.OutputFormat.MPEG_4))
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(fileFullName)

            try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun newFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())

        return "$filename.wav"
    }

    fun newDir(): String {
        var sdPath = USE_FOLDER_NAME
        var file = File(sdPath)
        file.mkdirs()

        return "$sdPath"
    }

    fun stopRecording(){
        Log.d("test","Sub: 녹음 끝내고 저장 시작")
        if(recordingState){
            recordingState = false
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            Log.d("test","Sub: 저장 완료")
        } else {
            Log.d("test","Sub: 녹음중이 아닙니다.")
        }
        recordingState = false
        when(fileLength){
            0 -> {
                checkTone()
                if (countSec == 2) {
                    countSec = 0
                    speechHandler.sendEmptyMessage(0)
                    Log.d("test", "Sub: Main 음성 인식 하라고 신호 보내기")
                    sleep(5000)
                }
            }
            1 -> {
                countSec = 0
                speechHandler.sendEmptyMessage(0)
                Log.d("test", "Sub: Main 음성 인식 하라고 신호 보내기")
                Thread.sleep(5000)
            }
        }
    }


    private var mlcode = MLcode(this)
    fun checkTone() {
        Log.d("test","Sub: 어조 판별 시작")
        if (fileLength == 0) {
            var fileToCheck = fileList.get(0)   // 어조 판정에 사용할 파일 이름

            // 딥러닝 코드 들어갈 자리

            resultCheckTone = false  // 어조가 언어 폭력이라고 가정





            val model = FinalModel.newInstance(MainApplication.applicationContext())
            Log.d("test","Sub: 모델 오픈")
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 220500), DataType.FLOAT32)
            var byteBuffer = inputFeature0.getBuffer()
            val byteArray =  mlcode.convertAudioToByteBuffer(fileToCheck)


            byteBuffer = ByteBuffer.wrap(byteArray)

            Log.d("test","Sub: 프로세싱 중.... ${byteBuffer}")
            val longByteBuffer = mlcode.extendBuffer(byteBuffer, 882000-byteBuffer.limit())

            if (longByteBuffer != null) {
                inputFeature0.loadBuffer(longByteBuffer)
            }
            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            Log.d("test","Sub: 프로세싱 중....")
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val predValue = outputFeature0.getFloatValue(0)

            // Releases model resources if no longer used.
            model.close()


            resultCheckTone = predValue >= 0.5
            Log.d("test", "Sub: 어조 판별 종료 결과 :${resultCheckTone}, $predValue")
            //시연할땐 predvalue 안보여주기
            leaveOrDeleteFile(fileToCheck)
        }
    }

    fun leaveOrDeleteFile(fileName: String) {
        Log.d("test","Sub: 파일 판정 시작")
        if (!(resultCheckTone || resultCheckWords)) {
            val fileToDel = File(fileName)
            fileToDel.delete()
            fileList.removeAt(0)
            Log.d("test","Sub: 파일 삭제")
        } else {
            Log.d("test","Sub: 파일 유지")
        }
    }

}

