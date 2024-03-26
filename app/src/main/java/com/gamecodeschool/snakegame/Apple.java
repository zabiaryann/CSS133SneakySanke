package com.gamecodeschool.snakegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

class Apple implements GameObject {
    private Point location = new Point();
    private Point mSpawnRange;
    private int mSize;
    private Bitmap mBitmapApple;

    Apple(Context context, Point sr, int s){
        mSpawnRange = sr;
        mSize = s;
        location.x = -10;
        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
    }

    void spawn(){
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;
    }

    // Overloaded spawn method demonstrating static polymorphism
    void spawn(Point specificArea){
        Random random = new Random();
        location.x = random.nextInt(specificArea.x) + 1;
        location.y = random.nextInt(specificArea.y - 1) + 1;
    }

    Point getLocation(){
        return location;
    }

    @Override
    public void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(mBitmapApple, location.x * mSize, location.y * mSize, paint);
    }

    @Override
    public void update() {
        //  dynamic behavior
    }
}
