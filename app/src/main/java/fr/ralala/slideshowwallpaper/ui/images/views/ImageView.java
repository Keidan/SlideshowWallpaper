package fr.ralala.slideshowwallpaper.ui.images.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import fr.ralala.slideshowwallpaper.sql.Image;
import fr.ralala.slideshowwallpaper.utils.Helper;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Image view.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ImageView extends View {
  private boolean USE_CORNERS_NO = false;
  private static final int STROKE_WIDTH = 5;
  private Paint mPaintRectangle = new Paint();
  private Paint mPaintCorner = new Paint();
  private Rectangle mRectangle = new Rectangle(-1, -1, 100, 100);
  private Rectangle mRealRectangle = new Rectangle(0, 0, 0, 0);
  private Image mImage = null;
  private Point mScreen;
  private CornerBall mCornerTopStart;
  private CornerBall mCornerTopEnd;
  private CornerBall mCornerBottomStart;
  private CornerBall mCornerBottomEnd;
  private int mPreviousX;
  private int mPreviousY;
  private Bitmap mScaled;
  private double diffWidth;
  private double diffHeight;
  private boolean mUseCorners = USE_CORNERS_NO;

  public ImageView(Context context) {
    super(context);
    initialize();
  }

  public ImageView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public ImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  /**
   * Initializes the objects.
   */
  private void initialize() {
    mPaintRectangle.setAntiAlias(true);
    mPaintRectangle.setDither(true);
    mPaintRectangle.setStrokeWidth(STROKE_WIDTH);
    mPaintRectangle.setStyle(Paint.Style.STROKE);
    mPaintRectangle.setStrokeJoin(Paint.Join.ROUND);
    mPaintCorner.setAntiAlias(true);
    mPaintCorner.setDither(true);
    mPaintCorner.setStrokeWidth(STROKE_WIDTH);
    mPaintCorner.setStyle(Paint.Style.STROKE);
    mPaintCorner.setStrokeJoin(Paint.Join.ROUND);

    final Context context = getContext();
    mCornerTopStart = new CornerBall(context, new Point(0, 0));
    mCornerTopEnd = new CornerBall(context, new Point(0, 0));
    mCornerBottomStart = new CornerBall(context, new Point(0, 0));
    mCornerBottomEnd = new CornerBall(context, new Point(0, 0));

    setFocusable(true);
    mScreen = Helper.getRealScreenDimension(getContext());
  }

  /**
   * Sets the rectangle color.
   * @param color The rectangle color.
   */
  public void setRectangleColor(int color) {
    mPaintRectangle.setColor(color);
  }

  /**
   * Sets the corner color.
   * @param color The corner color.
   */
  public void setCornerColor(int color) {
    mPaintCorner.setColor(color);
  }

  @Override
  public boolean performClick() {
    super.performClick();
    return true;
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if(!mImage.isScrollable() || mUseCorners == USE_CORNERS_NO) {
      return super.onTouchEvent(event);
    }
    final int x = (int) event.getRawX();
    final int y = (int) event.getRawY();

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        mPreviousX = x - mRectangle.x;
        mPreviousY = y - mRectangle.y;
        break;
      case MotionEvent.ACTION_MOVE:
        mRectangle.x = x - mPreviousX;
        mRectangle.y = y - mPreviousY;
        invalidate();
        break;
      default:
        performClick();
        super.onTouchEvent(event);
        break;
    }

    return true;
  }


  /**
   * Sets the image to display.
   * @param image The new image.
   */
  public void setImage(Image image) {
    Bitmap bm = image.getBitmap();
    mImage = image;
    if(image.isScrollable() && mUseCorners != USE_CORNERS_NO) {
      Point scrolled = Helper.getScrolledDimension(getContext(), bm, false);
      Log.e("TAG", "Screen: " + mScreen);
      Log.e("TAG", "scrolled: " + scrolled);
      Log.e("TAG", "bm.getWidth(): " + bm.getWidth() + ", bm.getHeight(): "+bm.getHeight());



      diffWidth = diffWidth(scrolled, bm.getWidth());
      diffHeight = diffHeight(scrolled, bm.getHeight());
      Log.e("TAG", "diffWidth: " + diffWidth);
      Log.e("TAG", "diffHeight: " + diffHeight);
      mRectangle.w = (int) ((double) scrolled.x - (((double) scrolled.x * diffWidth) / (double) 100));
      mRectangle.h = (int) ((double) scrolled.y - (((double) scrolled.y * diffHeight) / (double) 100));
      Log.e("TAG", "Rectangle: " + mRectangle);
      if (mImage.getWidth() != 0) {
        mRealRectangle.w = mImage.getWidth();
        mRectangle.x = mImage.getX();
      } else {
        mRealRectangle.w = scrolled.x;
        mRectangle.x = (mScreen.x / 2) - (mRectangle.w / 2);
      }
      if (mImage.getHeight() != 0) {
        mRealRectangle.h = mImage.getHeight();
        mRectangle.y = mImage.getY();
      } else {
        mRealRectangle.h = scrolled.y;
        mRectangle.y = (mScreen.y / 2) - (mRectangle.h / 2);
      }
    }
    mScaled = Bitmap.createScaledBitmap(mImage.getBitmap(), mScreen.x, mScreen.y, true);
    invalidate();
  }

  /**
   * Returns the difference in width.
   * @param ref Reference dimension.
   * @param width The source width.
   * @return double
   */
  private double diffWidth(Point ref, int width) {
    if(width < ref.x)
      return ((double)ref.x - (double)width) / (((double)ref.x + (double)width) / 2) * 100;
    else
      return ((double)width - (double)ref.x) / (((double)ref.x + (double)width) / 2) * 100;
  }

  /**
   * Returns the difference in height.
   * @param ref Reference dimension.
   * @param height The source height.
   * @return double
   */
  private double diffHeight(Point ref, int height) {
    if(height < ref.y)
      return ((double)ref.y - (double)height) / (((double)ref.y + (double)height) / 2) * 100;
    else
      return ((double)height - (double)ref.y) / (((double)ref.y + (double)height) / 2) * 100;
  }

  /**
   * Updates rectangles
   */
  private void updateBounds() {
    if(mRectangle.x < 0) mRectangle.x = 0;
    else if(mRectangle.x + mRectangle.w > mScreen.x) mRectangle.x = mScreen.x - mRectangle.w;
    if(mRectangle.y < 0) mRectangle.y = 0;
    else if(mRectangle.y + mRectangle.h > mScreen.y) mRectangle.y = mScreen.y - mRectangle.h;

    mCornerTopStart.set(mRectangle.x, mRectangle.y);
    mCornerTopEnd.set(mRectangle.x + mRectangle.w, mRectangle.y);
    mCornerBottomStart.set(mRectangle.x, mRectangle.y + mRectangle.h);
    mCornerBottomEnd.set(mRectangle.x + mRectangle.w, mRectangle.y + mRectangle.h);
  }

  /**
   * Returns the real rectangle.
   * @return Rectangle
   */
  public Rectangle getRealRectangle() {
    Log.e("TAG", "Before Rectangle: " + mRectangle);
    if(mImage.isScrollable()) {
      if(mUseCorners == USE_CORNERS_NO) {
        mRectangle.w = mRectangle.h = 0;
      } else {
        /* rescale XY */
        mRectangle.x = (int) ((double) mRectangle.x + (((double) mRectangle.x * diffWidth) / (double) 100));
        mRectangle.y = (int) ((double) mRectangle.y + (((double) mRectangle.y * diffHeight) / (double) 100));
        mRealRectangle.setXY(mRectangle.x, mRectangle.y);
      }
    } else {
      mRectangle.x = mRectangle.y = 0;
      mRectangle.w = mScreen.x;
      mRectangle.h = mScreen.y;
    }
    Log.e("TAG", "After Rectangle: " + mRectangle);
    return mRealRectangle;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if(mImage == null)
      return;
    /* Draw the background image */
    canvas.drawBitmap(mScaled, 0, 0, mPaintRectangle);
    if(!mImage.isScrollable() || mUseCorners == USE_CORNERS_NO) return;
    /* Display of the selection rectangle for the view to be displayed in scroll mode */
    updateBounds();
    /* Draw selection view */
    canvas.drawRect(mRectangle.x, mRectangle.y, mRectangle.x + mRectangle.w, mRectangle.y + mRectangle.h, mPaintRectangle);
    /* Draw the corners in the shape of a ball*/
    canvas.drawBitmap(mCornerTopStart.getBitmap(), mCornerTopStart.getX(), mCornerTopStart.getY(), mPaintCorner);
    canvas.drawBitmap(mCornerTopEnd.getBitmap(), mCornerTopEnd.getX(), mCornerTopEnd.getY(), mPaintCorner);
    canvas.drawBitmap(mCornerBottomStart.getBitmap(), mCornerBottomStart.getX(), mCornerBottomStart.getY(), mPaintCorner);
    canvas.drawBitmap(mCornerBottomEnd.getBitmap(), mCornerBottomEnd.getX(), mCornerBottomEnd.getY(), mPaintCorner);
  }

}
