package com.gamecodeschool.snakegame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;

class SnakeGame extends SurfaceView implements Runnable {
    private Thread mThread = null;
    private long mNextFrameTime;
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    private volatile boolean mNewGame = true;
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;
    private int mScore;
    private int highScore;
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private Snake mSnake;
    private Apple mApple;
    private List<GameObject> gameObjects = new ArrayList<>();
    private Bitmap mBitmapCanvas;
    private Obstacle mObstacle;
    MediaPlayer player;

    public SnakeGame(Context context, Point size) {
        super(context);
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);
        } catch (IOException e) {
            // Error handling
        }
        player = MediaPlayer.create(context, R.raw.naruto);
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mObstacle = new Obstacle(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        gameObjects.add(mApple);
        gameObjects.add(mSnake);
        gameObjects.add(mObstacle);
        SharedPreferences prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        highScore = prefs.getInt("HighScore", 0); // Load high score from SharedPreferences
    }

    public void newGame() {
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn();
        mObstacle.spawn(mApple.getLocation());
        mScore = 0; // Reset the score
        mNextFrameTime = System.currentTimeMillis();
        player.start();
        player.setLooping(true);

    }

    @Override
    public void run() {
        while (mPlaying) {
            if (!mPaused) {
                if (updateRequired()) {
                    update();
                }
            }
            draw();
        }
    }

    public boolean updateRequired() {
        final long TARGET_FPS = 10;
        final long MILLIS_PER_SECOND = 1000;
        if (mNextFrameTime <= System.currentTimeMillis()) {
            mNextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / TARGET_FPS;
            return true;
        }
        return false;
    }

    public void update() {
        mSnake.move();
        if (mSnake.checkDinner(mApple.getLocation())) {
            mApple.spawn();
            mObstacle.spawn(mApple.getLocation());
            mScore += 1;
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);
            if (mScore > highScore) {
                highScore = mScore;
                SharedPreferences prefs = getContext().getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("HighScore", highScore);
                editor.apply();
            }
        }
        if (mSnake.detectDeath() || mSnake.checkCollision(mObstacle.getLocation())) {
            player.pause();
            mSP.play(mCrashID, 1, 1, 0, 0, 1);
            mPaused = true;
            mNewGame = true;
        }
    }

    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();
            mBitmapCanvas = BitmapFactory.decodeResource(this.getResources(), R.drawable.game_background);
            mBitmapCanvas = Bitmap.createScaledBitmap(mBitmapCanvas, NUM_BLOCKS_WIDE * 57, mNumBlocksHigh * 57, false);
            mCanvas.drawBitmap(mBitmapCanvas, 0, 0, mPaint);
            Typeface nunitoTypeface = Typeface.createFromAsset(this.getContext().getAssets(), "fonts/Nunito-Regular.ttf");
            mPaint.setTypeface(nunitoTypeface);
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            mPaint.setTextSize(150);
            mCanvas.drawText("Score: " + mScore, 20, 120, mPaint);
            mCanvas.drawText("High Score: " + highScore, 20, 270, mPaint);
            if (!mNewGame && !mPaused) {
                mPaint.setTextSize(50);
                mCanvas.drawText("Pause", 2000, 100, mPaint);
            } else if (!mNewGame && mPaused) {
                mPaint.setTextSize(50);
                mCanvas.drawText("Resume", 2000, 100, mPaint);
            }
            for (GameObject obj : gameObjects) {
                obj.draw(mCanvas, mPaint);
            }
            if (mPaused && mNewGame) {
                mPaint.setTextSize(250);
                mCanvas.drawText("Tap To Play!", 200, 700, mPaint);
                mPaint.setColor(Color.argb(200, 0, 0, 128));
                mPaint.setTextSize(75);
                mCanvas.drawText("Zabi Aryan", 1600, 50, mPaint);
                mCanvas.drawText("Trenton Suddaby", 1600, 135, mPaint);
                mCanvas.drawText("Jaime Montanez", 1600, 200, mPaint);
            }
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mPaused && mNewGame) {
                    mPaused = false;
                    mNewGame = false;
                    newGame();
                    return true;
                } else if (motionEvent.getX() < 2190 && motionEvent.getY() < 100 && !mPaused && motionEvent.getX() > 2000) {
                    mPaused = true;
                    pause();
                    player.pause();
                    return true;
                } else if (motionEvent.getX() < 2190 && motionEvent.getY() < 100 && mPaused && motionEvent.getX() > 2000) {
                    mPaused = false;
                    resume();
                    player.start();
                    return true;
                }
                mSnake.switchHeading(motionEvent);
                break;
        }
        return true;
    }

    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error handling
        }
        mNextFrameTime = System.currentTimeMillis();
    }

    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}
