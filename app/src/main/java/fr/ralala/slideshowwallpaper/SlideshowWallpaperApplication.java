package fr.ralala.slideshowwallpaper;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

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
  private static final String KEY_FREQUENCY_VAL = "frequencyValue";
  private static final String KEY_FREQUENCY_UNIT = "frequencyUnit";
  private static final String KEY_FOLDER = "folder";
  private static final String KEY_CURRENT_FILE = "current";
  private static final int DEFAULT_FREQUENCY_VAL = 0;
  private static final int DEFAULT_FREQUENCY_UNIT = 0;
  private static final boolean DEFAULT_FREQUENCY_SCREEN = true;
  private static final String DEFAULT_FOLDER = "";
  private static final int DEFAULT_CURRENT_FILE = 0;
  private boolean mSenpuku = false;
  private SharedPreferences mPrefs;

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
   * Returns the frequency unit.
   * @return boolean
   */
  public boolean isFrequencyScreen() {
    return mPrefs.getBoolean(KEY_FREQUENCY_SCREEN, DEFAULT_FREQUENCY_SCREEN);
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
