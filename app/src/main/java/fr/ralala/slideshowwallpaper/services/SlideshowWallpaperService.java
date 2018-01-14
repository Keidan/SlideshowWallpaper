package fr.ralala.slideshowwallpaper.services;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.SlideshowWallpaperApplication;
import fr.ralala.slideshowwallpaper.sql.AppDatabase;
import fr.ralala.slideshowwallpaper.sql.Image;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Wallpaper service
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SlideshowWallpaperService extends Service implements Runnable {
  private static final String RESTART_ACTION = "fr.ralala.slideshowwallpaper.RESTART";
  private SlideshowWallpaperApplication mApp = null;
  private Handler mHandler;
  private WallpaperManager mWallpaperManager;
  private ScreenReceiver mScreenReceiver = null;

  private class ScreenReceiver extends BroadcastReceiver {
    /**
     * Called by the system when the screen state is changed.
     * @param context An Android context.
     * @param intent Associated intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
      final String action = intent.getAction();
      if(action == null)
        return;
      if(action.equals(Intent.ACTION_SCREEN_OFF))
        performChange();
    }
  }

  /**
   * Called by the system when the service is first created. Do not call this method directly.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    boolean kill = false;
    if(!SlideshowWallpaperApplication.checkPermissions(this)) {
      String err = getString(R.string.error_permissions_not_granted);
      UIHelper.toast(this, err);
      Log.e(getClass().getSimpleName(), err);
      kill = true;
    }
    if(!kill) {
      mApp = ((SlideshowWallpaperApplication) getApplication());
      if (mApp.getFolder().isEmpty()) {
        kill = true;
      } else {
        File folder = new File(mApp.getFolder());
        if (!folder.exists() || !folder.canRead() || !folder.isDirectory()) {
          String err = getString(R.string.error_invalid_folder_attribute) +
              "[e:" + folder.exists() + ",r:" + folder.canRead() + ",d:" + folder.isDirectory() + "]";
          UIHelper.toast(this, err);
          Log.e(getClass().getSimpleName(), err);
          kill = true;
        }
      }
      if(!kill) {
        mWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        if(!mApp.isFrequencyScreen())
          mHandler = new Handler();
        else {
          // register receiver that handles screen on and screen off logic
          final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
          filter.addAction(Intent.ACTION_SCREEN_OFF);
          mScreenReceiver = new ScreenReceiver();
          registerReceiver(mScreenReceiver, filter);
        }
      }
    }
    if(kill) {
      mApp.setSenpuku(true);
      stopSelf();
    }
  }

  /**
   * Called by the system to notify a Service that it is no longer used and is being removed.
   */
  @Override
  public void onDestroy() {
    if(mHandler != null)
      mHandler.removeCallbacks(this);
    if (mScreenReceiver != null) {
      unregisterReceiver(mScreenReceiver);
      mScreenReceiver = null;
    }
    if (!mApp.isSenpuku()) {
      Log.i(getClass().getSimpleName(), "Restart service.");
      /* restart the activity */
      final Intent intent = new Intent(this, RestartServiceReceiver.class);
      intent.setAction(RESTART_ACTION);
      sendBroadcast(intent);
    } else
      Log.i(getClass().getSimpleName(), "Destroy service.");
    super.onDestroy();
  }

  /**
   * Called by the system every time a client explicitly starts the service
   * @param intent The Intent supplied to startService(Intent), as given.
   * @param flags Additional data about this start request.
   * @param startId A unique integer representing this specific request to start.
   * @return int
   */
  @Override
  public int onStartCommand(final Intent intent, final int flags,
                            final int startId) {
    performChange();
    if(!mApp.isFrequencyScreen()) {
      long delay = getDelay();
      Log.i(getClass().getSimpleName(), "Service delay " + delay);
      mHandler.postDelayed(this, delay);
    }
    return START_STICKY;

  }

  /**
   * Changes the wallpaper.
   * @param bm The Bitmap to set.
   * @param scrollable Scrollable wallpaper.
   * @param idx Current index (log use).
   * @throws Exception If an error has occurred.
   */
  private void changeWallpaper(Bitmap bm, boolean scrollable, int idx) throws Exception {
    Log.i(getClass().getSimpleName(), "MainScreen -> idx:"+idx+", current:"+mApp.getCurrentFile() + ", bm:"+bm + ", scrollable:"+scrollable);
    if(!scrollable)
      mWallpaperManager.setBitmap(bm);
    else {
      //get screen height
      boolean error = false;
      WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
      if(window != null) {
        try {
          Display display = window.getDefaultDisplay();
          Point size = new Point();
          display.getSize(size);
          int screenHeight = size.y;
          //adjust the aspect ratio of the Image
          //this is the main part
          int width = bm.getWidth();
          int height = bm.getHeight();
          width = (width * screenHeight) / bm.getHeight();
          Log.i(getClass().getSimpleName(), "MainScreen -> Src [w:" + bm.getWidth() + ", h:" +
              bm.getHeight() + "], Screen [w:" + size.x + ", h:" + size.y + "], Dst [w:" + width + ", h:" + height + "]");
          //set the wallpaper
          //this may not be the most efficient way but it worked for me
          mWallpaperManager.setBitmap(Bitmap.createScaledBitmap(bm, width, height, true));
        } catch (Exception e) {
          Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
          error = true;
        }
      } else
        error = true;
      if(error) {
        Log.e(getClass().getSimpleName(), "MainScreen -> An error occurred with the scrollable wallpaper. Using static wallpaper instead.");
        mWallpaperManager.setBitmap(bm);
      }
    }
    if(mApp.isLockScreenWallpaper()) {
      Log.i(getClass().getSimpleName(), "LockScreen -> idx:"+idx+", current:"+mApp.getCurrentFile() + ", bm:"+bm);
      mWallpaperManager.setBitmap(bm, null, true, WallpaperManager.FLAG_LOCK);
    }
  }

  /**
   * Apply the change.
   */
  private void performChange() {
    if(mApp.getBrowseFrom() == SlideshowWallpaperApplication.BROWSE_FROM_FOLDER) {
      Log.d(getClass().getSimpleName(), "performChange from folder");
      int idx = 0;
      File folder = new File(mApp.getFolder());
      File[] files = folder.listFiles();
      if (files == null) {
        stopSelf();
        return;
      }

      boolean scrollable = mApp.isScrollableWallpaperFromFolder();
      for (File file : files) {
        String path = file.getAbsolutePath();
        if (file.isFile() && isImageFile(path)) {
          int i = idx;
          idx++;
          if (i == mApp.getCurrentFile()) {
            try {
              Bitmap bm = BitmapFactory.decodeFile(path);
              changeWallpaper(bm, scrollable, idx);
              break;
            } catch (Exception e) {
              String err = getString(R.string.error_unable_to_read_file) + " '" + file.getName() + "'";
              UIHelper.toast(this, err);
              Log.e(getClass().getSimpleName(), err, e);
            }
          }
        }
      }
      if (idx >= files.length)
        idx = 0;
      if (idx != mApp.getCurrentFile())
        mApp.setCurrentFile(idx);
    } else if(mApp.getBrowseFrom() == SlideshowWallpaperApplication.BROWSE_FROM_DATABASE) {
      Log.d(getClass().getSimpleName(), "performChange from database");
      AppDatabase appDatabase = AppDatabase.getInstance(this);
      AppDatabase.listImages(appDatabase, null, (images) -> {
        int idx = 0;
        for(int i = 0; i < images.size(); i++) {
          idx++;
          if (i == mApp.getCurrentFile()) {
            Image image = images.get(i);
            try {
              if(image.isScrollable()) {
                Bitmap temp = image.getBitmap();
                Log.i(getClass().getSimpleName(), "MainScreen -> Original [x:" + image.getX() + ", y:" +
                    image.getY() + ", w:" + temp.getWidth() + ", h:" + temp.getHeight() + "]");
                Bitmap part = Bitmap.createBitmap(temp, image.getX(), image.getY(), image.getWidth(), image.getHeight(), null, true);
                changeWallpaper(part, true, idx);
              } else
                changeWallpaper(image.getBitmap(), false, idx);
              break;
            } catch (Exception e) {
              String err = getString(R.string.error_unable_to_read_file) + " '" + image.getName() + "'";
              UIHelper.toast(this, err);
              Log.e(getClass().getSimpleName(), err, e);
            }
          }
        }
        if (idx >= images.size())
          idx = 0;
        if (idx != mApp.getCurrentFile())
          mApp.setCurrentFile(idx);
      });
    }
  }

  /**
   * Test if the file is an image or not.
   * @param path The file to test.
   * @return boolean
   */
  private static boolean isImageFile(String path) {
    String mimeType = URLConnection.guessContentTypeFromName(path);
    return mimeType != null && mimeType.startsWith("image");
  }

  /**
   * Called when on service bind.
   * @param intent Unused.
   * @return null
   */
  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }

  /**
   * Called when the handler post the runnable action.
   */
  @Override
  public void run() {
    mHandler.removeCallbacks(this);
    if(!mApp.isFrequencyScreen()) {
      performChange();
      mHandler.postDelayed(this, getDelay());
    }
  }

  public long getDelay() {
    long delay = 0L;
    /*
     * 0: Seconds
     * 1: Minutes
     * 2: Hours
     * 3: Days
     */
    switch(mApp.getFrequencyUnit()) {
      case 0:
        delay = TimeUnit.SECONDS.toMillis(mApp.getFrequencyValue());
        break;
      case 1:
        delay = TimeUnit.MINUTES.toMillis(mApp.getFrequencyValue());
        break;
      case 2:
        delay = TimeUnit.HOURS.toMillis(mApp.getFrequencyValue());
        break;
      case 3:
        delay = TimeUnit.DAYS.toMillis(mApp.getFrequencyValue());
        break;
    }
    return delay;
  }
}
