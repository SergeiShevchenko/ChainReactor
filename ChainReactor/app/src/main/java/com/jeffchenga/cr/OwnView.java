package com.jeffchenga.cr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.EditText;
import android.app.Activity;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ACER on 13.03.14.
 */
public class OwnView extends View {

    Context context;
    List<Ball> balls;
    List<ExplodingBall> explodingBalls;
    MediaPlayer player;
    MediaPlayer winLose;
    int height;
    int width;
    int maxSpeed = 5;
    int avgRadius = 20;
    int maxRadius = 50;
    int score = 0;
    int overallScore = 0;
    Paint textColor;
    boolean started;
    ShakeDetectActivity shakeDetectActivity;
    boolean sameSize = true;
    boolean sounds = true;

    int[] levels = {30,20,17,15,13,10,5,3,2};

    int currentLevel;
    float percentageToWin;
    ArrayList<Integer> colors;
    private Runnable r = new Runnable() {

        @Override

        public void run() {
            invalidate();
        }

    };

    private Handler h;
    int FRAME_RATE = 25;
    String message = "";

    boolean madeIt;

    public OwnView(Context contexT, int wi, int he) {
        super(contexT);
        context = contexT;

        shakeDetectActivity = new ShakeDetectActivity(context);
        shakeDetectActivity.addListener(
                new ShakeDetectActivityListener() {
                    @Override
                    public void shakeDetected() {
                        OwnView.this.triggerShakeDetected();
                    }
                });


        width =wi;
        height =he;

        currentLevel = 0;
        percentageToWin = 0.5F;
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        avgRadius = width/20;
        maxRadius = avgRadius*3;
        InitializePlayground();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        hitTest(event.getX(), event.getY());
        return super.onTouchEvent(event);
    }

    String backPath;

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        if (bm.getWidth()> bm.getHeight())
            bm = RotateBitmap(bm, 90);

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;


        // Resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);



        return resizedBitmap;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    void GetTheColorScheme()
    {
        colors = new ArrayList<Integer>();
        String mes = "";
        final String filename = "colors.txt";

        FileInputStream fis;

        final StringBuffer storedString = new StringBuffer();

        try {
            fis = context.openFileInput(filename);
            DataInputStream dataIO = new DataInputStream(fis);
            String strLine = null;

            while ((strLine = dataIO.readLine()) != null) {
                colors.add(new Integer(strLine));
            }
            dataIO.close();
            fis.close();
        }
        catch  (Exception e) {
            mes = "";
            colors.add(1);
        }
    }

    void InitializePlayground()
    {
        GetTheColorScheme();

        h = new Handler();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        sounds = preferences.getBoolean("sounds_checkbox",true);
        sameSize = preferences.getBoolean("size_checkbox", true);

        if (sounds)
        {
            player = MediaPlayer.create(context,R.raw.hit);
            winLose = MediaPlayer.create(context, R.raw.win);
        }

        madeIt = false;
        score = 0;
        started = false;

        balls = new ArrayList<Ball>();
        explodingBalls = new ArrayList<ExplodingBall>();


        textColor = new Paint();
        textColor.setColor(Color.BLACK);
        textColor.setTextSize(30);

        int sizeDifference=1;
        if (!sameSize) sizeDifference = 15;

        backPath = PreferenceManager.getDefaultSharedPreferences(context).getString("background_path", "no");
        try
        {
            File dest = new File(backPath);
            FileInputStream fis;
            fis = new FileInputStream(dest);
            Bitmap temp = BitmapFactory.decodeStream(fis);

            Drawable drawable = new BitmapDrawable(getResources(), getResizedBitmap(temp, height, width));

            setBackgroundDrawable(drawable);
        }
        catch (Exception e)
        {

        }

        Random r = new Random();

        for (int i=0; i<levels[currentLevel]; i++)
        {
            balls.add(new Ball(r.nextInt(width-avgRadius), r.nextInt(height-avgRadius), avgRadius+r.nextInt(sizeDifference), r.nextInt(maxSpeed)+1, r.nextInt(maxSpeed)+1, (int)colors.get(r.nextInt(colors.size()))));
        }
    }



