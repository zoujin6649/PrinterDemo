package com.example.printer;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by claire on 2020/9/1.
 */
public class SystemTTS extends UtteranceProgressListener implements TextToSpeech.OnUtteranceCompletedListener {
    private Context mContext;
    private static SystemTTS singleton;
    private TextToSpeech textToSpeech;//系统语音播报类
    private boolean isSuccess = true;

    public static SystemTTS getInstance(Context context) {
        if (singleton == null) {
            synchronized (SystemTTS.class) {
                if (singleton == null) {
                    singleton = new SystemTTS(context);
                }
            }
        }
        return singleton;
    }

    private SystemTTS(Context context) {
        this.mContext = context;
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    textToSpeech.setPitch(1.0f);//设置音调，值越大越尖，值越小则变成男生，1.0是常规
                    textToSpeech.setSpeechRate(1.0f);//设置语速
                    textToSpeech.setOnUtteranceProgressListener(SystemTTS.this);
                    textToSpeech.setOnUtteranceCompletedListener(SystemTTS.this);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        //系统不支持中文播报
                        isSuccess = false;
                    }
                }
            }
        });
    }


    @Override
    public void onUtteranceCompleted(String utteranceId) {
        //播报完成回调
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {

    }

    @Override
    public void onError(String utteranceId) {

    }

    public void playText(String playText) {
        if (!isSuccess) {
            Toast.makeText(mContext, "系统不支持中文播报", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textToSpeech != null) {
            textToSpeech.speak(playText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void stopSpeak() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }
}
