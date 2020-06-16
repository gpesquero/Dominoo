package org.dominoo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class PlayerTilesView extends View {

    private static final int BORDER_MARGIN = 5;
    private static final int TILE_MARGIN = 10;

    private ArrayList<DominoTile> mTiles = new ArrayList<DominoTile>();

    private float mOffsetX;
    private float mOffsetY;
    private float mTileWidth;
    private float mTileHeight;

    int mSelectedTile = -1;

    private Game.PlayerPos mTurnPlayerPos = Game.PlayerPos.NONE;

    OnTileSelectedListener mListener = null;

    public interface OnTileSelectedListener {

        void onTileSelected(DominoTile selectedTile);
    }

    public PlayerTilesView(Context context) {
        super(context);

    }

    public PlayerTilesView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public PlayerTilesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void setTiles(ArrayList<DominoTile> tiles) {

        mTiles = tiles;

        mSelectedTile = -1;
    }

    public void setOnTileSelectedListener(OnTileSelectedListener listener) {

        mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF borderRect = drawBorder(canvas);

        mTileHeight = calculateTileSize(borderRect);

        if (mTurnPlayerPos != Game.PlayerPos.PLAYER) {

            // No tile selected if it's not the PLAYER's turn

            mSelectedTile = -1;
        }

        drawTiles(canvas, borderRect, mTileHeight);
    }

    private RectF drawBorder(Canvas canvas) {

        RectF borderRect;

        borderRect=new RectF(BORDER_MARGIN, BORDER_MARGIN,
                getWidth()-BORDER_MARGIN, getHeight()-BORDER_MARGIN);

        Paint paint=new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        canvas.drawRect(borderRect, paint);

        return borderRect;
    }

    private float calculateTileSize(RectF borderRect) {

        float tileHeight = borderRect.height() - 2 * TILE_MARGIN;

        float tileWidth = tileHeight / 2;

        float totalTilesWidth = tileWidth * 7 + 8 * TILE_MARGIN;

        if (totalTilesWidth > borderRect.width()) {

            // We have to calculate tiles height and width based on border width

            tileWidth = (borderRect.width() - 8 * TILE_MARGIN) / 7;

            tileHeight = tileWidth * 2;
        }

        return tileHeight;
    }

    private void drawTiles(Canvas canvas, RectF borderRect, float tileHeight) {

        mTileWidth = tileHeight/2;

        int tileCount = mTiles.size();

        mOffsetX = borderRect.centerX()-(tileCount*mTileWidth+
                (tileCount-1)*TILE_MARGIN)/2;

        mOffsetY = borderRect.centerY()-tileHeight/2;

        for(int tile=0; tile<tileCount; tile++) {

            float left = mOffsetX + tile*(mTileWidth+TILE_MARGIN);
            float top = mOffsetY;
            float right = left+mTileWidth;
            float bottom = mOffsetY+tileHeight;

            RectF tileRect = new RectF(left, top, right, bottom);

            int tileColor = Color.WHITE;

            if (mSelectedTile == tile) {

                tileColor = Color.YELLOW;
            }

            mTiles.get(tile).drawTile(canvas, tileRect, tileColor, false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if (x < mOffsetX) {

            mSelectedTile = -1;

            invalidate();

            return false;
        }

        int tileCount = mTiles.size();

        float rightX = mOffsetX+tileCount*mTileWidth+(tileCount-1)*TILE_MARGIN;

        if (x > rightX) {

            mSelectedTile = -1;

            invalidate();

            return false;
        }

        mSelectedTile = (int)Math.floor((x-mOffsetX)/(rightX-mOffsetX)*(tileCount));

        if (mSelectedTile == tileCount) {

            mSelectedTile = tileCount-1;
        }

        DominoTile clickedTile = mTiles.get(mSelectedTile);

        if (mListener != null) {

            mListener.onTileSelected(clickedTile);
        }

        /*
        Toast toast=Toast.makeText(getContext(), "Clicked tile: "+clickedTile.mNumber1+"-"+
                clickedTile.mNumber2, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0, 0);
        toast.show();
        */

        invalidate();

        return false;
    }

    public void clearSelection() {

        mSelectedTile = -1;
    }

    public void setTurnPlayer(Game.PlayerPos turnPlayerPos) {

        mTurnPlayerPos = turnPlayerPos;
    }
}
