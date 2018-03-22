package fr.ralala.slideshowwallpaper.services.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.lang.ref.WeakReference;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.SlideshowWallpaperApplication;
import fr.ralala.slideshowwallpaper.ui.SlideshowWallpaperActivity;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Manages the service startup.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ServiceStartupTaskFromUI extends AsyncTask<Void, Void, Boolean>{
  private static final int DELAY_SEC = 10;
  private WeakReference<Activity> mActivityRef;
  private AlertDialog mProgress = null;
  private ServiceStartTaskListener mLi;

  public interface ServiceStartTaskListener {
    /**
     * Gets the service state.
     * @param started True if started.
     */
    void serviceStarted(boolean started);
  }

  public ServiceStartupTaskFromUI(Activity activity, ServiceStartTaskListener li) {
    mActivityRef = new WeakReference<>(activity);
    mLi = li;
  }

  /**
   *
   * Called when the task is in progress.
   * @param none Nothing.
   * @return true if success, false else.
   */
  protected Boolean doInBackground(Void... none) {
    Activity a = mActivityRef.get();
    SlideshowWallpaperApplication app = (SlideshowWallpaperApplication)a.getApplication();
    if(!app.getServiceUtils().isStarted()) {
      a.startService(new Intent(a, SlideshowWallpaperActivity.SERVICE));

      int sec = 0;
      while(!app.getServiceUtils().isSenpuku() && !app.getServiceUtils().isStarted()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
          Log.e(getClass().getSimpleName(), "InterruptedException: " + ie.getMessage(), ie);
        }
        if(sec++ > DELAY_SEC)
          return false;
      }
    }
    return true;
  }

  /**
   * Called when the task is started.
   */
  protected void onPreExecute() {
    mProgress = UIHelper.showProgressDialog(mActivityRef.get(), R.string.start_of_the_service);
    mProgress.show();
  }

  /**
   * Called when the task is finished.
   * @param start True if started false else.
   */
  protected void onPostExecute(Boolean start) {
    mProgress.dismiss();
    if(mLi != null)
      mLi.serviceStarted(start);
  }
}
