package fr.ralala.slideshowwallpaper.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.SlideshowWallpaperApplication;
import fr.ralala.slideshowwallpaper.services.SlideshowWallpaperService;
import fr.ralala.slideshowwallpaper.ui.changelog.ChangeLog;
import fr.ralala.slideshowwallpaper.ui.changelog.Configuration;
import fr.ralala.slideshowwallpaper.ui.chooser.FileChooserActivity;
import fr.ralala.slideshowwallpaper.ui.images.ManageImagesActivity;
import fr.ralala.slideshowwallpaper.services.utils.ServiceStartupTaskFromUI;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;
import fr.ralala.slideshowwallpaper.utils.Helper;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Main activity.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SlideshowWallpaperActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener{
  public static final Class<?> SERVICE = SlideshowWallpaperService.class;
  private Spinner mSpFrequencyValue;
  private Spinner mSpFrequencyUnit;
  private CheckBox mCkFrequency;
  private CheckBox mCkScrollableWallpaperFromFolder;
  private CheckBox mCkLockScreenWallpaper;
  private SlideshowWallpaperApplication mApp;
  private ToggleButton mToggleOnOff;
  private Button mBtBrowseFromFolder;
  private TextView mTvBrowseFromFolder;
  private TextView mTvFrequency;
  private ArrayAdapter<Integer> mAdapterFrequencyValue;
  private ChangeLog mChangeLog;
  private RadioButton mRbBrowseFromFolder;
  private RadioButton mRbBrowseFromDatabase;
  private Button mBtBrowseFromDatabase;

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    UIHelper.openTransition(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_slideshow_wallpaper);
    mApp = (SlideshowWallpaperApplication)getApplication();

    mRbBrowseFromFolder = findViewById(R.id.rbBrowseFromFolder);
    mRbBrowseFromDatabase = findViewById(R.id.rbBrowseFromDatabase);
    mCkFrequency = findViewById(R.id.ckFrequency);
    mCkScrollableWallpaperFromFolder = findViewById(R.id.ckScrollableWallpaperFromFolder);
    mCkLockScreenWallpaper = findViewById(R.id.ckLockScreenWallpaper);
    mSpFrequencyValue = findViewById(R.id.spFrequencyValue);
    mSpFrequencyUnit = findViewById(R.id.spFrequencyUnit);
    mTvBrowseFromFolder = findViewById(R.id.tvBrowseFromFolder);
    mTvFrequency = findViewById(R.id.tvFrequency);
    List<Integer> items = new ArrayList<>();
    mAdapterFrequencyValue = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
    mSpFrequencyValue.setAdapter(mAdapterFrequencyValue);

    fillValues();
    mSpFrequencyUnit.setSelection(mApp.getFrequencyUnit());
    mCkFrequency.setChecked(mApp.isFrequencyScreen());
    mCkScrollableWallpaperFromFolder.setChecked(mApp.isScrollableWallpaperFromFolder());
    mCkLockScreenWallpaper.setChecked(mApp.isLockScreenWallpaper());
    mSpFrequencyValue.setOnItemSelectedListener(this);
    mSpFrequencyUnit.setOnItemSelectedListener(this);
    mCkFrequency.setOnCheckedChangeListener(this);
    mCkScrollableWallpaperFromFolder.setOnCheckedChangeListener(this);
    mCkLockScreenWallpaper.setOnCheckedChangeListener(this);
    mRbBrowseFromFolder.setOnCheckedChangeListener(this);
    mRbBrowseFromDatabase.setOnCheckedChangeListener(this);

    changeEnabledStateOnScreenCheckbox();

    mBtBrowseFromFolder = findViewById(R.id.btBrowseFromFolder);
    mBtBrowseFromFolder.setOnClickListener((v) -> {
      if(SlideshowWallpaperApplication.checkPermissions(this)) {
        final Intent intent = new Intent(getApplicationContext(), FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.FILECHOOSER_TYPE_KEY, "" + FileChooserActivity.FILECHOOSER_TYPE_DIRECTORY_ONLY);
        intent.putExtra(FileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.chooser_image_folder_title));
        intent.putExtra(FileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.chooser_image_folder_message) + ":? ");
        intent.putExtra(FileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
        intent.putExtra(FileChooserActivity.FILECHOOSER_SHOW_KEY, "" + FileChooserActivity.FILECHOOSER_SHOW_DIRECTORY_ONLY);
        startActivityForResult(intent, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
      } else {
        UIHelper.showAlertDialog(this, R.string.error_title, R.string.error_permission_message);
      }
    });
    mBtBrowseFromDatabase = findViewById(R.id.btBrowseFromDatabase);
    mBtBrowseFromDatabase.setOnClickListener((v) -> {
      UIHelper.closeTransition(this);
      Intent intent = new Intent(this, ManageImagesActivity.class);
      startActivity(intent);
    });

    mToggleOnOff = findViewById(R.id.toggleOnOff);
    mToggleOnOff.setChecked(Helper.isServiceRunning(this, SERVICE));
    mToggleOnOff.setOnClickListener((v) -> {
      if(!mToggleOnOff.isChecked()) {
        ((SlideshowWallpaperApplication)getApplication()).getServiceUtils().setSenpuku(true);
        stopService(new Intent(this, SERVICE));
        mApp.setCurrentFile(0);
      } else {
        if(mApp.getFolder().isEmpty()) {
          UIHelper.showAlertDialog(this, R.string.error_title, R.string.error_invalid_folder_empty);
          mToggleOnOff.setChecked(false);
        } else {
          File folder = new File(mApp.getFolder());
          if (!folder.exists() || !folder.canRead() || !folder.isDirectory()) {
            UIHelper.showAlertDialog(this, R.string.error_title,
                getString(R.string.error_invalid_folder_attribute) +
                    "[e:" + folder.exists() + ",r:" + folder.canRead() + ",d:" + folder.isDirectory() + "]");
            mToggleOnOff.setChecked(false);
          } else {
            Helper.killServiceIfRunning(this, SERVICE);
            mApp.setCurrentFile(SlideshowWallpaperApplication.DEFAULT_CURRENT_FILE);
            ((SlideshowWallpaperApplication) getApplication()).getServiceUtils().setSenpuku(false);
            new ServiceStartupTaskFromUI(this, (start) -> {
              if(!start) {
                UIHelper.showAlertDialog(this, R.string.error_title,
                    getString(R.string.error_start_service));
                mToggleOnOff.setChecked(false);
                ((SlideshowWallpaperApplication) getApplication()).getServiceUtils().setSenpuku(true);
                stopService(new Intent(this, SERVICE));
                changeEnabledStateOnServiceStart();
              }
            }).execute();
          }
        }
      }
      changeEnabledStateOnServiceStart();
    });

    mTvBrowseFromFolder.setText(
        mApp.getFolder().equals("") ? getString(R.string.select_folder_none) : mApp.getFolder());
    /* permissions */
    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.SET_WALLPAPER,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
    }, 1);
    /* Changelog */
    mChangeLog = new ChangeLog(
        new Configuration(
            R.raw.changelog,
            R.string.changelog_ok_button,
            R.string.background_color,
            R.string.changelog_title,
            R.string.changelog_full_title,
            R.string.changelog_show_full), this);
    if(mChangeLog.firstRun())
      mChangeLog.getLogDialog().show();

    changeBrowseFromState(mApp.getBrowseFrom());
  }

  /**
   * Called to create the option menu.
   * @param menu The main menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  /**
   * Called when the user select an option menu item.
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_changelog:
        mChangeLog.getFullLogDialog().show();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Fill the adapter.
   * @param val The current value.
   * @param min The min value.
   * @param max The max value.
   * @return The index.
   */
  private int fillAdapter(int val, int min, int max) {
    int idx = 0;
    for(int i = min; i < max; i++) {
      mAdapterFrequencyValue.add(i);
      if(i <= val) idx++;
    }
    return idx;
  }

  /**
   * Fill the values.
   */
  private void fillValues() {
    /*
     * 0: Seconds
     * 1: Minutes
     * 2: Hours
     * 3: Days
     */
    mAdapterFrequencyValue.clear();
    int idx = 0;
    int val = mApp.getFrequencyValue();
    switch (mApp.getFrequencyUnit()) {
      case 0:
        idx = fillAdapter(val, 30, 60);
        break;
      case 1:
        idx = fillAdapter(val, 1, 60);
        break;
      case 2:
        idx = fillAdapter(val, 1, 24);
        break;
      case 3:
        idx = fillAdapter(val, 1, 31);
        break;
    }
    try {
      mSpFrequencyValue.setSelection(idx - 1);
    } catch(Exception e) {
      mSpFrequencyValue.setSelection(mAdapterFrequencyValue.getCount() - 1);
    }
  }

  /**
   * Change the state of the components if the service is started.
   */
  private void changeEnabledStateOnServiceStart() {
    final boolean enable = !mToggleOnOff.isChecked();
    mCkFrequency.setEnabled(enable);
    mCkLockScreenWallpaper.setEnabled(enable);
    mBtBrowseFromFolder.setEnabled(enable);
    mTvBrowseFromFolder.setEnabled(enable);
    mRbBrowseFromDatabase.setEnabled(enable);
    mRbBrowseFromFolder.setEnabled(enable);
    if(enable) {
      changeEnabledStateOnScreenCheckbox();
      if(mRbBrowseFromFolder.isChecked()) {
        changeBrowseFromState(SlideshowWallpaperApplication.BROWSE_FROM_FOLDER);
      } else if(mRbBrowseFromDatabase.isChecked()) {
        changeBrowseFromState(SlideshowWallpaperApplication.BROWSE_FROM_DATABASE);
      }
    } else {
      mCkScrollableWallpaperFromFolder.setEnabled(false);
      mTvFrequency.setEnabled(false);
      mSpFrequencyValue.setEnabled(false);
      mSpFrequencyUnit.setEnabled(false);
      mBtBrowseFromDatabase.setEnabled(false);
      mTvBrowseFromFolder.setEnabled(false);
      mBtBrowseFromFolder.setEnabled(false);
    }
  }


  /**
   * Changes the state of the browse components.
   * @param from Browse from ?
   */
  private void changeBrowseFromState(int from) {
    if(from == SlideshowWallpaperApplication.BROWSE_FROM_DATABASE) {
      mRbBrowseFromDatabase.setChecked(true);
      mRbBrowseFromFolder.setChecked(false);
      mBtBrowseFromDatabase.setEnabled(true);
      mTvBrowseFromFolder.setEnabled(false);
      mBtBrowseFromFolder.setEnabled(false);
      mCkScrollableWallpaperFromFolder.setEnabled(false);
    } else if(from == SlideshowWallpaperApplication.BROWSE_FROM_FOLDER) {
      mRbBrowseFromDatabase.setChecked(false);
      mRbBrowseFromFolder.setChecked(true);
      mBtBrowseFromDatabase.setEnabled(false);
      mTvBrowseFromFolder.setEnabled(true);
      mBtBrowseFromFolder.setEnabled(true);
      mCkScrollableWallpaperFromFolder.setEnabled(true);
    }
    if(from != mApp.getBrowseFrom())
      mApp.setBrowseFrom(from);
  }

  /**
   * Changes the state of the frequency spinners state if the checkbox of the screen is checked.
   */
  private void changeEnabledStateOnScreenCheckbox() {
    boolean enable = !mCkFrequency.isChecked();
    mSpFrequencyValue.setEnabled(enable);
    mSpFrequencyUnit.setEnabled(enable);
    mTvFrequency.setEnabled(enable);
    mCkScrollableWallpaperFromFolder.setEnabled(enable);
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    if(Helper.isServiceRunning(this, SlideshowWallpaperActivity.SERVICE)) {
      mToggleOnOff.setChecked(true);
      changeEnabledStateOnServiceStart();
    }
  }

  /**
   * Called when the activity gets a result after a call to the startActivityForResult method.
   * @param requestCode The request code.
   * @param resultCode The result code.
   * @param data The result data.
   */
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
     if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY) {
      if (resultCode == RESULT_OK) {
        final String rootDir = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        String folder = mApp.getFolder();
        if(!folder.equals(rootDir)) {
          mApp.setFolder(rootDir);
          mTvBrowseFromFolder.setText(mApp.getFolder());
        }
      }
    }
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    UIHelper.closeTransition(this);
    super.onBackPressed();
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    if(compoundButton.getId() == R.id.ckFrequency) {
      changeEnabledStateOnScreenCheckbox();
      mApp.setFrequencyScreen(mCkFrequency.isChecked());
    } else if(compoundButton.getId() == R.id.ckScrollableWallpaperFromFolder)
      mApp.setScrollableWallpaperFromFolder(mCkScrollableWallpaperFromFolder.isChecked());
    else if(compoundButton.getId() == R.id.ckLockScreenWallpaper)
      mApp.setLockScreenWallpaper(mCkLockScreenWallpaper.isChecked());
    else if(compoundButton.getId() == R.id.rbBrowseFromFolder) {
      if(mRbBrowseFromFolder.isChecked()) {
        changeBrowseFromState(SlideshowWallpaperApplication.BROWSE_FROM_FOLDER);
      }
    } else if(compoundButton.getId() == R.id.rbBrowseFromDatabase) {
      if(mRbBrowseFromDatabase.isChecked()) {
        changeBrowseFromState(SlideshowWallpaperApplication.BROWSE_FROM_DATABASE);
      }
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    switch (adapterView.getId()){
      case R.id.spFrequencyUnit: {
        int freq = mApp.getFrequencyUnit();
        int pos = mSpFrequencyUnit.getSelectedItemPosition();
        if(freq != pos) {
          mApp.setFrequencyUnit(pos);
        }
        fillValues();
        break;
      }
      case R.id.spFrequencyValue: {
        int freq = mApp.getFrequencyValue();
        @SuppressWarnings("ConstantConditions") int pos = mAdapterFrequencyValue.getItem(i);
        if(freq != pos) {
          mApp.setFrequencyValue(pos);
        }
        break;
      }
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

  }

}