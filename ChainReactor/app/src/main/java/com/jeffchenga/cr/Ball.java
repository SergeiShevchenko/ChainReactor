package com.jeffchenga.cr;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by ACER on 15.03.14.
 */
public class Ball
{
    public float x;
    public float y;
    public float radius;

    public float vx;
    public float vy;
    public int color;
    private Paint paint;
    private Paint paintStroke;

    public int[] Colors = {Color.RED,Color.GRAY,Color.BLUE,Color.YELLOW,Color.WHITE,Color.CYAN,Color.LTGRAY,Color.BLACK, Color.MAGENTA, Color.GREEN};

    public Ball(float X, float Y, float Rad, float Vx, float Vy, int Color)
    {
        x = X;
        y = Y;
        radius = Rad;
        vx = Vx;
        vy = Vy;
        color = Color;
        paint = new Paint();
        paint.setColor(decodeColor(color));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paintStroke = new Paint();
        paintStroke.setStyle(Paint.Style.STROKE);
    }

    int decodeColor(int c)
    {
        c %= Colors.length;
        return Colors[c];
    }

    public void Move()
    {
        x+=vx;
        y+=vy;
    }

    public void Draw(Canvas canvas)
    {
        canvas.drawCircle(x,y,radius,paint);
        canvas.drawCircle(x,y,radius,paintStroke);
    }

    public double Distance(float x1, float y1, float x2, float y2)
    {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public boolean Intersect(Ball b)
    {
        if (Distance(this.x,this.y,b.x,b.y)<=this.radius+b.radius)
            return true;
        else return false;
    }

    public void reverseVx()
    {
        vx*=-1;
    }
    public void reverseVy()
    {
        vy*=-1;
    }
}
