package com.gamecodeschool.snakegame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.graphics.BitmapFactory;
//fourth commit. Changing font and background color.
import android.graphics.Typeface;

class SnakeGame extends SurfaceView implements Runnable{
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    private volatile boolean mNewGame = true;
    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;
    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;
    // How many points does the player have
    private int mScore;
    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    // A snake ssss
    private Snake mSnake;
    // And an apple
    private Apple mApple;
    private List<GameObject> gameObjects = new ArrayList<>();

    private Bitmap mBitmapCanvas;

    // This is the constructor method that gets called
    // from SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);
        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;
        // Initialize the SoundPool
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

            // Prepare the sounds in memory
            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Error
        }
        // Initialize the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        // Call the constructors of our two game objects
        mApple = new Apple(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mSnake = new Snake(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);
        gameObjects.add(mApple); // Dynamic polymorphism
        gameObjects.add(mSnake); // Dynamic polymorphism
    }
    // Called to start a new game
    public void newGame() {
        // reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);

        // Get the apple ready for dinner
        mApple.spawn();

        // Reset the mScore
        mScore = 0;

        // Setup mNextFrameTime so an update can triggered
        mNextFrameTime = System.currentTimeMillis();
    }
    // Handles the game loop
    @Override
    public void run() {
        while (mPlaying) {
            if(!mPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }
            draw();
        }
    }
    // Check to see if it is time for an update
    public boolean updateRequired() {
        // Run at 10 frames per second
        final long TARGET_FPS = 10;

        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;
        // Are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed
            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;
            // Return true so that the update and draw
            // methods are executed
            return true;
        }
        return false;
    }
    // Update all the game objects
    public void update() {
        // Move the snake
        mSnake.move();
        // Did the head of the snake eat the apple?
        if(mSnake.checkDinner(mApple.getLocation())){
            // This reminds me of Edge of Tomorrow.
            // One day the apple will be ready!
            mApple.spawn();
            // Add to  mScore
            mScore = mScore + 1;
            // Play a sound
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);
        }
        // Did the snake die?
        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);
            mPaused = true;
            mNewGame = true;
        }
    }
    // Drawing method enhanced to iterate over GameObjects
    public void draw() {
        // Get a lock on the mCanvas
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();

            mBitmapCanvas = BitmapFactory.decodeResource(this.getResources(), R.drawable.game_background);
            mBitmapCanvas = Bitmap.createScaledBitmap(mBitmapCanvas, NUM_BLOCKS_WIDE*57, mNumBlocksHigh*57, false);
            mCanvas.drawBitmap(mBitmapCanvas, 0, 0, mPaint);


            // Add background to game
            mBitmapCanvas = BitmapFactory.decodeResource(this.getResources(), R.drawable.game_background);
            mBitmapCanvas = Bitmap.createScaledBitmap(mBitmapCanvas, NUM_BLOCKS_WIDE*57, mNumBlocksHigh*57, false);
            mCanvas.drawBitmap(mBitmapCanvas, 0, 0, mPaint);

            //Set text font
            Typeface nunitoTypeface = Typeface.createFromAsset(this.getContext().getAssets(), "fonts/Nunito-Regular.ttf");
            mPaint.setTypeface(nunitoTypeface);

            // Set the size and color of the mPaint for the text
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            mPaint.setTextSize(150);
            // Draw the score
            mCanvas.drawText("" + mScore, 20, 120, mPaint);

            // Draw the pause/resume button
            if(!mNewGame && !mPaused) {
                //Draw Pause button
                mPaint.setTextSize(50);
                mCanvas.drawText("Pause", 200, 100, mPaint);
            }
            else if (!mNewGame && mPaused) {
                mPaint.setTextSize(50);
                mPaint.setColor(Color.argb(255, 255, 255, 255));
                mCanvas.drawText("Resume", 200, 100, mPaint);
            }

            // Draw the apple and the snake
            // Iterate over GameObjects to draw them
            for (GameObject obj : gameObjects) {
                obj.draw(mCanvas, mPaint);
            }

            // Draw some text while paused
            if (mPaused && mNewGame) {
                // Set the size and color of the mPaint for the text
                mPaint.setColor(Color.argb(255, 255, 255, 255));
                mPaint.setTextSize(250);
                // Draw the message
                mCanvas.drawText("Tap To Play!", 200, 700, mPaint);
                mPaint.setColor(Color.argb(200, 0, 0, 128));
                mPaint.setTextSize(75);
                mCanvas.drawText("Zabi Aryan", 1905, 60, mPaint);
                mCanvas.drawText("Trenton Suddaby", 1700, 135, mPaint);
            }

            // Unlock the mCanvas and reveal the graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Check if the pause/resume button is pressed

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mPaused && mNewGame) {
                    mPaused = false;
                    mNewGame = false;
                    newGame();
                    // If the game is paused and the area outside the pause button is touched,
                    // don't process any further to avoid unintended game state changes.
                    return true;
                }
                else if (motionEvent.getX() < 390 && motionEvent.getY() < 100 && !mPaused
                         && motionEvent.getX() > 200) {

                    mPaused = true;
                    pause();
                    return true;
                }
                else if (motionEvent.getX() < 390 && motionEvent.getY() < 100 && mPaused
                         && motionEvent.getX() > 200) {

                    mPaused = false;
                    resume();
                    return true;
                }

                // Let the Snake class handle the input for changing direction
                mSnake.switchHeading(motionEvent);
                break;

            default:
                break;
        }
        return true;
    }
    // Stop the thread
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
        mNextFrameTime = System.currentTimeMillis();
    }
    // Start the thread
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}