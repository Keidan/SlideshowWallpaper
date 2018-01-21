package fr.ralala.slideshowwallpaper.ui.images.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import fr.ralala.slideshowwallpaper.R;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Corner ball.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class CornerBall {
  private Bitmap mBitmap;
  private Point mPoint;
  private int mId;
  private static int mCount = 0;

  public CornerBall(Context context, Point point) {
    mId = mCount++;
    mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.gray_circle);
    mPoint = point;
  }

  /**
   * Returns the ball width.
   * @return int
   */
  public int getWidthOfBall() {
    return mBitmap.getWidth();
  }

  /**
   * Returns the ball height.
   * @return int
   */
  public int getHeightOfBall() {
    return mBitmap.getHeight();
  }

  /**
   * Returns the Bitmap.
   * @return Bitmap
   */
  Bitmap getBitmap() {
    return mBitmap;
  }

  /**
   * Returns the id.
   * @return int
   */
  public int getID() {
    return mId;
  }

  /**
   * Returns the X positions.
   * @return int
   */
  int getX() {
    return mPoint.x;
  }

  /**
   * Returns the Y positions.
   * @return int
   */
  int getY() {
    return mPoint.y;
  }

  /**
   * Sets the XY positions.
   * @param x The new value.
   * @param y The new value.
   */
  void set(int x, int y) {
    mPoint.y = y - getHeightOfBall() / 2;
    mPoint.x = x - getWidthOfBall() / 2;
  }
}
