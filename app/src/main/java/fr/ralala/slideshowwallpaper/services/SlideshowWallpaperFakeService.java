package fr.ralala.slideshowwallpaper.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Generates a service notification to prevent the service from being killed by the operating system.
 * Normally, this notification is not displayed in the status bar.
 * https://stackoverflow.com/questions/10962418/how-to-startforeground-without-showing-notification
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SlideshowWallpaperFakeService extends Service {
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (SlideshowWallpaperService.instance == null)
      throw new RuntimeException(SlideshowWallpaperService.class.getSimpleName() + " not running");

    //Set both services to foreground using the same notification id, resulting in just one notification
    startForeground(SlideshowWallpaperService.instance);
    startForeground(this);

    //Cancel this service's notification, resulting in zero notifications
    stopForeground(true);

    //Stop this service so we don't waste RAM.
    //Must only be called *after* doing the work or the notification won't be hidden.
    stopSelf();

    return START_NOT_STICKY;
  }

  private static final int NOTIFICATION_ID = 10;

  private static void startForeground(Service service) {
    @SuppressWarnings("deprecation") Notification notification = new Notification.Builder(service).getNotification();
    service.startForeground(NOTIFICATION_ID, notification);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
