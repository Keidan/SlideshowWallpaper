package fr.ralala.slideshowwallpaper.sql;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Room database
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
@Database(entities = {Image.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
  private static final String DATABASE_NAME = "slides-show-wallpaper";
  private static AppDatabase sInstance;
  private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();
  private Executor mExecutor;

  /**
   * Returns the image DAO.
   * @return ImageDao
   */
  public abstract ImageDao imageDao();


  /**
   * Returns the AppDatabase instance.
   * @param context The android context.
   * @return AppDatabase
   */
  public static AppDatabase getInstance(final Context context) {
    if (sInstance == null) {
      synchronized (AppDatabase.class) {
        sInstance = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
            .build();
        sInstance.mExecutor = Executors.newSingleThreadExecutor();
        sInstance.updateDatabaseCreated(context.getApplicationContext());
      }
    }
    return sInstance;
  }

  /**
   * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
   * @param context The android context.
   */
  private void updateDatabaseCreated(final Context context) {
    if (context.getDatabasePath(DATABASE_NAME).exists()) {
      setDatabaseCreated();
    }
  }

  /**
   * Set created flag.
   */
  private void setDatabaseCreated(){
    mIsDatabaseCreated.postValue(true);
  }

  /**
   * Returns the created flag.
   * @return LiveData<Boolean>
   */
  public LiveData<Boolean> getDatabaseCreated() {
    return mIsDatabaseCreated;
  }

  /**
   * Updates an image to the database.
   * @param database The database reference.
   * @param image The image to update.
   */
  public static void updateImage(final AppDatabase database, final Image image) {
    synchronized (database) {
      database.mExecutor.execute(() -> database.runInTransaction(() -> database.imageDao().update(image)));
    }
  }

  /**
   * Inserts an image to the database.
   * @param database The database reference.
   * @param image The image to insert.
   */
  public static void insertImage(final AppDatabase database, final Image image) {
    synchronized (database) {
      database.mExecutor.execute(() -> database.runInTransaction(() -> database.imageDao().insert(image)));
    }
  }

  /**
   * Deletes an image to the database.
   * @param database The database reference.
   * @param image The image to delete.
   */
  public static void deleteImage(final AppDatabase database, final Image image) {
    synchronized (database) {
      database.mExecutor.execute(() -> database.runInTransaction(() -> database.imageDao().delete(image)));
    }
  }

  public interface ListListener {
    void listImages(final List<Image> images);
  }

  /**
   * Lists the images.
   * @param database The database reference.
   * @param activity The activity (if non null the listener is called in the UI thread).
   * @param listener The list listener.
   */
  public static void listImages(final AppDatabase database, final AppCompatActivity activity, final ListListener listener) {
    synchronized (database) {
      database.mExecutor.execute(() -> database.runInTransaction(() -> {
        List<Image> images = new ArrayList<>();
        images.addAll(database.imageDao().list());
        if(activity == null)
          listener.listImages(images);
        else
          activity.runOnUiThread(() -> listener.listImages(images));
      }));
    }
  }


  public interface FindListener {
    void findImage(final Image image);
  }


  /**
   * Finds an image.
   * @param database The database reference.
   * @param activity The activity (if non null the listener is called in the UI thread).
   * @param name The image name.
   * @param listener The find listener.
   */
  public static void findImage(final AppDatabase database, final AppCompatActivity activity, final String name, final FindListener listener) {
    synchronized (database) {
      database.mExecutor.execute(() -> database.runInTransaction(() -> {
        final Image img = database.imageDao().findByName(name);
        if(activity == null)
          listener.findImage(img);
        else
          activity.runOnUiThread(() -> listener.findImage(img));
      }));
    }
  }

}
