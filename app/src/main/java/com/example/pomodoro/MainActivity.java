package com.example.pomodoro;

import static com.example.pomodoro.R.string.pause;
import static com.example.pomodoro.R.string.start;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private CountDownTimer pomodoroTimer = null;
    private boolean timerRunning = false;
    private TextView timerTextView;
    private Button startButton;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timer_text_view);
        startButton = findViewById(R.id.start_button);

        startButton.setOnClickListener(view -> {
            if (!timerRunning) {
                startTimer();
                timerRunning = true;
                startButton.setText(pause);
            } else {
                if (pomodoroTimer != null) {
                    pomodoroTimer.cancel();
                }
                timerRunning = false;
                startButton.setText(start);
            }
        });
    }

    private void startTimer() {
        long timerLength = 10 * 1000; // change to any number 25 is the recommended length
        ProgressBar circular_progress_bar = new ProgressBar(MainActivity.this);
        circular_progress_bar.setMax(100); // Set the maximum value for the progress bar
        circular_progress_bar.setProgress(100); // Start with full progress

        pomodoroTimer = new CountDownTimer(timerLength, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = millisUntilFinished / 1000 % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
                int progress = (int) ((millisUntilFinished * 100) / timerLength);
                circular_progress_bar.setProgress(100 - progress);
                Log.d("TimerProgress", "Progress: " + (100 - progress));
            }

            @Override
            public void onFinish() {
                if (pomodoroTimer != null) {
                    pomodoroTimer.cancel();
                }
                timerRunning = false;
                playAlarmSound();
                showFinishDialog();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showNotification();
                }
            }
        };
        pomodoroTimer.start();
    }

    private void playAlarmSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.ring); // Replace with your sound file
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayer = null;
        });
    }

    private void showFinishDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Start a 5-minute break timer?")
                .setPositiveButton("Yes", (dialog, which) -> startBreakTimer())
                .setNegativeButton("No", (dialog, which) -> {
                    timerRunning = false;
                    startButton.setText(start);
                    Toast.makeText(MainActivity.this, "Thank you for using the Pomodoro timer!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void showNotification() {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "pomodoro_channel")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Pomodoro Timer")
                .setContentText("Your Pomodoro session is finished!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }

        notificationManager.notify(0, builder.build());
    }

    private void startBreakTimer() {
        long timerLength = 10 * 1000;
        pomodoroTimer = new CountDownTimer(timerLength, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = millisUntilFinished / 1000 % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                if (pomodoroTimer != null) {
                    pomodoroTimer.cancel();
                }
                timerRunning = false;
                playAlarmSound();
                showBreakFinishDialog();
            }
        };
        pomodoroTimer.start();
    }

    private void showBreakFinishDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Start a 25-minute Pomodoro timer?")
                .setPositiveButton("Yes", (dialog, which) -> startTimer())
                .setNegativeButton("No", (dialog, which) -> {
                    timerRunning = false;
                    startButton.setText(start);
                    Toast.makeText(MainActivity.this, "Thank you for using the Pomodoro timer!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}