package fr.ralala.slideshowwallpaper.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fr.ralala.slideshowwallpaper.SlideshowWallpaperApplication;
import fr.ralala.slideshowwallpaper.ui.SlideshowWallpaperActivity;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Management of restart notifications
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class RestartServiceReceiver extends BroadcastReceiver {

  /**
   * Called by the system when it is fully started.
   * @param context An Android context.
   * @param intent Not used.
   */
  @Override
  public void onReceive(final Context context, final Intent intent) {
    Log.i(getClass().getSimpleName(), "New restart notification received.");
    if(intent.getAction() == null) {
      Log.w(getClass().getSimpleName(), "System fully started with null action.");
    }
    ((SlideshowWallpaperApplication)context.getApplicationContext()).setLastUpdateTime(SlideshowWallpaperApplication.DEFAULT_LAST_UPDATE_TIME);
    final Intent service = new Intent(context, SlideshowWallpaperActivity.SERVICE);
    context.startService(service);
  }
}
