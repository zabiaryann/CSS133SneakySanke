// Defines the package this class belongs to
package com.gamecodeschool.snakegame;

// Import statements for necessary Android and Java classes
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

// The Apple class, which implements the GameObject and Consumable interfaces
class Apple implements GameObject, Consumable {
    // Private member variable to store the location of the apple. Hidden from other classes.
    private Point location = new Point();

    // Private member variable to define the range within which the apple can spawn.
    private Point mSpawnRange;

    // Private member variable to store the size of the apple.
    private int mSize;

    // Private member variable for the bitmap representation of the apple.
    private Bitmap mBitmapApple;

    // Constructor for the Apple class
    Apple(Context context, Point sr, int s){
        mSpawnRange = sr; // Initializes the spawn range of the apple.
        mSize = s; // Initializes the size of the apple.
        location.x = -10; // Initially sets the apple's location out of the visible area.
        // Loads the apple image from resources and scales it according to the size specified.
        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
    }

    // Method defined from the Consumable interface. Contains logic when the snake consumes the apple.
    @Override
    public void consume(Snake snake) {
        // Logic for when the snake consumes the apple...
    }

    // Method defined from the GameObject interface for checking collisions with other game objects.
    @Override
    public boolean checkCollision(GameObject other) {
        // Collision detection logic specific to Apple...
        return false; // Placeholder return value
    }

    // Public method to spawn the apple at a random location within the defined spawn range.
    void spawn(){
        Random random = new Random();
        // Randomly sets the apple's location within the spawn range.
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;
    }

    // Overloaded spawn method for spawning the apple within a specific area. Demonstrates static polymorphism.
    void spawn(Point specificArea){
        Random random = new Random();
        // Randomly sets the apple's location within the specific area.
        location.x = random.nextInt(specificArea.x) + 1;
        location.y = random.nextInt(specificArea.y - 1) + 1;
    }

    // Public getter method for the location of the apple. Allows read-only access.
    Point getLocation(){
        return location;
    }

    // Method defined from the GameObject interface to draw the apple on the canvas.
    @Override
    public void draw(Canvas canvas, Paint paint){
        // Draws the bitmap representation of the apple at its location.
        canvas.drawBitmap(mBitmapApple, location.x * mSize, location.y * mSize, paint);
    }

    // Method defined from the GameObject interface for updating the apple's state. Placeholder for dynamic behavior.
    @Override
    public void update() {
        // Placeholder for future dynamic behavior
    }
}
