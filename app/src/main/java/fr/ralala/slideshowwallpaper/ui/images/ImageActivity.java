package fr.ralala.slideshowwallpaper.ui.images;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.sql.AppDatabase;
import fr.ralala.slideshowwallpaper.sql.Image;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Image activity.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ImageActivity  extends AppCompatActivity {
  private static final int DELAY_MS = 200;
  protected static final String KEY_NAME = "ImageActivity.KEY_NAME";
  private Image mImage;
  private AppDatabase mAppDatabase;

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    UIHelper.openTransition(this);

    mAppDatabase = AppDatabase.getInstance(this);
    if(getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      AppDatabase.findImage(mAppDatabase, this, extras.getString(KEY_NAME), (image) -> mImage = image);
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image);

    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    ImageView iv = findViewById(R.id.image);
    iv.postDelayed(() -> iv.setImageDrawable(new BitmapDrawable(getResources(), mImage.getBitmap())), DELAY_MS);
  }

  /**
   * Called to create the option menu.
   * @param menu The main menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.image, menu);
    new Handler().postDelayed(() -> menu.findItem(R.id.action_scrollable).setChecked(mImage.isScrollable()), DELAY_MS);
    return true;
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
      case R.id.action_scrollable:
        item.setChecked(!item.isChecked());
        mImage.setScrollable(item.isChecked());
        AppDatabase.updateImage(mAppDatabase, mImage);
        return true;
    }
    return false;
  }

}
