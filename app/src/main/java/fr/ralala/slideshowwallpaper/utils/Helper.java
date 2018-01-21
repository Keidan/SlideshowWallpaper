package fr.ralala.slideshowwallpaper.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Android helper.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Helper {

  /**
   * Returns the real screen size.
   * @param context The Android context.
   * @return Point
   */
  public static Point getRealScreenDimension(final Context context) {
    Point point = new Point();
    WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if(window == null) {
      point.set(0, 0);
    } else {
      Display display = window.getDefaultDisplay();
      display.getRealSize(point);
    }
    return point;
  }

  /**
   * Returns the screen size.
   * @param context The Android context.
   * @return Point
   */
  private static Point getScreenDimension(final Context context) {
    Point point = new Point();
    WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if(window == null) {
      point.set(0, 0);
    } else {
      Display display = window.getDefaultDisplay();
      display.getSize(point);
    }
    return point;
  }

  /**
   * Returns the scrolled dimension.
   * @param context The Android context.
   * @param bm The bitmap.
   * @param real Uses real screen size.
   * @return Point
   */
  public static Point getScrolledDimension(final Context context, final Bitmap bm, boolean real) {
    Point point = new Point();
    Point screen = real ? getRealScreenDimension(context) : getScreenDimension(context);
    if(screen.x == 0 && screen.y == 0) {
      point.set(bm.getWidth(), bm.getHeight());
    } else {
      int screenHeight = screen.y;
      int screenWidth = screen.x;
      int width = bm.getWidth();
      int height = bm.getHeight();
      height = (height * screenWidth / bm.getWidth());
      width = (width * screenHeight) / bm.getHeight();
      point.set(width, height);
    }
    return point;
  }

  /**
   * Test if a specific service is in running state.
   * @param activity The main activity.
   * @param serviceClass The service class
   * @return boolean
   */
  public static boolean isServiceRunning(final Activity activity, final Class<?> serviceClass) {
    final ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
    if(manager != null) {
      for (final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.getName().equals(service.service.getClassName())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Kill a specific service (if running)
   * @param activity The main activity.
   * @param serviceClass The service class.
   */
  public static void killServiceIfRunning(final Activity activity, final Class<?> serviceClass) {
    if(isServiceRunning(activity, serviceClass))
      activity.stopService(new Intent(activity, serviceClass));
  }

}
