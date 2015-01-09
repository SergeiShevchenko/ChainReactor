package com.jeffchenga.cr;

/**
 * Created by ACER on 15.03.14.
 */
public class ExplodingBall extends Ball
{
    boolean Exploding;
    float MaxRadius;
    public ExplodingBall(float X, float Y, float Rad, float Vx, float Vy, int color, int maxR) {
        super(X, Y, Rad, Vx, Vy, color);
        Exploding = true;
        MaxRadius = maxR;
    }

    public void Move()
    {
        if (Exploding)
        {
            radius++;
            if (radius>=MaxRadius)
                Exploding = false;
        }
        else
        {
            radius--;
        }
    }
}
