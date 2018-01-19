package fr.ralala.slideshowwallpaper.services.utils;


/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Service utils
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ServiceUtils {

  private boolean mSenpuku = false;
  private boolean mCreated = false;
  private boolean mStarted = false;

  /**
   * Returns the senpuku status.
   * @return boolean
   */
  public boolean isSenpuku() {
    return mSenpuku;
  }

  /**
   * Sets the senpuku status.
   * @param senpuku The new value.
   */
  public void setSenpuku(boolean senpuku) {
    mSenpuku = senpuku;
  }

  /**
   * Returns the created status.
   * @return boolean
   */
  public boolean isCreated() {
    return mCreated;
  }

  /**
   * Sets the created status.
   * @param created The new value.
   */
  public void setCreated(boolean created) {
    mCreated = created;
  }

  /**
   * Returns the started status.
   * @return boolean
   */
  public boolean isStarted() {
    return mStarted;
  }

  /**
   * Sets the started status.
   * @param started The new value.
   */
  public void setStarted(boolean started) {
    mStarted = started;
  }
}
