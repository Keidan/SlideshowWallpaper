package fr.ralala.slideshowwallpaper.ui.chooser;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.Locale;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * File option
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserOption implements Comparable<FileChooserOption> {
  private String mName;
  private String mData;
  private String mPath;
  private Drawable mIcon;
  private boolean mPreview;

  /**
   * Creates a new chooser option.
   * @param n The option name.
   * @param d The option data.
   * @param p The option path.
   * @param i The option icon.
   * @param preview True for image preview.
   */
  FileChooserOption(final String n, final String d, final String p, final Drawable i, boolean preview) {
    mName = n;
    mData = d;
    mPath = p;
    mIcon = i;
    mPreview = preview;
  }

  /**
   * Returns true if a preview must be used.
   * @return boolean
   */
  boolean isPreview() {
    return mPreview;
  }

  /**
   * Returns the name.
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Returns the data.
   * @return String
   */
  public String getData() {
    return mData;
  }

  /**
   * Returns the path.
   * @return String
   */
  String getPath() {
    return mPath;
  }

  /**
   * Returns the drawable icon.
   * @return Drawable
   */
  public Drawable getIcon() {
    return mIcon;
  }

  /**
   * Compares this instance to an other instance.
   * @param o Instance to compare.
   * @return int
   */
  @SuppressLint("DefaultLocale")
  @Override
  public int compareTo(@NonNull final FileChooserOption o) {
    if (this.mName != null)
      return this.mName.toLowerCase(Locale.getDefault()).compareTo(
          o.getName().toLowerCase());
    else
      throw new IllegalArgumentException();
  }
}
