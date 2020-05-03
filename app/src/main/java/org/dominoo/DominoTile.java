package org.dominoo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class DominoTile implements Comparable<DominoTile> {

    int mNumber1;
    int mNumber2;

    public DominoTile(int number1, int number2) {

        mNumber1 = number1;
        mNumber2 = number2;
    }

    public void drawTile(Canvas canvas, RectF tileRect, int tileColor, boolean swapNumbers) {

        Paint fillPaint=new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStrokeWidth(3);
        fillPaint.setAntiAlias(true);
        fillPaint.setColor(tileColor);

        Paint strokePaint=new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(5);
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLACK);

        canvas.drawRect(tileRect, fillPaint);

        canvas.drawRect(tileRect, strokePaint);

        // Check tile orientation

        boolean horizontal = false;

        if (tileRect.width() > tileRect.height()) {

            horizontal = true;
        }

        // Draw center line

        float startX, startY, stopX, stopY;
        float LINE_MARGIN = 10;

        if (horizontal) {

            startX = tileRect.centerX();
            startY = tileRect.top+LINE_MARGIN;
            stopX = tileRect.centerX();
            stopY = tileRect.bottom-LINE_MARGIN;
        }
        else {

            startX = tileRect.left+LINE_MARGIN;
            startY = tileRect.centerY();
            stopX = tileRect.right-LINE_MARGIN;
            stopY = tileRect.centerY();
        }

        canvas.drawLine(startX, startY, stopX, stopY, strokePaint);

        // Draw number 1

        RectF number1Rect;

        if (horizontal) {
            number1Rect = new RectF(tileRect.left, tileRect.top,
                    tileRect.centerX(), tileRect.bottom);
        }
        else {

            number1Rect = new RectF(tileRect.left, tileRect.top,
                    tileRect.right, tileRect.centerY());
        }

        fillPaint.setColor(Color.BLACK);

        int pos1Number;

        if (swapNumbers) {

            pos1Number = mNumber2;
        }
        else {

            pos1Number = mNumber1;
        }

        drawDots(canvas, pos1Number, number1Rect, horizontal, fillPaint);

        RectF number2Rect;

        if (horizontal) {

            number2Rect = new RectF(tileRect.centerX(), tileRect.top,
                    tileRect.right, tileRect.bottom);
        }
        else {

            number2Rect = new RectF(tileRect.left, tileRect.centerY(),
                    tileRect.right, tileRect.bottom);
        }

        int pos2Number;

        if (swapNumbers) {

            pos2Number = mNumber1;
        }
        else {

            pos2Number = mNumber2;
        }

        drawDots(canvas, pos2Number, number2Rect, horizontal, fillPaint);
    }

    private void drawDots(Canvas canvas, int number, RectF rect, boolean horizontal,
                          Paint paint) {

        float centerX = rect.centerX();
        float centerY = rect.centerY();

        float radius = rect.width()/10;

        float offset = rect.width()/4;

        if (number == 0) {

        }
        else if (number == 1) {

            canvas.drawCircle(centerX, centerY, radius, paint);
        }
        else if (number == 2) {

            if (horizontal) {

                canvas.drawCircle(centerX+offset, centerY+offset, radius, paint);
                canvas.drawCircle(centerX-offset, centerY-offset, radius, paint);
            }
            else {

                canvas.drawCircle(centerX-offset, centerY+offset, radius, paint);
                canvas.drawCircle(centerX+offset, centerY-offset, radius, paint);
            }
        }
        else if (number == 3) {

            canvas.drawCircle(centerX, centerY, radius, paint);

            if (horizontal) {

                canvas.drawCircle(centerX+offset, centerY+offset, radius, paint);
                canvas.drawCircle(centerX-offset, centerY-offset, radius, paint);
            }
            else {

                canvas.drawCircle(centerX-offset, centerY+offset, radius, paint);
                canvas.drawCircle(centerX+offset, centerY-offset, radius, paint);
            }
        }
        else if (number == 4) {

            canvas.drawCircle(centerX-offset, centerY-offset, radius, paint);
            canvas.drawCircle(centerX-offset, centerY+offset, radius, paint);
            canvas.drawCircle(centerX+offset, centerY-offset, radius, paint);
            canvas.drawCircle(centerX+offset, centerY+offset, radius, paint);
        }
        else if (number == 5) {

            canvas.drawCircle(centerX, centerY, radius, paint);

            canvas.drawCircle(centerX-offset, centerY-offset, radius, paint);
            canvas.drawCircle(centerX-offset, centerY+offset, radius, paint);
            canvas.drawCircle(centerX+offset, centerY-offset, radius, paint);
            canvas.drawCircle(centerX+offset, centerY+offset, radius, paint);
        }
        else if (number == 6) {

            canvas.drawCircle(centerX-offset, centerY-offset, radius, paint);
            canvas.drawCircle(centerX-offset, centerY+offset, radius, paint);
            canvas.drawCircle(centerX+offset, centerY-offset, radius, paint);
            canvas.drawCircle(centerX+offset, centerY+offset, radius, paint);

            if (horizontal) {

                canvas.drawCircle(centerX, centerY-offset, radius, paint);
                canvas.drawCircle(centerX, centerY+offset, radius, paint);
            }
            else {

                canvas.drawCircle(centerX-offset, centerY, radius, paint);
                canvas.drawCircle(centerX+offset, centerY, radius, paint);
            }
        }
        else {

            canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, paint);
            canvas.drawLine(rect.left, rect.bottom, rect.right, rect.top, paint);
        }

    }

    static public void sortTiles(ArrayList<DominoTile> tiles) {

        // First of all, turn tiles (lower dots first...)

        Iterator<DominoTile> iter = tiles.iterator();

        while(iter.hasNext()) {

            DominoTile tile = iter.next();

            if (tile.mNumber1 > tile.mNumber2) {

                int aux = tile.mNumber1;

                tile.mNumber1 = tile.mNumber2;

                tile.mNumber2 = aux;
            }
        }

        Collections.sort(tiles);

    }

    @Override
    public int compareTo(DominoTile o) {

        if (mNumber1 == o.mNumber1) {

            if (mNumber2 == o.mNumber2) {

                // Domino tiles are the same
                return 0;
            }
            else if (mNumber2 < o.mNumber2) {

                return -1;
            }
            else {

                return 1;
            }
        }
        else if (mNumber1 < o.mNumber1) {

            return -1;
        }

        return 1;
    }

    public boolean isDouble() {

        return (mNumber1 == mNumber2);
    }

    public boolean contains(int mNumber) {

        if (mNumber1 == mNumber) {

            return true;
        }

        if (mNumber2 == mNumber) {

            return true;
        }

        return false;
    }
}