    private void updatePositions()
    {
        if ((started)&&explodingBalls.size()==0)
        {
            if (madeIt)
            {
                NextLevel();
            }
            else
                ThisLevel();
        }
        else
        {

            for (int i=0; i<balls.size(); i++)
            {
                Ball b = balls.get(i);

                for (int j=0; j<explodingBalls.size(); j++)
                {
                    Ball eb = explodingBalls.get(j);
                    if (b.Intersect(eb))
                    {
                        if (sounds)
                        {
                            player.start();
                        }
                        explodingBalls.add(new ExplodingBall(b.x,b.y,b.radius,0,0,b.color,maxRadius));
                        balls.remove(b);
                        i--;
                        score++;
                        didHeMakeIt();
                        break;
                    }
                }

                if (b!=null)
                {
                    if (b.y + b.radius >= height)
                    {
                        b.y = height - b.radius;
                        b.reverseVy();
                    }
                    if (b.y - b.radius <= 0)
                    {
                        b.y = 0 + b.radius;
                        b.reverseVy();
                    }
                    if (b.x + b.radius >= width)
                    {
                        b.reverseVx();
                        b.x=width-b.radius;
                    }
                    if (b.x - b.radius <= 0)
                    {
                        b.reverseVx();
                        b.x=0+b.radius;
                    }


                    for (int j=0; j<balls.size(); j++)
                    {
                        if (i==j) continue;
                        Ball a = balls.get(j);
                        if (b.Intersect(a))
                        {
                                if ((a.y>b.y)&&(b.vy>0)) b.reverseVy();
                                if ((a.y<b.y)&&(b.vy<0)) b.reverseVy();

                                if ((a.x>b.x)&&(b.vx>0)) b.reverseVx();
                                if ((a.x<b.x)&&(b.vx<0)) b.reverseVx();

                            break;
                        }
                    }
                }
                b.Move();
            }

            for (int i=0; i<explodingBalls.size(); i++)
            {
                ExplodingBall b = explodingBalls.get(i);
                b.Move();
                if (b.radius<1)
                {
                    explodingBalls.remove(b);
                    i--;
                }
            }
        }

    }

    void didHeMakeIt()
    {
        if (!madeIt)
        {
            if (score>=(int) Math.ceil(levels[currentLevel]*percentageToWin))
            {
                madeIt = true;
                if (sounds)
                {
                    winLose = MediaPlayer.create(context,R.raw.win);
                    winLose.start();
                }
            }
        }

    }

    boolean running = true;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        updatePositions();

        for (int i=0; i<balls.size(); i++)
        {
            Ball b = balls.get(i);
            b.Draw(canvas);
        }

        for (int i=0; i<explodingBalls.size(); i++)
        {
            ExplodingBall b = explodingBalls.get(i);
            b.Draw(canvas);
        }

        h.postDelayed(r, FRAME_RATE);
    }

    public boolean hitTest(float xx, float yy)
    {
        if (!started)
        {
            explodingBalls.add(new ExplodingBall(xx,yy,avgRadius,0,0,1,maxRadius));
            started = true;
        }

        //h.removeCallbacksAndMessages(null);
        return false;
    }

    void drawBackground(Canvas canvas)
    {
        //canvas.drawRect(1, 1, width-1, height-1, backgroundColor);
        /*if (backgroundImage!=null)
            canvas.drawBitmap(backgroundImage,0,0,null);*/
        canvas.drawText("Score: " + String.valueOf(score), 20, 40, textColor);
        canvas.drawText("Level: "+String.valueOf(currentLevel+1), 20, 80, textColor);
        canvas.drawText("To win: "+String.valueOf((int) Math.ceil(levels[currentLevel]*percentageToWin)), width-180, 40, textColor);
        canvas.drawText("Game score: "+String.valueOf(overallScore), width-230, 80, textColor);
        //canvas.drawText(message, width/2-40, height-40, textColor);
    }

    void NextLevel()
    {
        /*if (sounds)
        {
            player = MediaPlayer.create(context,R.raw.win);
            player.start();
        }*/

        message= "Next level!";
        Toast toast = Toast.makeText(context, "Next level!", Toast.LENGTH_SHORT);
        toast.show();
        overallScore+=score;
        h.removeCallbacksAndMessages(null);
        if (currentLevel!=levels.length-1) currentLevel++;
        else
        {
            toast = Toast.makeText(context, "You have completed the game!", Toast.LENGTH_SHORT);
            toast.show();
            message = "You have completed the game!";
            highScoreSubmit(overallScore);
            currentLevel = 0;
            overallScore = 0;
        }
        InitializePlayground();
    }

    void ThisLevel()
    {
        if (sounds)
        {
            winLose = MediaPlayer.create(context,R.raw.lose);
            winLose.start();
        }
        message = "Try again!";
        Toast toast = Toast.makeText(context, "Try again!", Toast.LENGTH_SHORT);
        toast.show();
        h.removeCallbacksAndMessages(null);
        InitializePlayground();
    }

    void highScoreSubmit(final int overScore)
    {
        AlertDialog.Builder alert1 = new AlertDialog.Builder(context);

        alert1.setTitle("High-score time!");
        alert1.setMessage("Enter your name:");

        // Set an EditText view to get user input
        final EditText input = new EditText(context);
        alert1.setView(input);

        alert1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                Highscore hs = new Highscore(context, overScore, currentLevel+1, value.toString());
            }
        });

        alert1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                return;
                // Canceled.
            }
        });

        alert1.show();
    }




    public void triggerShakeDetected()
    {
        for (int i=0; i<balls.size(); i++)
        {
            balls.get(i).vx = -balls.get(i).vx;
            balls.get(i).vy = -balls.get(i).vy;
        }
    }


}
