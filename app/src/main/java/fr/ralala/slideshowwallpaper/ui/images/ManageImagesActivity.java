package fr.ralala.slideshowwallpaper.ui.images;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import java.io.File;
import java.util.StringTokenizer;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.sql.AppDatabase;
import fr.ralala.slideshowwallpaper.sql.Image;
import fr.ralala.slideshowwallpaper.ui.chooser.FileChooserActivity;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Manage images activity.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ManageImagesActivity extends AppCompatActivity{
  private GridViewAdapter mGridAdapter;
  private AppDatabase mAppDatabase;
  private GridView mGridView;

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    UIHelper.openTransition(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_manage_images);

    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    mGridView = findViewById(R.id.gridView);
    mAppDatabase = AppDatabase.getInstance(this);
  }

  /**
   * On click.
   * @param v The view.
   */
  public void onClick(View v) {
    final Intent intent = new Intent(getApplicationContext(), FileChooserActivity.class);
    intent.putExtra(FileChooserActivity.FILECHOOSER_TYPE_KEY, "" + FileChooserActivity.FILECHOOSER_TYPE_FILE_ONLY);
    intent.putExtra(FileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.chooser_image_title));
    intent.putExtra(FileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.chooser_image_message) + ":? ");
    intent.putExtra(FileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
    intent.putExtra(FileChooserActivity.FILECHOOSER_SHOW_KEY, "" + FileChooserActivity.FILECHOOSER_SHOW_FILE_AND_DIRECTORY);
    intent.putExtra(FileChooserActivity.FILECHOOSER_FILE_FILTER_KEY, "png,jpeg,jpg,webp");
    intent.putExtra(FileChooserActivity.FILECHOOSER_PREVIEW, FileChooserActivity.FILECHOOSER_PREVIEW_YES);
    intent.putExtra(FileChooserActivity.FILECHOOSER_MODE, FileChooserActivity.FILECHOOSER_MODE_MULTIPLE);
    startActivityForResult(intent, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE);
  }


  /**
   * Called when the activity gets a result after a call to the startActivityForResult method.
   * @param requestCode The request code.
   * @param resultCode The result code.
   * @param data The result data.
   */
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE) {
      if (resultCode == RESULT_OK) {
        StringTokenizer token = new StringTokenizer(data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY),
            FileChooserActivity.FILECHOOSER_MULTI_SEPARATOR);
        while(token.hasMoreTokens()) {
          final File file = new File(token.nextToken());
          if (!mGridAdapter.containsName(file.getName())) {
            Bitmap.CompressFormat format = null;
            String n = file.getName().toLowerCase();
            if (n.endsWith(".png"))
              format = Bitmap.CompressFormat.PNG;
            if (n.endsWith(".jpg") || n.endsWith(".jpeg"))
              format = Bitmap.CompressFormat.JPEG;
            if (n.endsWith(".webp"))
              format = Bitmap.CompressFormat.WEBP;
            if (format != null) {
              Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
              byte[] array = Image.bitmapToArray(bitmap, format);
              Image image = new Image(file.getName(), false, array, bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
              AppDatabase.insertImage(mAppDatabase, image);
              mGridAdapter.add(image);
            }
          } else {
            UIHelper.showAlertDialog(this, R.string.error_title, R.string.error_contains_image);
          }
        }
      }
    }
  }

  /**
   * Called when the activity is resumed.
   */
  public void onResume() {
    super.onResume();
    AppDatabase.listImages(mAppDatabase, this, (images) -> {
      mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, images);
      mGridView.setAdapter(mGridAdapter);
      mGridView.setOnItemClickListener((parent, v, position, id) -> {
        Image image = mGridAdapter.getItem(position);
        if(image != null) {
          final Intent intent = new Intent(this, ImageActivity.class);
          intent.putExtra(ImageActivity.KEY_NAME, image.getName());
          startActivity(intent);
        }
      });
      mGridView.setOnItemLongClickListener((parent, v, position, id) -> {
        UIHelper.showConfirmDialog(this, getString(R.string.delete),
            getString(R.string.confirm_delete),
            (view) -> {
              Image image = mGridAdapter.getItem(position);
              AppDatabase.deleteImage(mAppDatabase,image);
              mGridAdapter.remove(image);
            });
        return true;
      });
    });
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    UIHelper.closeTransition(this);
    super.onBackPressed();
  }

  /**
   * Called when the options item is clicked (home).
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
    }
    return false;
  }

}
