package com.sakari.firstgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class CharacterSprite {
    private Bitmap image; // actual image
    private float width; // image width
    private float height; // image height
    private int x, y; // coordinates
    private float xVelocity = 20; // Speed on X axis
    private float yVelocity = 12; // Speed on Y acis
    private int responsivity = 100; // Control responsivity delay - higher = more sluggish reaction
    private float friction = 0.1f; // movement friction
    private final int MAX_VELOCITY = 30; // Maximum velocity
    private GameView gameView; // Main game system

    public CharacterSprite(Bitmap bmp, GameView gv) {

        image = bmp;
        gameView = gv;
        width = image.getWidth();
        height = image.getHeight();
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
    }

    public void update() {
        // Move the player according to velocity
        x += (int) xVelocity;
        y += (int) yVelocity;

        // Prevents character sprite from going off the screen borders
        checkBorders();

        // Slows down the character as simulation of movement friction
        slowDown();
    }

    private void checkBorders() {
        /**
         * To keep the player within game view, walls cause bouncing.
         * Also x/y is moved by a pixel in order to prevent sticking to walls.
         */
        if ((x >= gameView.getScreenWidth() - width) || (x <= 0)) {
            xVelocity = xVelocity * -1;
            if (x <= 0) {
                x++;
            } else {
                x--;
            }
        }
        if ((y >= gameView.getScreenHeight() - height) || (y <= 0)) {
            yVelocity = yVelocity * -1;
            if (y <= 0) {
                y++;
            } else {
                y--;
            }
        }
    }

    /**
     * Simulates movement friction, slows down character movement accordingly.
     */
    private void slowDown() {
        // Slow down X movement all the time
        if (xVelocity > 0) {
            xVelocity -= friction;
        } else if (xVelocity < 0) {
            xVelocity += friction;
        }
        // Slow down Y movement all the time
        if (yVelocity > 0) {
            yVelocity -= friction;
        } else if (yVelocity < 0) {
            yVelocity += friction;
        }
    }

    /**
     * Performs a velocity change related from character location to screen tapping location.
     * @param xTap X coordinate of a tap/click
     * @param yTap Y coordinate of a tap/click
     */
    public void relateVelocity(double xTap, double yTap) {
        addVelocity((xTap - x - width / 2) / responsivity, (yTap - y - height / 2) / responsivity);
    }

    /**
     * Does the actual velocity change
     * @param xV Velocity on X coordinate
     * @param yV Velocity on Y coordinate
     */
    public void addVelocity(double xV, double yV) {
        xVelocity += xV;
        yVelocity += yV;
        if (yVelocity > MAX_VELOCITY) {
            yVelocity = MAX_VELOCITY;
        } else if (yVelocity < -MAX_VELOCITY) {
            yVelocity = -MAX_VELOCITY;
        }
        if (xVelocity > MAX_VELOCITY) {
            xVelocity = MAX_VELOCITY;
        } else if (xVelocity < -MAX_VELOCITY) {
            xVelocity = -MAX_VELOCITY;
        }
    }

    /**
     * Checks if a ball is touching this character sprite [rectangle].
     * @param xB Ball x coordinate
     * @param yB Ball y coordinate
     * @param radius Ball radius
     * @return true if touching, false if not
     */
    public boolean ballTouching(int xB, int yB, int radius) {
        // Height and width of the player is adjusted for further proximity in impact
        float height = this.height * 0.8f;
        float width = this.width * 0.8f;

        float ballDistX = Math.abs(xB - (x + width / 2));
        float ballDistY = Math.abs(yB - (y + height / 2));

        if (ballDistX > (width/2 + radius)) {
            return false;
        }
        if (ballDistY > (height/2 + radius)) {
            return false;
        }

        if (ballDistX <= (width/2)) {
            return true;
        }
        if (ballDistY <= (height/2)) {
            return true;
        }

        double cornerDistance_sq = Math.pow(ballDistX - width/2, 2) +
                Math.pow(ballDistY - height/2, 2);

        return (cornerDistance_sq <= Math.pow(radius, 2));
    }
}