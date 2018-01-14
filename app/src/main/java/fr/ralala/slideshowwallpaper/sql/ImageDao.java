package fr.ralala.slideshowwallpaper.sql;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Sql image DAO.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
@Dao
public interface ImageDao {
  /**
   * Lists the images.
   * @return List<Image>
   */
  @Query("SELECT * FROM images")
  List<Image> list();

  /**
   * Finds an image by name.
   * @param name The image name.
   * @return Image
   */
  @Query("SELECT * FROM images where name LIKE  :name")
  Image findByName(String name);

  /**
   * Inserts an image.
   * @param image The image to insert.
   */
  @Insert
  void insert(Image image);

  /**
   * Updates an image.
   * @param image The image to update.
   */
  @Update
  void update(Image image);

  /**
   * Deletes an image.
   * @param image The image to delete.
   */
  @Delete
  void delete(Image image);
}
