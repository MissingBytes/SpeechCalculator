package justdoit.speechcalc;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import java.lang.ref.WeakReference;

public class MyService extends Service {
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    protected AudioManager mAudioManager;
    protected volatile boolean mIsCountDownOn;
    protected boolean mIsListening;
    private boolean mIsStreamSolo;
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            MyService.this.mIsCountDownOn = false;
            try {
                MyService.this.mServerMessenger.send(Message.obtain(null, 2));
                MyService.this.mServerMessenger.send(Message.obtain(null, 1));
            } catch (RemoteException e) {
            }
        }
    };
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;

    protected class IncomingHandler extends Handler {
        private WeakReference<MyService> mtarget;

        IncomingHandler(MyService target) {
            this.mtarget = new WeakReference(target);
        }

        public void handleMessage(Message msg) {
            MyService target = (MyService) this.mtarget.get();
            switch (msg.what) {
                case 1:
                    if (VERSION.SDK_INT >= 16 && !MyService.this.mIsStreamSolo) {
                        MyService.this.mAudioManager.setStreamSolo(0, true);
                        MyService.this.mIsStreamSolo = true;
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        return;
                    }
                    return;
                case 2:
                    if (MyService.this.mIsStreamSolo) {
                        MyService.this.mAudioManager.setStreamSolo(0, false);
                        MyService.this.mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    return;
                default:
                    return;
            }
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener {
        private static final String TAG = "SpeechRecognitionListener";

        protected SpeechRecognitionListener() {
        }

        public void onBeginningOfSpeech() {
            if (MyService.this.mIsCountDownOn) {
                MyService.this.mIsCountDownOn = false;
                MyService.this.mNoSpeechCountDown.cancel();
            }
        }

        public void onBufferReceived(byte[] buffer) {
        }

        public void onEndOfSpeech() {
            System.out.print("===========================DONE=======================");
        }

        public void onError(int error) {
            if (MyService.this.mIsCountDownOn) {
                MyService.this.mIsCountDownOn = false;
                MyService.this.mNoSpeechCountDown.cancel();
            }
            MyService.this.mIsListening = false;
            try {
                MyService.this.mServerMessenger.send(Message.obtain(null, 1));
            } catch (RemoteException e) {
            }
        }

        public void onEvent(int eventType, Bundle params) {
        }

        public void onPartialResults(Bundle partialResults) {
            System.out.print("===========================DOING=======================");
        }

        public void onReadyForSpeech(Bundle params) {
            if (VERSION.SDK_INT >= 16) {
                MyService.this.mIsCountDownOn = true;
                MyService.this.mNoSpeechCountDown.start();
            }
        }

        public void onResults(Bundle results) {
        }

        public void onRmsChanged(float rmsdB) {
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        this.mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        this.mSpeechRecognizerIntent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        this.mSpeechRecognizerIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
        this.mSpeechRecognizerIntent.putExtra("calling_package", getPackageName());
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mIsCountDownOn) {
            this.mNoSpeechCountDown.cancel();
        }
        if (this.mSpeechRecognizer != null) {
            this.mSpeechRecognizer.destroy();
        }
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}
