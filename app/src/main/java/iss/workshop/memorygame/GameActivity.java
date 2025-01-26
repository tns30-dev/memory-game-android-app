package iss.workshop.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class GameActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Chronometer Timer;
    private long pauseOffset = 0;

    private int matchrecord = 0;

    private int buttonpressed = 0;
    private int imgpos = 0;
    private GridAdapter ga;

    MediaPlayer mediaPlayer;
    ArrayList<String> gameimages = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        ArrayList<String> selectedimgs = intent.getStringArrayListExtra("selectedimgs");
        duplicateAndShuffle(selectedimgs);
        TextView txtgameprog = findViewById(R.id.txtgameprog);
        String scoreTextSet = "Pairs: 0/6";
        txtgameprog.setText(scoreTextSet);
        Timer = findViewById(R.id.Timer);
        StartTimer();
        GridView gamegrid = findViewById(R.id.gamegrid);
        ga = new GridAdapter(this,gameimages);
        if (gamegrid != null){
            gamegrid.setAdapter(ga);
            gamegrid.setOnItemClickListener(this);
        }

    }
    private void duplicateAndShuffle(ArrayList<String> selectedimgs){
        gameimages.addAll(selectedimgs);
        gameimages.addAll(selectedimgs);
        Collections.shuffle(gameimages);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        buttonpressed ++;
        ga.updategamecon(position,true);
        ga.notifyDataSetChanged();
        if (buttonpressed == 2){
            if (gameimages.get(imgpos).equals(gameimages.get(position))){
                matchrecord ++;
                updatePairsScore(matchrecord);
                buttonpressed = 0;
                if (matchrecord == 6){
                    mediaPlayer = MediaPlayer.create(GameActivity.this,R.raw.win);
                    mediaPlayer.start();
                    Intent response = new Intent();
                    StopTimer();
                    response.putExtra("timer",pauseOffset);
                    setResult(RESULT_OK,response);
                    finish();
                }else {
                    mediaPlayer = MediaPlayer.create(GameActivity.this,R.raw.match2);
                    mediaPlayer.start();
                }
            } else if (!Objects.equals(gameimages.get(imgpos), gameimages.get(position))) {
                buttonpressed = 0;
                mediaPlayer = MediaPlayer.create(GameActivity.this,R.raw.mismatch);
                mediaPlayer.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ga.updategamecon(imgpos,false);
                        ga.updategamecon(position,false);
                        ga.notifyDataSetChanged();
                    }
                },1000);
            }
        }
        else {
            imgpos = position;
        }
        //Toast toast = Toast.makeText(getBaseContext(),"Oh yeahhh",Toast.LENGTH_LONG);
        //toast.show();
    }


    public void StartTimer(){
        Timer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        Timer.start();
    }

    public void StopTimer(){
        Timer.stop();
        String timerText = Timer.getText().toString();
        pauseOffset = convertToMilliseconds(timerText);
    }
    private long convertToMilliseconds(String timerText) {
        String[] timeComponents = timerText.split(":");
        long minutes = Long.parseLong(timeComponents[0]);
        long seconds = Long.parseLong(timeComponents[1]);
        return (minutes * 60 + seconds);
    }

    public void updatePairsScore(int pairScore){
        TextView txtgameprog = findViewById(R.id.txtgameprog);
        String scoreTextSet = "Pairs: " + pairScore + "/6";
        txtgameprog.setText(scoreTextSet);
    }
}