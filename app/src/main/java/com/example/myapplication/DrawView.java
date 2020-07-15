/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * From: https://github.com/MARGI3/CropImage
 */


package com.example.myapplication;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {

    Point[] points = new Point[4];

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    int groupId = -1;
    private ArrayList<ColorBall> colorballs = new ArrayList<>();

    private int mStrokeColor = Color.parseColor("#AADB1255");
    private int mFillColor = Color.parseColor("#55DB1255");
    private Rect mCropRect = new Rect();

    // array that holds the balls
    private int balID = 0;
    // variable to know what ball is being dragged
    Paint paint;

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        setFocusable(true); // necessary for getting the touch events
    }

    private void initRectangle(int X, int Y, int width, int height) {
        ColorBall colorBall = new ColorBall(getContext(), R.drawable.ic_circle, new Point(), 0);

        //initialize rectangle.
        points[0] = new Point();
        points[0].x = X;
        points[0].y = Y;

        points[1] = new Point();
        points[1].x = X;
        points[1].y = height - colorBall.getHeightOfBall();

        points[2] = new Point();
        points[2].x = width - colorBall.getWidthOfBall();
        points[2].y = height - colorBall.getHeightOfBall();

        points[3] = new Point();
        points[3].x = width - colorBall.getWidthOfBall();
        points[3].y = Y;

        balID = 2;
        groupId = 1;
        // declare each ball with the ColorBall class
        for (int i = 0; i < points.length; i++) {
            colorballs.add(new ColorBall(getContext(), R.drawable.ic_circle, points[i], i));
        }
    }

    // the method that draws the balls
    @Override
    protected void onDraw(Canvas canvas) {
        if(points[3]==null) {
            //point4 null when view first create
            initRectangle(0, 0, getWidth(), getHeight());
        }

        int left, top, right, bottom;
        left = points[0].x;
        top = points[0].y;
        right = points[0].x;
        bottom = points[0].y;
        for (int i = 1; i < points.length; i++) {
            left = Math.min(left, points[i].x);
            top = Math.min(top, points[i].y);
            right = Math.max(right, points[i].x);
            bottom = Math.max(bottom, points[i].y);
        }
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5);

        //draw stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(mStrokeColor);
        paint.setStrokeWidth(2);

        mCropRect.left = left + colorballs.get(0).getWidthOfBall() / 2;
        mCropRect.top = top + colorballs.get(0).getWidthOfBall() / 2;
        mCropRect.right = right + colorballs.get(2).getWidthOfBall() / 2;
        mCropRect.bottom = bottom + colorballs.get(3).getWidthOfBall() / 2;
        canvas.drawRect(mCropRect, paint);

        //fill the rectangle
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mFillColor);
        paint.setStrokeWidth(0);
        canvas.drawRect(mCropRect, paint);

        // draw the balls on the canvas
        paint.setColor(Color.RED);
        paint.setStrokeWidth(0);
        for (int i =0; i < colorballs.size(); i ++) {
            ColorBall ball = colorballs.get(i);
            canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(),
                    paint);
        }
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventAction) {

            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                // a ball
                if (points[0] == null) {
                    initRectangle(X, Y, this.getWidth(), this.getHeight());
                } else {
                    //resize rectangle
                    balID = -1;
                    groupId = -1;
                    for (int i = colorballs.size()-1; i>=0; i--) {
                        ColorBall ball = colorballs.get(i);
                        // check if inside the bounds of the ball (circle)
                        // get the center for the ball
                        int centerX = ball.getX() + ball.getWidthOfBall();
                        int centerY = ball.getY() + ball.getHeightOfBall();
                        paint.setColor(Color.CYAN);
                        // calculate the radius from the touch to the center of the
                        // ball
                        double radCircle = Math
                                .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                        * (centerY - Y)));

                        if (radCircle < ball.getWidthOfBall()) {

                            balID = ball.getID();
                            if (balID == 1 || balID == 3) {
                                groupId = 2;
                            } else {
                                groupId = 1;
                            }
                            invalidate();
                            break;
                        }
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE: // touch drag with the ball

                if (balID > -1) {
                    // move the balls the same as the finger
                    colorballs.get(balID).setX(X);
                    colorballs.get(balID).setY(Y);

                    paint.setColor(Color.CYAN);
                    if (groupId == 1) {
                        colorballs.get(1).setX(colorballs.get(0).getX());
                        colorballs.get(1).setY(colorballs.get(2).getY());
                        colorballs.get(3).setX(colorballs.get(2).getX());
                        colorballs.get(3).setY(colorballs.get(0).getY());
                    } else {
                        colorballs.get(0).setX(colorballs.get(1).getX());
                        colorballs.get(0).setY(colorballs.get(3).getY());
                        colorballs.get(2).setX(colorballs.get(3).getX());
                        colorballs.get(2).setY(colorballs.get(1).getY());
                    }

                    invalidate();
                }

                break;

            case MotionEvent.ACTION_UP:
                // touch drop - just do things here after dropping
                // doTheCrop()
                break;
        }
        // redraw the canvas
        invalidate();
        return true;
    }

    public BitmapDrawable getSelectedRegion() {
        Bitmap sourceBitmap = null;
        Drawable backgroundDrawable = getBackground();
        if (backgroundDrawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) backgroundDrawable;
            if(bitmapDrawable.getBitmap() != null) {
                sourceBitmap = bitmapDrawable.getBitmap();
            }
        }
        if (sourceBitmap != null) {
            //source bitmap was scaled, you should calculate the rate
            float widthRate = ((float) sourceBitmap.getWidth()) / getWidth();
            float heightRate =  ((float) sourceBitmap.getHeight()) / getHeight();

            //crop the source bitmap with rate value
            int left = (int) (mCropRect.left * widthRate);
            int top = (int) (mCropRect.top * heightRate);
            int right = (int) (mCropRect.right * widthRate);
            int bottom = (int) (mCropRect.bottom * heightRate);
            Bitmap croppedBitmap = Bitmap.createBitmap(sourceBitmap, left, top, right - left, bottom - top);
            BitmapDrawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
            return drawable;
        }
        return null;
    }

    public static class ColorBall {

        Bitmap bitmap;
        Context mContext;
        Point point;
        int id;

        ColorBall(Context context, int resourceId, Point point, int id) {
            this.id = id;

            Drawable drawable = context.getResources().getDrawable(resourceId);
            Canvas canvas = new Canvas();
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            mContext = context;
            this.point = point;
        }

        int getWidthOfBall() {
            return bitmap.getWidth();
        }

        int getHeightOfBall() {
            return bitmap.getHeight();
        }

        Bitmap getBitmap() {
            return bitmap;
        }

        int getX() {
            return point.x;
        }

        int getY() {
            return point.y;
        }

        int getID() {
            return id;
        }

        void setX(int x) {
            point.x = x;
        }

        void setY(int y) {
            point.y = y;
        }
    }
}