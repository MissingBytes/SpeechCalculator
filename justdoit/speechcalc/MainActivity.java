package justdoit.speechcalc;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    Button f8b;
    TextView f9t;
    TextToSpeech t1;

    class C01781 implements OnInitListener {
        C01781() {
        }

        public void onInit(int status) {
            if (status != -1) {
                MainActivity.this.t1.setLanguage(Locale.UK);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0180R.layout.activity_main);
        this.f9t = (TextView) findViewById(C0180R.id.textView);
        this.t1 = new TextToSpeech(getApplicationContext(), new C01781());
        Intent i = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        i.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
        i.putExtra("android.speech.extra.LANGUAGE", Locale.getDefault());
        i.putExtra("android.speech.extra.PROMPT", "Say something");
        startActivityForResult(i, 100);
    }

    public void onActivityResult(int request_code, int result_code, Intent i) {
        super.onActivityResult(request_code, result_code, i);
        switch (request_code) {
            case 100:
                if (result_code == -1 && i != null) {
                    ArrayList<String> s = i.getStringArrayListExtra("android.speech.extra.RESULTS");
                    this.f9t.setText((CharSequence) s.get(0));
                    String sentence = (String) s.get(0);
                    int Num1 = 0;
                    int Num2 = 0;
                    int result = 0;
                    Scanner in = new Scanner(sentence).useDelimiter("[^0-9]+");
                    if (in.hasNext()) {
                        Num1 = in.nextInt();
                    }
                    if (in.hasNext()) {
                        Num2 = in.nextInt();
                    }
                    if (sentence.contains("add") || sentence.contains("+")) {
                        result = Num1 + Num2;
                    }
                    if (sentence.contains("multipl") || sentence.contains("into") || sentence.contains("x")) {
                        result = Num1 * Num2;
                    }
                    if (sentence.contains("subtract") || sentence.contains("minus") || sentence.contains("-")) {
                        result = Num1 - Num2;
                    }
                    if (sentence.contains("divide") || sentence.contains("by") || sentence.contains("/")) {
                        result = Num1 / Num2;
                    }
                    this.t1.speak(Integer.toString(result), 0, null);
                    return;
                }
                return;
            default:
                return;
        }
    }
}
