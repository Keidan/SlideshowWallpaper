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

import java.io.File;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.SlideshowWallpaperApplication;
import fr.ralala.slideshowwallpaper.sql.AppDatabase;
import fr.ralala.slideshowwallpaper.sql.Image;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;
import fr.ralala.slideshowwallpaper.utils.Helper;

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
    mApp = ((SlideshowWallpaperApplication) getApplication());
    mApp.getServiceUtils().setCreated(true);
    if(!SlideshowWallpaperApplication.checkPermissions(this)) {
      String err = getString(R.string.error_permissions_not_granted);
      UIHelper.toast(this, err);
      Log.e(getClass().getSimpleName(), err);
      kill = true;
    }
    if(!kill) {
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
      mApp.getServiceUtils().setSenpuku(true);
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
    mApp.getServiceUtils().setCreated(false);
    mApp.getServiceUtils().setStarted(false);
    if (!mApp.getServiceUtils().isSenpuku()) {
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
    new Thread(() -> {
      performChange();
      if(!mApp.isFrequencyScreen()) {
        long delay = getDelay();
        Log.i(getClass().getSimpleName(), "Service delay " + delay);
        mHandler.postDelayed(this, delay);
      }
    }).start();
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
    Log.i(getClass().getSimpleName(), "MainScreen -> idx:"+idx+", current:"+mApp.getCurrentFile() + ", bm:[w="+bm.getWidth()+",h=" + bm.getHeight()+"]" + ", scrollable:"+scrollable);
    mWallpaperManager.setBitmap(bm);
    if(mApp.isLockScreenWallpaper()) {
      Log.i(getClass().getSimpleName(), "LockScreen -> idx:"+idx+", current:"+mApp.getCurrentFile() + ", bm:"+bm);
      mWallpaperManager.setBitmap(bm, null, true, WallpaperManager.FLAG_LOCK);
    }
  }

  /**
   * Apply the change.
   */
  private void performChange() {
    Point screenSize = Helper.getRealScreenDimension(this);
    Log.i(getClass().getSimpleName(), "MainScreen -> Screen [width:" + screenSize.x + ", height:" + screenSize.y + "]");
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
              if(scrollable) {
                Point p = Helper.getScrolledDimension(this, bm, false);
                bm = Bitmap.createScaledBitmap(bm, p.x, p.y, true);
              }
              changeWallpaper(bm, scrollable, idx);
              break;
            } catch (Exception e) {
              String err = getString(R.string.error_unable_to_read_file) + " '" + file.getName() + "'";
              UIHelper.toast(this, err);
              Log.e(getClass().getSimpleName(), err, e);
              mApp.getServiceUtils().setStarted(false);
              stopSelf();
              return;
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
                    image.getY() + ", w:" + temp.getWidth() + ", h:" + temp.getHeight() + "], Db [w:" + image.getWidth() + ", h:" + image.getHeight() + "]");
                //Bitmap part = Bitmap.createBitmap(temp, image.getX(), image.getY(), image.getWidth() - image.getX(), image.getHeight() - image.getY(), null, true);
                Bitmap part = Bitmap.createBitmap(temp, image.getX(), image.getY(), image.getWidth() - image.getX(), image.getHeight() - image.getY(), null, true);

                Log.i(getClass().getSimpleName(), "MainScreen -> Scrolled [x:0, y:0, w:" + part.getWidth() + ", h:" + part.getHeight() + "]");
                changeWallpaper(part, true, idx);
              } else
                changeWallpaper(image.getBitmap(), false, idx);
              break;
            } catch (Exception e) {
              String err = getString(R.string.error_unable_to_read_file) + " '" + image.getName() + "'";
              UIHelper.toast(this, err);
              Log.e(getClass().getSimpleName(), err, e);
              try {
                changeWallpaper(image.getBitmap(), false, idx);
              }  catch (Exception ee) {
                Log.e(getClass().getSimpleName(), "Exception: " + ee.getMessage(), ee);
                mApp.getServiceUtils().setStarted(false);
                stopSelf();
                return;
              }
              break;
            }
          }
        }
        if (idx >= images.size())
          idx = 0;
        if (idx != mApp.getCurrentFile())
          mApp.setCurrentFile(idx);
      });
    }
    mApp.getServiceUtils().setStarted(true);
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
