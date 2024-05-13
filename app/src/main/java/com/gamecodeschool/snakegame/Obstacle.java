package com.gamecodeschool.snakegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

class Obstacle implements GameObject{
    private Point location = new Point();
    private Point mSpawnRange;
    private int mSize;
    private Bitmap mBitmapObstacle;

    Obstacle(Context context, Point spawnRange, int size){
        mSpawnRange = spawnRange;
        mSize = size;
        location.x = -10;

        mBitmapObstacle = BitmapFactory.decodeResource(context.getResources(), R.drawable.obstacle);
        mBitmapObstacle = Bitmap.createScaledBitmap(mBitmapObstacle, size, size, false);
    }

    void spawn(Point appleLocation){
        Random random = new Random();

        int minDistance = 1;  // Minimum distance from the apple
        int maxDistance = 5;  // Maximum distance from the apple

        // Generate random distances within the specified range
        int distanceX = minDistance + random.nextInt(maxDistance - minDistance + 1);
        int distanceY = minDistance + random.nextInt(maxDistance - minDistance + 1);

        // Randomly decide to add or subtract distance from apple's location
        distanceX *= random.nextBoolean() ? 1 : -1;
        distanceY *= random.nextBoolean() ? 1 : -1;

        // Calculate new location for the obstacle ensuring it's within the game bounds
        int newX = appleLocation.x + distanceX;
        int newY = appleLocation.y + distanceY;

        // Ensure the obstacle does not spawn outside the playable area
        newX = Math.max(1, Math.min(mSpawnRange.x - 1, newX));
        newY = Math.max(1, Math.min(mSpawnRange.y - 1, newY));

        // Set the obstacle's location
        location.set(newX, newY);

    }
    Point getLocation(){
        return location;
    }

    @Override
    public void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(mBitmapObstacle, location.x * mSize, location.y * mSize, paint);
    }

    @Override
    public void update() {

    }

}