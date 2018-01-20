package fr.ralala.slideshowwallpaper;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import fr.ralala.slideshowwallpaper.services.utils.ServiceUtils;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Main application context.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SlideshowWallpaperApplication extends Application {
  private static final String KEY_FREQUENCY_SCREEN = "frequencyScreen";
  private static final String KEY_SCROLLABLE_WALLPAPER_FROM_FOLDER = "scrollableWallpaperFromFolder";
  private static final String KEY_LOCK_SCREEN_WALLPAPER = "lockScreenWallpaper";
  private static final String KEY_FREQUENCY_VAL = "frequencyValue";
  private static final String KEY_FREQUENCY_UNIT = "frequencyUnit";
  private static final String KEY_FOLDER = "folder";
  private static final String KEY_CURRENT_FILE = "current";
  private static final String KEY_BROWSE_FROM = "browseFrom";
  private static final int DEFAULT_FREQUENCY_VAL = 30;
  private static final int DEFAULT_FREQUENCY_UNIT = 1;
  private static final boolean DEFAULT_FREQUENCY_SCREEN = true;
  private static final boolean DEFAULT_SCROLLABLE_WALLPAPER_FROM_FOLDER = false;
  private static final boolean DEFAULT_LOCK_SCREEN_WALLPAPER = false;
  private static final String DEFAULT_FOLDER = "";
  public static final int DEFAULT_CURRENT_FILE = 0;
  public static final int BROWSE_FROM_FOLDER = 0;
  public static final int BROWSE_FROM_DATABASE = 1;
  private static final int DEFAULT_BROWSE_FROM = BROWSE_FROM_FOLDER;
  private SharedPreferences mPrefs;
  private ServiceUtils mServiceUtils;

  @Override
  public void onCreate() {
    super.onCreate();
    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
  }

  /**
   * Checks the permissions.
   * @param c The Android context.
   * @return boolean
   */
  public static boolean checkPermissions(Context c) {
    return ActivityCompat.checkSelfPermission(c, Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(c, Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Returns the instance of the service utils.
   * @return ServiceUtils
   */
  public ServiceUtils getServiceUtils() {
    if(mServiceUtils == null)
      mServiceUtils = new ServiceUtils();
    return mServiceUtils;
  }
  /**
   * Sets the browse from value.
   * @param browseFrom The new value.
   */
  public void setBrowseFrom(int browseFrom) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putInt(KEY_BROWSE_FROM, browseFrom);
    e.apply();
  }

  /**
   * Returns the browse from value.
   * @return int
   */
  public int getBrowseFrom() {
    return mPrefs.getInt(KEY_BROWSE_FROM, DEFAULT_BROWSE_FROM);
  }

  /**
   * Sets the current file.
   * @param current The new value.
   */
  public void setCurrentFile(int current) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putInt(KEY_CURRENT_FILE, current);
    e.apply();
  }

  /**
   * Returns the current file.
   * @return int
   */
  public int getCurrentFile() {
    return mPrefs.getInt(KEY_CURRENT_FILE, DEFAULT_CURRENT_FILE);
  }

  /**
   * Sets the frequency value.
   * @param frequency The new value.
   */
  public void setFrequencyValue(int frequency) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putInt(KEY_FREQUENCY_VAL, frequency);
    e.apply();
  }

  /**
   * Returns the frequency value.
   * @return int
   */
  public int getFrequencyValue() {
    return mPrefs.getInt(KEY_FREQUENCY_VAL, DEFAULT_FREQUENCY_VAL);
  }

  /**
   * Sets the frequency unit.
   * @param frequency The new value.
   */
  public void setFrequencyUnit(int frequency) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putInt(KEY_FREQUENCY_UNIT, frequency);
    e.apply();
  }

  /**
   * Returns the frequency unit.
   * @return int
   */
  public int getFrequencyUnit() {
    return mPrefs.getInt(KEY_FREQUENCY_UNIT, DEFAULT_FREQUENCY_UNIT);
  }

  /**
   * Sets the frequency screen.
   * @param frequency The new value.
   */
  public void setFrequencyScreen(boolean frequency) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putBoolean(KEY_FREQUENCY_SCREEN, frequency);
    e.apply();
  }

  /**
   * Returns the frequency screen state.
   * @return boolean
   */
  public boolean isFrequencyScreen() {
    return mPrefs.getBoolean(KEY_FREQUENCY_SCREEN, DEFAULT_FREQUENCY_SCREEN);
  }

  /**
   * Sets the scrollable wallpaper.
   * @param scrollableWallpaper The new value.
   */
  public void setScrollableWallpaperFromFolder(boolean scrollableWallpaper) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putBoolean(KEY_SCROLLABLE_WALLPAPER_FROM_FOLDER, scrollableWallpaper);
    e.apply();
  }

  /**
   * Returns the scrollable wallpaper state.
   * @return boolean
   */
  public boolean isScrollableWallpaperFromFolder() {
    return mPrefs.getBoolean(KEY_SCROLLABLE_WALLPAPER_FROM_FOLDER, DEFAULT_SCROLLABLE_WALLPAPER_FROM_FOLDER);
  }

  /**
   * Sets the lock screen wallpaper.
   * @param lockScreen The new value.
   */
  public void setLockScreenWallpaper(boolean lockScreen) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putBoolean(KEY_LOCK_SCREEN_WALLPAPER, lockScreen);
    e.apply();
  }

  /**
   * Returns the lock screen wallpaper state.
   * @return boolean
   */
  public boolean isLockScreenWallpaper() {
    return mPrefs.getBoolean(KEY_LOCK_SCREEN_WALLPAPER, DEFAULT_LOCK_SCREEN_WALLPAPER);
  }

  /**
   * Sets the folder.
   * @param folder The new value.
   */
  public void setFolder(String folder) {
    SharedPreferences.Editor e = mPrefs.edit();
    e.putString(KEY_FOLDER, folder);
    e.apply();
  }

  /**
   * Returns the current timestamp.
   * @return String
   */
  public String getFolder() {
    return mPrefs.getString(KEY_FOLDER, DEFAULT_FOLDER);
  }

}
