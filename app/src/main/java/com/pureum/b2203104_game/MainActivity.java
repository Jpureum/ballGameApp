package com.pureum.b2203104_game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.Random;


public class MainActivity extends AppCompatActivity {

    static int cx, cy, r, dx = 0, dy = 0, layoutWidth, layoutHeight;
    static int rx, ry, w;
    static int level = 1, boxSize = 10, sec = 20;
    static int lives = 3;
    static MyGraphicView myGraphicView;
    static LinearLayout layout;
    SensorManager sensorManager;
    Sensor accelerometerSensor;
    static TextView textView, timerTv, livesTv, modeTv;

    Button startBtn, stopBtn, hmodeBtn, emodeBtn;
    static boolean isHardMode = false;

    private static CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        myGraphicView = new MyGraphicView(this);
        layout = (LinearLayout) findViewById(R.id.ViewLayout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        textView = (TextView) findViewById(R.id.textView);

        startBtn = (Button) findViewById(R.id.start_btn);
        stopBtn = (Button) findViewById(R.id.stop_btn);
        hmodeBtn = (Button) findViewById(R.id.hard_btn);
        emodeBtn = (Button) findViewById(R.id.easy_btn);

        timerTv = (TextView) findViewById(R.id.timer_tv); //타이머 표시
        livesTv = (TextView) findViewById(R.id.lives_tv); //목숨 표시
        modeTv = (TextView) findViewById(R.id.mode_tv); //모드 표시

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myGraphicView.getParent() != null){
                    ((ViewGroup) myGraphicView.getParent()).removeView(myGraphicView);
                }
                layout.addView(myGraphicView);
                startGame(); // 게임 시작
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endGame();
            }
        });

        hmodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHardMode = true;
                updateModeText();

            }
        });

        emodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHardMode = false;
                updateModeText();
            }
        });

        //타이머 생성
        countDownTimer = new CountDownTimer(sec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTv.setText(millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                timerTv.setText("0s");
                lives--; // 시간이 끝났을 때 목숨 -1

                updateLivesText(); // 남은 목숨 표시

                if (lives == 0) {
                    endGame(); // 목숨 0 -> 게임 종료
                } else {
                    countDownTimer.start();
                    restartLevel();
                }
            }
        };
    }

    //게임 시작
    private void startGame(){
        level = 1;
        sec = 30;
        lives = 3;
        boxSize = 10;
        isHardMode = false; //기본 -> 이지모드

        updateModeText();
        updateLivesText();

        countDownTimer.start();
        restartLevel();
    }

    //게임 종료
    private static void endGame(){
        countDownTimer.cancel();
        layout.removeView(myGraphicView);
    }

    //다음 레벨 진행
    private static void restartLevel(){

        Random random = new Random();

        w = r * boxSize;
        rx = random.nextInt(layoutWidth - w);
        ry = random.nextInt(layoutHeight - w);
        cx = layoutWidth / 2;
        cy = layoutHeight / 2;

        updateLevelText();

        myGraphicView.invalidate();
    }

    private void updateLivesText(){
        if(lives == 3){
            livesTv.setText("♥ ♥ ♥");
        }
        if(lives == 2){
            livesTv.setText("♥ ♥  ");
        }
        if(lives == 1){
            livesTv.setText("♥    ");
        }
        if(lives == 0) {
            livesTv.setText("Game End!");
        }
    }

    private static void updateLevelText(){
        textView.setText("Level "+level);
    }

    private static void updateModeText(){
        if(!isHardMode) {
            modeTv.setText("E");
        } else {
            modeTv.setText("H");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, sensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        layoutWidth = layout.getWidth();
        layoutHeight = layout.getHeight();

        cx = layoutWidth / 2;
        cy = layoutHeight / 2;
        r = (int)(layoutHeight * 0.02);

        restartLevel();
    }

    public static class MyGraphicView extends View {

        private MyGraphicView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setColor(Color.YELLOW);
            Paint paint1 = new Paint();
            paint1.setColor(Color.BLUE);

            canvas.drawRect(rx, ry, rx+w, ry+w, paint1);
            canvas.drawCircle(cx, cy, r, paint);

            if (cx - r > rx && cx + r < rx + w && cy - r > ry && cy + r <= ry + w) {
                // 원이 사각형 안으로 들어감
                level++; //level up
                boxSize -= 0.5; // 박스 사이즈 감소

                countDownTimer.cancel();
                countDownTimer.start();


                restartLevel();

            }

            if(r*2 >= w) {
                textView.setText("CLEAR!");
                endGame();
            }

        }
    }

    final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

                if(!isHardMode) {
                    dx = -(int)(event.values[0] * level); // 이동값
                    dy = (int)(event.values[1] * level);
                } else {
                    dx = -(int)(event.values[0] * (level*5)); // 이동값
                    dy = (int)(event.values[1] * (level*5));
                }

                if(cx - r + dx > 0 && cx + r + dx < layoutWidth) {
                    cx = cx + dx;
                }
                if(cy - r + dy > 0 && cy + r + dy < layoutHeight) {
                    cy = cy + dy;
                }
                myGraphicView.invalidate();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}