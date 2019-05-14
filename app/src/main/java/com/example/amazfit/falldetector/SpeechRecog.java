package com.example.amazfit.falldetector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A very simple application to handle Voice Recognition intents
 * and display the results
 */

public class SpeechRecog extends Activity implements TextToSpeech.OnInitListener
{

    private static final int REQUEST_CODE = 1234;
    private ListView wordsList;
    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;
     TextToSpeech tts;
    String utterid;
    HashMap<String, String> hash = new HashMap<String,String>();
    final String TAG =SpeechRecog.class.getSimpleName();
    private boolean ready = false;

    TextView out;
    private boolean allowed = false;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_recog);
        tts = new TextToSpeech(SpeechRecog.this,SpeechRecog.this);
        Button speakButton = (Button) findViewById(R.id.speakButton);
        out = (TextView)findViewById(R.id.tvout);
        wordsList = (ListView) findViewById(R.id.list);

        // Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }
        checkTTS();

    }
    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }


    /**
     * Handle the action of the button being clicked
     */
    public  void speakButtonClicked(View v)
    {

        hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                "start");
        pause(1000);
        tts.speak("Did Something happen?", TextToSpeech.QUEUE_FLUSH, hash);
    }


    /**
     * Fire an intent to start the voice recognition activity.

     */
    private void startVoiceRecognitionActivity(String utter)
    {
        utterid = utter;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech Recognition");
        startActivityForResult(intent, REQUEST_CODE);

    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            out.setText(matches.get(0).toString());
            wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
            allow(true);
            if(utterid.equals("start")){
                if(out.getText().toString().equals("no")) {
                    hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            "startno");
                    pause(1000);
                    tts.speak("Are you sure you are all right?", TextToSpeech.QUEUE_FLUSH, hash);
                }
                else{
                    hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            "startyes");
                    pause(1000);
                    tts.speak("I think i should call someone", TextToSpeech.QUEUE_FLUSH, hash);
                }
            }
            else if(utterid.equals("startno"))
            {
                if(out.getText().toString().equals("yes"))
                {
                    hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            "end");
                    pause(1000);
                    tts.speak("Ok. Good Day then.", TextToSpeech.QUEUE_FLUSH, hash);
                }
                else
                {
                    hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            "startyes");
                    pause(1000);
                    tts.speak("I think i should call someone", TextToSpeech.QUEUE_FLUSH, hash);
                }
            }
            else if(utterid.equals("startyes"))
            {
                if(out.getText().toString().equals("yes"))
                {
                    hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            "callself");
                    pause(1000);
                    tts.speak("Who shall i ring?", TextToSpeech.QUEUE_FLUSH, hash);
                }
                else{
                    hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            "callauto");
                    pause(1000);
                    tts.speak("I will ring someone just to be sure", TextToSpeech.QUEUE_FLUSH, hash);
                }
            }



        }
        else if(requestCode == CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){

            }else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
        finish();
    }

    public boolean isAllowed(){
        return allowed;
    }

    public void allow(boolean allowed){
        this.allowed = allowed;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public void onInit(int status) {

        if(status == TextToSpeech.SUCCESS){
            // Change this to match your
            // locale
            if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                tts.setLanguage(Locale.US);
            ready = true;
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "onStart ( utteranceId :" + utteranceId + " ) ");
                }

                @Override
                public void onError(String utteranceId) {
                    Log.d(TAG, "onError ( utteranceId :"+utteranceId+" ) ");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "onDone ( utteranceId :" + utteranceId + " ) ");

                        if((!utteranceId.equals("end"))&&(!utteranceId.equals("callauto"))&&(!utteranceId.equals("callself")))
                        startVoiceRecognitionActivity(utteranceId);




                }
            });
        }else if (status == TextToSpeech.ERROR) {
            Toast.makeText(SpeechRecog.this, "Sorry! Text To Speech failed...",
                    Toast.LENGTH_LONG).show();
            ready = false;
        }

    }

    public void pause(int duration){
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }
    public void destroy(){
        tts.shutdown();
    }

}
