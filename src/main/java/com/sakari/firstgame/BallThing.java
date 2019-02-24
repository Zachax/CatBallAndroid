package com.sakari.firstgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BallThing {
    private int x, y; // coordinates
    private float radius; // ball radius
    private Paint color; // ball color
    private float xVelocity; // x velocity
    private float yVelocity; // y velocity
    private int maxRadius;
    private int maxVelocity;
    private GameView gameView;
    private boolean collectible;
    private boolean alive;

    public BallThing(GameView gv, int maxRad, int maxVel, boolean collect) {
        gameView = gv;
        maxRadius = maxRad;
        maxVelocity = maxVel;
        radius = gameView.randomValue(maxRadius);
        x = (int) (radius + gameView.randomValue(gv.getScreenWidth() - (int) radius * 2));
        y = (int) (radius + gameView.randomValue(gv.getScreenHeight() - (int) radius * 2));
        xVelocity = gameView.randomValue(maxVelocity);
        yVelocity = gameView.randomValue(maxVelocity);
        color = new Paint();
        setNewColor();
        collectible = collect;
        alive = true;
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, color);
    }

    public void update() {
        x += (int) xVelocity;
        y += (int) yVelocity;

        // To keep the object within game view, walls cause bouncing.
        // Also x/y is moved in order to prevent sticking to walls.
        if ((x >= gameView.getScreenWidth() - radius) || (x <= 0 + radius)) {
            xVelocity = xVelocity * -1;
            if (x <= 0 + radius) {
                x++;
            } else {
                x--;
            }
        }
        if ((y >= gameView.getScreenHeight() - radius) || (y <= 0 + radius)) {
            yVelocity = yVelocity * -1;
            if (y <= 0 + radius) {
                y++;
            } else {
                y--;
            }
        }
        if (collectible) {
            setNewColor();
            radius += 0.5;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return (int) radius;
    }

    public boolean isCollectible() {
        return collectible;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean life) {
        alive = life;
    }

    public void setNewColor() {
        if (color != null) {
            color.setColor(Color.rgb(gameView.randomValue(255),
                    gameView.randomValue(255),gameView.randomValue(255)));
        }
    }
}
