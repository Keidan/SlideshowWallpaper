package fr.ralala.slideshowwallpaper.sql;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
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

  @ColumnInfo(name = "path")
  private String mPath;

  @ColumnInfo(name = "scrollable")
  private boolean mScrollable;

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
  private File mPathFile;

  @Ignore
  public Image(String path, boolean scrollable, Bitmap bm, int x, int y, int width, int height) {
    mId = UUID.randomUUID().toString();
    initialize(path, scrollable, x, y, width, height);
    mBitmap = bm;
  }

  public Image(@NonNull String id, String path, boolean scrollable, int x, int y, int width, int height) {
    mId = id;
    initialize(path, scrollable, x, y, width, height);
    mBitmap = BitmapFactory.decodeFile(path);
  }

  private void initialize(String path, boolean scrollable, int x, int y, int width, int height) {
    mPath = path;
    mScrollable = scrollable;
    mX = x;
    mY = y;
    mWidth = width;
    mHeight = height;
    mPathFile = new File(mPath);
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
   * Returns the image path file.
   * @return File
   */
  public File getFile() {
    return mPathFile;
  }

  /**
   * Returns the image path.
   * @return String
   */
  public String getPath() {
    return mPath;
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
