package org.dominoo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class GameBoardView extends View {

    private static final int BORDER_MARGIN = 5;

    private static final int TEXT_SIZE = 20;

    private static final float TILE_SIZE_SCALE_FACTOR = 7;

    private Game.PlayerPos mTurnPlayer = Game.PlayerPos.NONE;

    private String mPlayerName = null;
    private String mPartnerName = null;
    private String mLeftOpponentName = null;
    private String mRightOpponentName = null;

    private int mPartnerTileCount = 7;
    private int mLeftOpponentTileCount = 7;
    private int mRightOpponentTileCount = 7;

    private ArrayList<DominoTile> mBoardTiles1 = null;
    private ArrayList<DominoTile> mBoardTiles2 = null;

    private RectF mNextBoardTile1Rect = null;
    private RectF mNextBoardTile2Rect = null;

    private boolean mHighlightNextBoardTile1 = false;
    private boolean mHighlightNextBoardTile2 = false;

    public GameBoardView(Context context) {
        super(context);

    }

    public GameBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public GameBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF borderRect = drawBorder(canvas);

        drawPlayerNames(canvas, borderRect);

        float tileSize = calculateTileSize(borderRect);

        drawPlayerTiles(canvas, borderRect, tileSize);

        drawBoardTiles1(canvas, borderRect, tileSize);

        drawBoardTiles2(canvas, borderRect, tileSize);

        drawNextBoardTiles(canvas);
    }

    private float calculateTileSize(RectF borderRect) {

        return borderRect.height()/TILE_SIZE_SCALE_FACTOR;
    }

    private RectF drawBorder(Canvas canvas) {

        RectF borderRect;

        borderRect=new RectF(BORDER_MARGIN, BORDER_MARGIN,
                getWidth()-BORDER_MARGIN, getHeight()-BORDER_MARGIN);

        Paint paint=new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        //canvas.drawRect(borderRect, paint);

        /*
        paint.setColor(Color.YELLOW);
        canvas.drawLine(borderRect.left, borderRect.centerY(),
                borderRect.right, borderRect.centerY(), paint);

        canvas.drawLine(borderRect.centerX(), borderRect.top,
                borderRect.centerX(), borderRect.bottom, paint);
        */

        return borderRect;
    }

    private void drawPlayerNames(Canvas canvas, RectF borderRect) {

        Paint textPaint = new Paint();

        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);

        float scaledTextSize = TEXT_SIZE * getResources().getDisplayMetrics().scaledDensity;

        textPaint.setTextSize(scaledTextSize);
        textPaint.setTextAlign(Paint.Align.LEFT);

        Paint fillPaint = new Paint();

        fillPaint.setAntiAlias(true);
        fillPaint.setColor(Color.YELLOW);
        fillPaint.setStyle(Paint.Style.FILL);

        // Calculate scale and text size
        float scale = getResources().getDisplayMetrics().scaledDensity;

        float textHeight = textPaint.descent() - textPaint.ascent();
        float textOffset = (textHeight / 2) - textPaint.descent();

        //textHeight *= scale;
        textOffset *= scale;

        Typeface normalTypeface = fillPaint.getTypeface();

        Typeface boldTypeface = Typeface.create(normalTypeface, Typeface.BOLD);

        String text;
        float x, y;

        // Draw <Player> name

        textPaint.setTextAlign(Paint.Align.CENTER);

        text = "["+mPlayerName+"]";

        x = borderRect.width()/2;
        y = borderRect.bottom-2-textHeight+textOffset;

        if (mTurnPlayer == Game.PlayerPos.PLAYER) {

            textPaint.setColor(Color.YELLOW);
            textPaint.setTypeface(boldTypeface);
        }
        else {
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(normalTypeface);
        }

        canvas.drawText(text, x, y, textPaint);

        // Draw <Partner> name

        textPaint.setTextAlign(Paint.Align.RIGHT);

        text = "["+mPartnerName+"]";

        x = borderRect.width()/3;
        y = borderRect.top+2+textOffset;

        if (mTurnPlayer == Game.PlayerPos.PARTNER) {

            textPaint.setColor(Color.YELLOW);
            textPaint.setTypeface(boldTypeface);
        }
        else {
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(normalTypeface);
        }

        canvas.drawText(text, x, y, textPaint);

        // Draw left opponent name

        textPaint.setTextAlign(Paint.Align.LEFT);

        text = "["+mLeftOpponentName+"]";

        x = borderRect.left+2;
        y = borderRect.bottom-2-textHeight+textOffset;

        if (mTurnPlayer == Game.PlayerPos.LEFT_OPPONENT) {

            textPaint.setColor(Color.YELLOW);
            textPaint.setTypeface(boldTypeface);
        }
        else {
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(normalTypeface);
        }

        canvas.drawText(text, x, y, textPaint);

        // Draw right opponent name

        textPaint.setTextAlign(Paint.Align.RIGHT);

        text = "["+mRightOpponentName+"]";

        x = borderRect.right-4;
        y = borderRect.bottom-2-textHeight+textOffset;

        if (mTurnPlayer == Game.PlayerPos.RIGHT_OPPONENT) {

            textPaint.setColor(Color.YELLOW);
            textPaint.setTypeface(boldTypeface);
        }
        else {
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(normalTypeface);
        }

        canvas.drawText(text, x, y, textPaint);

    }

    private void drawPlayerTiles(Canvas canvas, RectF borderRect, float tileSize) {

        Paint fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.DKGRAY);

        Paint strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);

        float tileLength = tileSize;
        float tileWidth = tileSize/2;
        float gap = tileSize/4;

        // Draw partner tiles

        float offsetX = borderRect.centerX()-
                (mPartnerTileCount*tileWidth+(mPartnerTileCount-1)*gap)/2;

        float offsetY=gap;

        for(int i=0; i<mPartnerTileCount; i++) {

            float left = offsetX+i*(tileWidth+gap);
            float right = left+tileWidth;
            float top = gap;
            float bottom = gap+tileLength;

            canvas.drawRect(left, top, right, bottom, fillPaint);

            canvas.drawRect(left, top, right, bottom, strokePaint);
        }

        // Draw left opponent tiles

        offsetX = gap;

        offsetY = borderRect.centerY()-
                (mLeftOpponentTileCount*tileWidth+(mLeftOpponentTileCount-1)*gap)/2;

        for(int i=0; i<mLeftOpponentTileCount; i++) {

            float left = gap;
            float right = left+tileLength;
            float top = offsetY+i*(tileWidth+gap);
            float bottom = top+tileWidth;

            canvas.drawRect(left, top, right, bottom, fillPaint);

            canvas.drawRect(left, top, right, bottom, strokePaint);
        }

        // Draw right opponent tiles

        offsetX = borderRect.right-gap-tileLength;

        offsetY = borderRect.centerY()-
                (mRightOpponentTileCount*tileWidth+(mRightOpponentTileCount-1)*gap)/2;

        for(int i=0; i<mRightOpponentTileCount; i++) {

            float left = offsetX;
            float right = left+tileLength;
            float top = offsetY+i*(tileWidth+gap);
            float bottom = top+tileWidth;

            canvas.drawRect(left, top, right, bottom, fillPaint);

            canvas.drawRect(left, top, right, bottom, strokePaint);
        }
    }

    public void setTurnPlayer(Game.PlayerPos turnPlayer) {

        mTurnPlayer = turnPlayer;
    }

    public void setPlayerName(String playerName) {

        mPlayerName = playerName;
    }

    public void setPartnerName(String partnerName) {

        mPartnerName = partnerName;
    }

    public void setLeftOpponentName(String leftOpponentName) {

        mLeftOpponentName = leftOpponentName;
    }

    public void setRightOpponentName(String rightOpponentName) {

        mRightOpponentName = rightOpponentName;
    }

    public void setPartnerTileCount(int partnerTileCount) {

        mPartnerTileCount = partnerTileCount;
    }

    public void setLeftOpponentTileCount(int leftOpponentTileCount) {

        mLeftOpponentTileCount = leftOpponentTileCount;
    }

    public void setRightOpponentTileCount(int rightOpponentTileCount) {

        mRightOpponentTileCount = rightOpponentTileCount;
    }

    public void setBoardTiles1(ArrayList<DominoTile> boardTiles1) {

        mBoardTiles1 = boardTiles1;
    }

    public void setBoardTiles2(ArrayList<DominoTile> boardTiles2) {

        mBoardTiles2 = boardTiles2;
    }

    public void onTileSelected(DominoTile selectedTile, boolean forceDouble6Tile) {

        if (mBoardTiles1 == null)
            return;

        if (mBoardTiles2 == null)
            return;

        mHighlightNextBoardTile1 = false;
        mHighlightNextBoardTile2 = false;

        if (mBoardTiles1.size() == 0) {

            if (forceDouble6Tile) {

                if ((selectedTile.mNumber1 == 6) && (selectedTile.mNumber2 == 6)) {

                    // Only the Double 6 tile can be placed in the first position
                    mHighlightNextBoardTile1 = true;
                }

            }
            else {

                // Any tile can be placed in the first position
                mHighlightNextBoardTile1 = true;
            }
        }
        else {

            // Check if we can highlight next tile of board 1






            // Check if we can highlight next tile of board 2


        }
    }

    private void drawBoardTiles1(Canvas canvas, RectF borderRect, float tileLength) {

        float offsetX = borderRect.centerX();
        float offsetY = borderRect.centerY();

        float left, right, top, bottom;

        for(int i=0; i<mBoardTiles1.size(); i++) {

            DominoTile tile = mBoardTiles1.get(i);

            if (tile.isDouble()) {

                left=offsetX-tileLength/4;
                right=offsetX+tileLength/4;
                top=offsetY-tileLength/2;
                bottom = offsetY+tileLength/2;

                offsetX += tileLength/2;
            }
            else {

                left=offsetX-tileLength/2;
                right=offsetX+tileLength/2;
                top=offsetY-tileLength/4;
                bottom=offsetY+tileLength/4;

                offsetX += tileLength;
            }

            RectF tileRect = new RectF(left, top, right, bottom);

            tile.drawTile(canvas, tileRect, Color.WHITE);
        }

        left=offsetX-tileLength/2;
        right=offsetX+tileLength/2;
        top=offsetY-tileLength/4;
        bottom=offsetY+tileLength/4;

        mNextBoardTile1Rect = new RectF(left, top, right, bottom);
    }

    private void drawBoardTiles2(Canvas canvas, RectF borderRect, float tileLength) {


    }

    private void drawNextBoardTiles(Canvas canvas) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.YELLOW);

        if (mHighlightNextBoardTile1) {

            if (mNextBoardTile1Rect != null) {

                canvas.drawRect(mNextBoardTile1Rect, paint);
            }
        }
    }
}
