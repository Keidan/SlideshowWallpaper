package fr.ralala.slideshowwallpaper.sql;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Sql image using the room persistence library.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
@Entity(tableName = "images")
public class Image {
  @PrimaryKey
  @ColumnInfo(name = "id")
  private @NonNull String mId;

  @ColumnInfo(name = "name")
  private String mName;

  @ColumnInfo(name = "scrollable")
  private boolean mScrollable;

  @ColumnInfo(name = "data")
  private byte[] mData;

  @ColumnInfo(name = "x")
  private int mX;

  @ColumnInfo(name = "y")
  private int mY;

  @ColumnInfo(name = "width")
  private int mWidth;

  @ColumnInfo(name = "height")
  private int mHeight;

  @Ignore
  private Bitmap mBitmap;

  @Ignore
  public Image(String name, boolean scrollable, byte[] data, Bitmap bm, int x, int y, int width, int height) {
    mId = UUID.randomUUID().toString();
    initialize(name, scrollable, null, x, y, width, height);
    mData = data;
    mBitmap = bm;
  }

  public Image(@NonNull String id, String name, boolean scrollable, byte[] data, int x, int y, int width, int height) {
    mId = id;
    initialize(name, scrollable, data, x, y, width, height);
  }

  public static byte[] bitmapToArray(Bitmap bitmap, Bitmap.CompressFormat format) {
    ByteArrayOutputStream blob = new ByteArrayOutputStream();
    bitmap.compress(format, 100 /* Ignored for PNGs */, blob);
    return blob.toByteArray();
  }

  private void initialize(String name, boolean scrollable, byte[] data, int x, int y, int width, int height) {
    mName = name;
    mScrollable = scrollable;
    mData = data;
    mX = x;
    mY = y;
    mWidth = width;
    mHeight = height;
    if(data != null)
      mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
  }

  public Bitmap getBitmap() {
    return mBitmap;
  }

  /**
   * Returns the database ID.
   * @return String
   */
  public String getId() {
    return mId;
  }

  /**
   * Returns the image name.
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Returns the scroll state.
   * @return boolean
   */
  public boolean isScrollable() {
    return mScrollable;
  }

  /**
   * Sets the scroll state.
   * @param scrollable The state.
   */
  public void setScrollable(boolean scrollable) {
    mScrollable = scrollable;
  }

  /**
   * Returns the image data.
   * @return byte[]
   */
  public byte[] getData() {
    return mData;
  }

  /**
   * Returns the image X position.
   * @return int
   */
  public int getX() {
    return mX;
  }

  /**
   * Returns the image Y position.
   * @return int
   */
  public int getY() {
    return mY;
  }

  /**
   * Returns the image width.
   * @return int
   */
  public int getWidth() {
    return mWidth;
  }

  /**
   * Returns the image height.
   * @return int
   */
  public int getHeight() {
    return mHeight;
  }


  /**
   * Returns the image bounds.
   * @param x The new value.
   * @param y The new value.
   * @param width The new value.
   * @param height The new value.
   */
  public void setBounds(int x, int y, int width, int height) {
    mX = x;
    mY = y;
    mWidth = width;
    mHeight = height;
  }
}
