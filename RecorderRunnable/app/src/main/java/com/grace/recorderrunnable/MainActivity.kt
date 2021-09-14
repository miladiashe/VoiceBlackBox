package com.grace.recorderrunnable

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import com.grace.recorderrunnable.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.util.zip.Inflater




class MainActivity : BaseActivity() {

    val PERM_STORAGE = 99
    val PERM_RECORDER = 100
    val PERM_INTERNET = 101

    var badWordSet = mutableSetOf<String>()
    var recordedWords: String? = null
    var resultCheckwords = false

    var recorderThread = Recorder()

    var speechServiceState: Boolean = false
    var serviceState: Boolean = false
    var SRhandler : Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            Log.d("test", "Main: 중간 점검 음성인식.")
            speechRecognition()
        }
    }

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requirePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_STORAGE)

        makeBadwordsList()

        binding.btnStart.setOnClickListener {
            binding.btnStart.visibility = View.INVISIBLE
            binding.btnStop.visibility = View.VISIBLE

            speechRecognition()

        }

        binding.btnStop.setOnClickListener {
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStop.visibility = View.INVISIBLE
            serviceState = false

            toast("저장 후 녹음을 종료합니다.")
            recorderThread.serviceState = false
            recorderThread.recorderHandler.sendEmptyMessage(0)
            Log.d("test", "Main: Sub에게 종료신호 보냈다.")

        }
    }

    override fun permissionGranted(requestCode: Int) {
        when (requestCode) {
            PERM_STORAGE -> requirePermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ), PERM_RECORDER
            )
            PERM_RECORDER -> toast("START를 눌러 녹음을 시작하세요.")
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when (requestCode) {
            PERM_INTERNET, PERM_STORAGE, PERM_RECORDER -> {
                toast("외부 저장소 및 마이크 권한을 승인해야 앱을 사용할 수 있습니다. 앱을 종료합니다.")
                finish()
            }
        }
    }

    fun toast(message: String, duration: Int = 0) {
        var durationTime = Toast.LENGTH_SHORT
        if (duration != 0) durationTime = Toast.LENGTH_LONG
        Toast.makeText(MainApplication.applicationContext(), message, durationTime).show()
    }

    fun speechRecognition() {
        Log.d("test","Main: 음성인식 시작")
        if (!speechServiceState) {
            speechServiceState = true

            var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

            var mRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            mRecognizer.setRecognitionListener(listener())

            try {
                mRecognizer.startListening(intent)
            } catch (e: Exception) {
                toast("예외처리 오류가 발생했습니다.")
                Log.d("test", "예외처리 오류가 발생했습니다.")
            }
        }
    }

    fun listener() = object: RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            toast("음성인식을 시작합니다.")
        }

        override fun onBeginningOfSpeech() {

        }

        override fun onRmsChanged(rmsdB: Float) {

        }

        override fun onBufferReceived(buffer: ByteArray?) {

        }

        override fun onEndOfSpeech() {

        }

        override fun onError(error: Int) {
            toast("오류가 발생하였습니다.")
            Log.d("test","speechRecognition() 오류발생")

            speechServiceState = false

            if (serviceState){
                Thread.sleep(500)
                toast("다시 실행합니다.")
                speechRecognition()
            }
        }

        override fun onResults(results: Bundle?) {
            Log.d("test","Main: 음성인식 종료")

            recordedWords = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0]
            binding.textView.text = recordedWords
            checkBadwords()

            speechServiceState = false

            toast("녹음을 시작합니다.")
            Log.d("test", "Main: Sub 시작")
            if (recorderThread.state == Thread.State.NEW) {
                recorderThread.serviceState = true
                recorderThread.start()
                Log.d("test", "Main: Sub 상태 ${recorderThread.state}")
            }

        }

        override fun onPartialResults(partialResults: Bundle?) {

        }

        override fun onEvent(eventType: Int, params: Bundle?) {

        }
    }

    fun makeBadwordsList() {
        badWordSet.add("시발")
        badWordSet.add("존나")
        badWordSet.add("꺼져")
        badWordSet.add("병신")
        badWordSet.add("미친놈")
        badWordSet.add("새끼")
    }

    fun checkBadwords() {
        Log.d("test","Main: 단어 판별 시작")
        Log.d("test","Main: 들어온 말 [${recordedWords}]")
        for (badWord in badWordSet) {
            resultCheckwords = recordedWords!!.contains(badWord)

            if (resultCheckwords) {
                recorderThread.resultCheckWords = true
                break
            } else {
                recorderThread.resultCheckWords = false
            }
        }
        Log.d("test","Main: 단어 판별 종료")
        Log.d("test","Main: 단어 판별 결과 :${recorderThread.resultCheckWords}")
    }

//    internal class LooperThead : Thread() {
//        override fun run() {
//            Looper.prepare()
//
//            //현재 루퍼의 메세지큐를 얻는다 ==;
//            val que = Looper.myQueue()
//            que.addIdleHandler(object : MessageQueue.IdleHandler {
//                @TargetApi(Build.VERSION_CODES.M)
//                override fun queueIdle(): Boolean {
//                    while (que.isIdle) {
//                        speechRecognition()
//                    }
//                    return true //false반환하면 리스너해제
//                }
//            })
//            Looper.loop()
//        }
//    }
}