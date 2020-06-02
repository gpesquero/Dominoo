package org.dominoo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class GameBoardView extends View {

    private static final int BORDER_MARGIN = 5;

    private static final int TEXT_SIZE = 20;

    private static final float TILE_SIZE_SCALE_FACTOR = 7;

    private static final float TILE_LIMITS_HOR_SCALE = 3;
    //private static final float TILE_LIMITS_HOR_SCALE = 10;
    private static final float TILE_LIMITS_VER_SCALE = 8;

    private static final float EXIT_BUTTON_MARGIN = 5;
    private static final float EXIT_BUTTON_SIZE = 100;

    private Game.PlayerPos mTurnPlayer = Game.PlayerPos.NONE;
    private Game.PlayerPos mHandPlayer = Game.PlayerPos.NONE;

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

    private RectF mTileLimits = null;

    private boolean mDrawTestLines = false;

    public boolean mDrawExitButton = false;

    private RectF mExitButtonRect = null;

    public boolean mSilentModeOn = false;

    private RectF mSilentModeRect = null;

    private float mOffsetY = 0;

    OnGameBoardViewListener mListener = null;

    DominoTile mSelectedTile = null;

    DominoTile mDummyTile = new DominoTile(1 ,2);

    enum Dir {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public interface OnGameBoardViewListener {

        void onTilePlayed(DominoTile selectedTile, int boardSide);
        void onExitButtonClicked();
        void onSilentModeClicked();
    }

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

        drawExitButton(canvas);

        drawSilentMode(canvas, borderRect);

        drawPlayerNames(canvas, borderRect);

        float tileSize = calculateTileSize(borderRect);

        calculateTileLimits(canvas, borderRect, tileSize);

        drawPlayerTiles(canvas, borderRect, tileSize);

        drawBoardTiles1(canvas, borderRect, tileSize);

        drawBoardTiles2(canvas, borderRect, tileSize);

        drawNextBoardTiles(canvas);
    }

    private float calculateTileSize(RectF borderRect) {

        return borderRect.height()/TILE_SIZE_SCALE_FACTOR;
    }

    private void calculateTileLimits(Canvas canvas, RectF borderRect, float tileLength) {

        mOffsetY = tileLength/4;

        mTileLimits = new RectF();

        float horLimit = borderRect.width()/TILE_LIMITS_HOR_SCALE;

        mTileLimits.left = borderRect.centerX()-horLimit;

        mTileLimits.right = borderRect.centerX()+horLimit;

        float verLimit = borderRect.height()/TILE_LIMITS_VER_SCALE;

        mTileLimits.top = borderRect.centerY()+mOffsetY-verLimit;
        mTileLimits.bottom = borderRect.centerY()+mOffsetY+verLimit;

        if (mDrawTestLines) {

            // Draw limit lines

            Paint paint = new Paint();

            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(3);
            paint.setColor(Color.YELLOW);

            // Draw left tile limit line
            canvas.drawLine(mTileLimits.left, 0, mTileLimits.left, borderRect.bottom, paint);

            // Draw right tile limit line
            canvas.drawLine(mTileLimits.right, 0, mTileLimits.right, borderRect.bottom, paint);

            // Draw bottom tile limit line
            canvas.drawLine(0, mTileLimits.bottom, borderRect.right, mTileLimits.bottom, paint);

            // Draw top tile limit line
            canvas.drawLine(0, mTileLimits.top, borderRect.right, mTileLimits.top, paint);
        }
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

        if (mDrawTestLines) {

            // Draw reference lines

            paint.setColor(Color.YELLOW);

            // Draw horizontal center line
            canvas.drawLine(borderRect.left, borderRect.centerY(),
                    borderRect.right, borderRect.centerY(), paint);

            // Draw vertical center line
            canvas.drawLine(borderRect.centerX(), borderRect.top,
                    borderRect.centerX(), borderRect.bottom, paint);
        }

        return borderRect;
    }

    private void drawExitButton(Canvas canvas) {

        if (!mDrawExitButton) {

            return;
        }

        mExitButtonRect = new RectF(EXIT_BUTTON_MARGIN, EXIT_BUTTON_MARGIN,
                                    EXIT_BUTTON_SIZE, EXIT_BUTTON_SIZE);

        Paint paint=new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);

        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res,
                android.R.drawable.ic_menu_close_clear_cancel);

        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        canvas.drawBitmap(bitmap, srcRect, mExitButtonRect, paint);
    }

    private void drawSilentMode(Canvas canvas, RectF borderRect) {

        float left = borderRect.right-EXIT_BUTTON_MARGIN-EXIT_BUTTON_SIZE;
        float right = left+EXIT_BUTTON_SIZE;
        float top = EXIT_BUTTON_MARGIN;
        float bottom = top+EXIT_BUTTON_SIZE;

        mSilentModeRect = new RectF(left, top, right, bottom);

        Paint paint=new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);

        int drawableId;

        if (mSilentModeOn) {

            drawableId = android.R.drawable.ic_lock_silent_mode;
        }
        else {

            drawableId = android.R.drawable.ic_lock_silent_mode_off;
        }

        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, drawableId);

        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        canvas.drawBitmap(bitmap, srcRect, mSilentModeRect, paint);
    }

    private void drawPlayerNames(Canvas canvas, RectF borderRect) {

        String handString = new String("\uD83D\uDC4B");

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

        if (mHandPlayer == Game.PlayerPos.PLAYER) {

            text += handString;
        }

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

        if (mHandPlayer == Game.PlayerPos.PARTNER) {

            text += handString;
        }

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

        if (mHandPlayer == Game.PlayerPos.LEFT_OPPONENT) {

            text += handString;
        }

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

        if (mHandPlayer == Game.PlayerPos.RIGHT_OPPONENT) {

            text += handString;
        }

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

    public void setHandPlayer(Game.PlayerPos handPlayer) {

        mHandPlayer = handPlayer;
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

        mSelectedTile = selectedTile;

        if (mBoardTiles1 == null)
            return;

        if (mBoardTiles2 == null)
            return;

        mHighlightNextBoardTile1 = false;
        mHighlightNextBoardTile2 = false;

        if (mBoardTiles1.size() == 0) {

            if (forceDouble6Tile) {

                if ((mSelectedTile.mNumber1 == 6) && (mSelectedTile.mNumber2 == 6)) {

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

            DominoTile endTile1 = mBoardTiles1.get(mBoardTiles1.size()-1);

            int endNumber1 = endTile1.mNumber2;

            if (mSelectedTile.contains(endNumber1)) {

                mHighlightNextBoardTile1 = true;
            }

            // Check if we can highlight next tile of board 2

            int endNumber2;

            if (mBoardTiles2.size() == 0) {

                DominoTile beginTile1 = mBoardTiles1.get(0);

                endNumber2 = beginTile1.mNumber1;
            }
            else {

                DominoTile endTile2 = mBoardTiles2.get(mBoardTiles2.size()-1);

                endNumber2 = endTile2.mNumber2;
            }

            if (mSelectedTile.contains(endNumber2)) {

                mHighlightNextBoardTile2 = true;
            }
        }
    }

    private void drawBoardTiles1(Canvas canvas, RectF borderRect, float tileLength) {

        /*
        mBoardTiles1.clear();
        mBoardTiles1.add(new DominoTile(6, 6));
        mBoardTiles1.add(new DominoTile(6, 5));
        mBoardTiles1.add(new DominoTile(6, 4));
        mBoardTiles1.add(new DominoTile(6, 3));
        mBoardTiles1.add(new DominoTile(6, 2));
        mBoardTiles1.add(new DominoTile(6, 1));
        mBoardTiles1.add(new DominoTile(6, 0));

        mBoardTiles1.add(new DominoTile(5, 5));
        mBoardTiles1.add(new DominoTile(5, 4));
        mBoardTiles1.add(new DominoTile(5, 3));
        mBoardTiles1.add(new DominoTile(5, 2));
        mBoardTiles1.add(new DominoTile(5, 1));
        mBoardTiles1.add(new DominoTile(5, 0));

        mBoardTiles1.add(new DominoTile(4, 4));
        mBoardTiles1.add(new DominoTile(4, 3));
        mBoardTiles1.add(new DominoTile(4, 2));
        mBoardTiles1.add(new DominoTile(4, 1));
        mBoardTiles1.add(new DominoTile(4, 0));

        mBoardTiles1.add(new DominoTile(3, 3));
        mBoardTiles1.add(new DominoTile(3, 2));
        mBoardTiles1.add(new DominoTile(3, 1));
        mBoardTiles1.add(new DominoTile(3, 0));

        mBoardTiles1.add(new DominoTile(2, 2));
        mBoardTiles1.add(new DominoTile(2, 1));
        mBoardTiles1.add(new DominoTile(2, 0));

        mBoardTiles1.add(new DominoTile(1, 1));
        mBoardTiles1.add(new DominoTile(1, 0));

        mBoardTiles1.add(new DominoTile(0, 0));
        */

        // Starting direction is to the right...
        Dir dir = Dir.RIGHT;

        // Initial position is border center...
        PointF pos = new PointF(borderRect.centerX(), borderRect.centerY());

        // Shift starting position...
        pos.x -= (tileLength/2);
        pos.y += mOffsetY;

        if (mBoardTiles1.size() < 1) {

            mNextBoardTile1Rect = calculateTileRect(mDummyTile, pos, dir, tileLength);

            return;
        }

        // Draw first tile in the center of the board

        DominoTile tile = mBoardTiles1.get(0);

        if (tile.isDouble()) {

            // Center tile
            pos.x += (tileLength/4);
        }

        RectF rect = calculateTileRect(tile, pos, dir, tileLength);

        // Draw first tile...
        tile.drawTile(canvas, rect, Color.WHITE, false);

        // Store previous tile
        DominoTile prevTile = tile;

        // Draw the rest of tiles...

        for(int i=1; i<mBoardTiles1.size(); i++) {

            tile = mBoardTiles1.get(i);

            // Check if we have to change direction...

            if (tile.isDouble()) {

                // Do not change direction if tile is double
            }
            else if ((dir == Dir.RIGHT) && (pos.x > mTileLimits.right)) {

                // We have reached the right limit
                // Change to down direction

                if (prevTile.isDouble()) {

                    pos.x -= (tileLength/4);
                    pos.y += (tileLength/2);
                }
                else {

                    pos.x -= (tileLength / 4);
                    pos.y += (tileLength / 4);
                }

                dir = Dir.DOWN;
            }
            else if ((dir == Dir.DOWN) && (pos.y > mTileLimits.bottom)) {

                // We have reached the bottom limit
                // Change to left direction

                if (prevTile.isDouble()) {

                    pos.x -= (tileLength/2);
                    pos.y -= (tileLength/4);
                }
                else {

                    pos.x += (tileLength/4);
                    pos.y += (tileLength/4);
                }

                dir = Dir.LEFT;
            }

            // Update the tile position
            rect = calculateTileRect(tile, pos, dir, tileLength);

            boolean swapNumbers = false;

            if (dir == Dir.LEFT) {

                swapNumbers = true;
            }

            tile.drawTile(canvas, rect, Color.WHITE, swapNumbers);

            prevTile = tile;
        }

        mNextBoardTile1Rect = calculateTileRect(mDummyTile, pos, dir, tileLength);

        // Check if next board tile 1 rect is out of bounds...
        if (mNextBoardTile1Rect.left < 0) {

            float xOffset = -mNextBoardTile1Rect.left;

            mNextBoardTile1Rect.left += xOffset;
            mNextBoardTile1Rect.right += xOffset;
        }
    }

    private void drawBoardTiles2(Canvas canvas, RectF borderRect, float tileLength) {

        // Starting direction is to the left...
        Dir dir = Dir.LEFT;

        // Initial position is border center...
        PointF pos = new PointF(borderRect.centerX(), borderRect.centerY());

        if (mBoardTiles1.size() > 0) {

            // Shift starting position...

            if (mBoardTiles1.get(0).isDouble()) {

                pos.x -= (tileLength/4);
            }
            else {

                pos.x -= (tileLength/2);
            }

            pos.y += mOffsetY;
        }

        // Simulate any custom tile...
        DominoTile tile = new DominoTile(0, 1);

        if (mBoardTiles2.size() < 1) {

            mNextBoardTile2Rect = calculateTileRect(tile, pos, dir, tileLength);

            return;
        }

        RectF rect;

        DominoTile prevTile = tile;

        for(int i=0; i<mBoardTiles2.size(); i++) {

            tile = mBoardTiles2.get(i);

            // Check if we have to change direction...

            if (tile.isDouble()) {

                // Do not change direction if tile is double
            }
            else if ((dir == Dir.LEFT) && (pos.x < mTileLimits.left)) {

                // We have reached the left limit
                // Change to up direction

                if (prevTile.isDouble()) {

                    pos.x += (tileLength / 4);
                    pos.y -= (tileLength / 2);
                }
                else {

                    pos.x += (tileLength / 4);
                    pos.y -= (tileLength / 4);
                }

                dir = Dir.UP;
            }
            else if ((dir == Dir.UP) && (pos.y < mTileLimits.top)) {

                // We have reached the top limit
                // Change to right direction

                if (prevTile.isDouble()) {

                    pos.x += (tileLength / 2);
                    pos.y += (tileLength / 4);
                }
                else {

                    pos.x -= (tileLength / 4);
                    pos.y -= (tileLength / 4);
                }

                dir = Dir.RIGHT;
            }

            // Update the tile position
            rect = calculateTileRect(tile, pos, dir, tileLength);

            boolean swapNumbers = false;

            if ((dir == Dir.LEFT) || (dir == Dir.UP)) {

                swapNumbers = true;
            }

            tile.drawTile(canvas, rect, Color.WHITE, swapNumbers);

            prevTile = tile;
        }

        mNextBoardTile2Rect = calculateTileRect(mDummyTile, pos, dir, tileLength);

        // Check if next board tile 2 rect is out of bounds...
        if (mNextBoardTile2Rect.right > borderRect.right) {

            float xOffset = borderRect.right-mNextBoardTile2Rect.right;

            mNextBoardTile2Rect.left += xOffset;
            mNextBoardTile2Rect.right += xOffset;
        }
    }

    private RectF calculateTileRect(DominoTile tile, PointF pos, Dir dir, float tileLength) {

        RectF rect = new RectF();

        if (tile.isDouble()) {

            switch (dir) {

                case LEFT:

                    rect.left = pos.x - tileLength/2;
                    rect.right = pos.x;
                    rect.top = pos.y - tileLength/2;
                    rect.bottom = pos.y + tileLength/2;

                    pos.x -= (tileLength/2);

                    break;

                case RIGHT:

                    rect.left = pos.x;
                    rect.right = pos.x + tileLength/2;
                    rect.top = pos.y - tileLength/2;
                    rect.bottom = pos.y + tileLength/2;

                    pos.x += (tileLength/2);

                    break;

                case UP:

                    rect.left = pos.x - tileLength/2;
                    rect.right = pos.x + tileLength/2;
                    rect.top = pos.y  - tileLength/2;
                    rect.bottom = pos.y;

                    pos.y -= (tileLength/2);

                    break;

                case DOWN:

                    rect.left = pos.x - tileLength/2;
                    rect.right = pos.x + tileLength/2;
                    rect.top = pos.y;
                    rect.bottom = pos.y + tileLength/2;

                    pos.y += (tileLength/2);

                    break;

                default:
                    break;
            }
        }
        else {

            switch (dir) {

                case LEFT:

                    rect.left = pos.x - tileLength;
                    rect.right = pos.x;
                    rect.top = pos.y - tileLength/4;
                    rect.bottom = pos.y + tileLength/4;

                    pos.x -= tileLength;

                    break;

                case RIGHT:

                    rect.left = pos.x;
                    rect.right = pos.x + tileLength;
                    rect.top = pos.y - tileLength/4;
                    rect.bottom = pos.y + tileLength/4;

                    pos.x += tileLength;

                    break;

                case UP:

                    rect.left = pos.x - tileLength/4;
                    rect.right = pos.x + tileLength/4;
                    rect.top = pos.y - tileLength;
                    rect.bottom = pos.y;

                    pos.y -= tileLength;

                    break;

                case DOWN:

                    rect.left = pos.x - tileLength/4;
                    rect.right = pos.x + tileLength/4;
                    rect.top = pos.y;
                    rect.bottom = pos.y + tileLength;

                    pos.y += tileLength;

                    break;

                default:
                    break;
            }
        }

        return rect;
    }

    private void drawNextBoardTiles(Canvas canvas) {

        if (mTurnPlayer != Game.PlayerPos.PLAYER) {

            // Draw next board tiles only if it's the turn of the PLAYER

            return;
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.YELLOW);

        if (mHighlightNextBoardTile1) {

            if (mNextBoardTile1Rect != null) {

                canvas.drawRect(mNextBoardTile1Rect, paint);
            }
        }

        if (mHighlightNextBoardTile2) {

            if (mNextBoardTile2Rect != null) {

                canvas.drawRect(mNextBoardTile2Rect, paint);
            }
        }
    }

    public void setGameBoardViewListener(OnGameBoardViewListener listener) {

        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mListener == null) {

            return false;
        }

        float x = event.getX();
        float y = event.getY();

        // Check if we have clicked in the next tile of board 1

        if (mHighlightNextBoardTile1) {

            if (mNextBoardTile1Rect != null) {

                if (mNextBoardTile1Rect.contains(x, y)) {

                    mListener.onTilePlayed(mSelectedTile, 1);

                    return false;
                }
            }
        }

        if (mHighlightNextBoardTile2) {

            if (mNextBoardTile2Rect != null) {

                if (mNextBoardTile2Rect.contains(x, y)) {

                    mListener.onTilePlayed(mSelectedTile, 2);
                }
            }
        }

        if ((mDrawExitButton) && (mExitButtonRect.contains(x, y))) {

            // Clicked on the exit button...
            mListener.onExitButtonClicked();
        }

        if (mSilentModeRect.contains(x, y)) {

            // Clicked on the silent mode icon...
            mListener.onSilentModeClicked();
        }

        return false;
    }

    public void clearHighlights() {

        mHighlightNextBoardTile1 = false;
        mHighlightNextBoardTile2 = false;

        mSelectedTile = null;
    }
}
