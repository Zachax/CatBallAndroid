package com.sakari.firstgame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.ArrayList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread; // main game thread
    private CharacterSprite characterSprite; // player character
    private float firstTouchX, firstTouchY; // Would be only used with gesture controls active
    private MotionEvent localME; // for continuous control when touchpad is pressed
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private ArrayList<BallThing> balls = new ArrayList<>(); // balls in the game
    private int ballCounterDefault = 100; // starting delay of new balls appearing - higher is slower
    private int ballCounter = ballCounterDefault; // current delay - value keeps decreasing
    private double points = 0; // player score
    private double catHits = 1000; // hit points of player
    private int scoreTextSize = 50; // font size of score text
    private int scoreX = scoreTextSize / 2; // score text X coordinate
    private int scoreY = scoreTextSize + scoreTextSize / 4; // score text Y coordinate
    private Paint scorePaint = new Paint(); // score text color
    private Paint energyPaint = new Paint(); // energy text color
    private Paint bgPaint = new Paint(); // background color
    private int energyBarThickness = 5; // hit points bar thickness
    private boolean gameOver = false;

    private final int MAX_BALL_VELOCITY = 15; // spawned ball max speed
    private final int MAX_BALL_RADIUS = 65; // spawned ball max radius
    private final double PRIZE_CHANCE = 0.005; // price ball chance as % per frame
    private final int MINIMUM_BALL_COUNTER = 10; // minimum delay with new ball spawns
    private final boolean BALLS_DEFAULT_EDIBLE = false; // whether normal balls are consumable
    private final int POINT_MODIFIER = 1000; // higher value yields more points

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
        scorePaint.setTextSize(scoreTextSize);
        scorePaint.setColor(Color.RED);
        energyPaint.setColor(Color.GREEN);
    }

    // Not used in current version, but required due implementation
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        try {
            thread.start();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        }
        characterSprite = new CharacterSprite(BitmapFactory.decodeResource(getResources(),R.drawable.catface_small), this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    // Contstant updates of the whole game system
    public void update() {
        if (catHits <= 0) {
            gameOver = true;
            endGame();
        }

        // Checks if a touch is active - if is, movement action is carried on
        if (localME != null) {
            repeatMotion(localME);
        }
        characterSprite.update();
        for (int i = balls.size() - 1; i > 0; i--) {
            BallThing ball = balls.get(i);
            if (ball != null) {
                if (ball.isAlive()) {
                    ball.update();

                    if (characterSprite.ballTouching(ball.getX(), ball.getY(), ball.getRadius())) {
                        if (ball.isCollectible()) {
                            ball.setAlive(false);
                            points += (POINT_MODIFIER / ball.getRadius()) * 2;
                            System.out.println("Eaten balls: " + points);
                        } else {
                            catHits--;
                            //System.out.println("Cat gotten hit times: " + catHits);
                        }
                    }
                } else {
                    balls.remove(i);
                }
            }
        }
        ballCounter--;
        if (ballCounter < 0) {
            addBall(BALLS_DEFAULT_EDIBLE);
            ballCounter = ballCounterDefault;
            if (ballCounterDefault > MINIMUM_BALL_COUNTER) {
                ballCounterDefault--;
            }
            points += (POINT_MODIFIER / ballCounter) / 2;
        }
        if (PRIZE_CHANCE > Math.random()) {
            addBall(true);
        }
    }

    // Drawing of the game view
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(bgPaint.getColor());
            for (int i = 0; i < balls.size(); i++) {
                BallThing ball = balls.get(i);
                if (ball != null) {
                    ball.draw(canvas);
                }
            }
            characterSprite.draw(canvas);
            drawEnergy(canvas);
            if (gameOver) {
                Bitmap endSplash = BitmapFactory.decodeResource(getResources(),R.drawable.second_cat);
                RectF dst = new RectF(0, 0, screenWidth, screenHeight);
                canvas.drawBitmap(endSplash, null, dst, null);
                Paint endText = new Paint();
                endText.setTextSize(100);
                endText.setColor(Color.MAGENTA);
                canvas.drawText("RIP, meow", screenWidth / 4, screenHeight / 2, endText);

            }
            canvas.drawText("Score: " + (int) points, scoreX, scoreY, scorePaint);
        }
    }

    public void pause() {
        try {
            thread.setRunning(false);
            System.out.println("paused");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        try {
            thread = new MainThread(getHolder(), this);
            System.out.println("continue");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                /*
                firstTouchX = motionEvent.getX();
                firstTouchY = motionEvent.getY();
                */
                localME = motionEvent;
                System.out.println("Click X: " + motionEvent.getX() + " Y: " + motionEvent.getY());
                break;

            // Player is dragging finger
            case MotionEvent.ACTION_MOVE:
                localME = motionEvent;
                //System.out.println(motionEvent.getX() + " " + motionEvent.getY());
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                /*
                System.out.println("First X/Y: " + firstTouchX + " " + firstTouchY +
                        "Second X/Y: " + motionEvent.getX() + " " + motionEvent.getY());
                if (firstTouchX != motionEvent.getX() || firstTouchY != motionEvent.getY()) {
                    moveCharacterTowards(firstTouchX, firstTouchY, motionEvent.getX(), motionEvent.getX());
                }
                */
                localME = null;
                break;
        }
        return true;
    }

    public void repeatMotion(MotionEvent me) {
        if (me != null) {
            characterSprite.relateVelocity(me.getX(), me.getY());
        }
    }

    public void moveCharacterTowards(float x1, float y1, float x2, float y2) {
        characterSprite.addVelocity(x1+x2, y1+y2);
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void addBall(boolean collectible) {
        if (!collectible) {
            System.out.println("Adding a ball");
            BallThing ball = new BallThing(this, MAX_BALL_RADIUS, MAX_BALL_VELOCITY,
                    false);
            balls.add(ball);
            System.out.println(balls.size() + " balls in the game now.");
        } else {
            BallThing ball = new BallThing(this, MAX_BALL_RADIUS * 2, MAX_BALL_VELOCITY / 2,
                    true);
            balls.add(ball);
        }
    }

    public void drawEnergy(Canvas canvas) {
        int eX, eY;
        if (screenWidth > screenHeight) {
            eX = energyBarThickness;
            eY = screenHeight - energyBarThickness;
            canvas.drawRect(eX, eY, (float) (eX + catHits), eY+energyBarThickness, energyPaint);
        } else {
            eX = screenWidth - energyBarThickness;
            eY = energyBarThickness;
            canvas.drawRect(eX, eY, eX+energyBarThickness, (float) (eY + catHits), energyPaint);
        }
    }

    public int randomValue(int maxValue) {
        int value = (int) (Math.random() * maxValue);
        return value;
    }

    public void endGame() {
        System.out.println("Game over");
        //pause();
        thread.setRunning(false);
    }
}
