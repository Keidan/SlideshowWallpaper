package fr.ralala.slideshowwallpaper.ui.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.SlideshowWallpaperApplication;
import fr.ralala.slideshowwallpaper.services.SlideshowWallpaperService;
import fr.ralala.slideshowwallpaper.ui.chooser.FileChooserActivity;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;

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
  private SlideshowWallpaperApplication mApp;
  private ToggleButton mToggleOnOff;
  private Button mBtBrowse;
  private TextView tvBrowse;
  private TextView tvFrequency;
  private ArrayAdapter<Integer> mAdapterFrequencyValue;

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

    mCkFrequency = findViewById(R.id.ckFrequency1);
    mSpFrequencyValue = findViewById(R.id.spFrequencyValue);
    mSpFrequencyUnit = findViewById(R.id.spFrequencyUnit);
    tvBrowse = findViewById(R.id.tvBrowse);
    tvFrequency = findViewById(R.id.tvFrequency);

    List<Integer> items = new ArrayList<>();
    mAdapterFrequencyValue = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
    mSpFrequencyValue.setAdapter(mAdapterFrequencyValue);

    fillValues();
    mSpFrequencyUnit.setSelection(mApp.getFrequencyUnit());
    mCkFrequency.setChecked(mApp.isFrequencyScreen());
    mSpFrequencyValue.setOnItemSelectedListener(this);
    mSpFrequencyUnit.setOnItemSelectedListener(this);
    mCkFrequency.setOnCheckedChangeListener(this);

    changeEnabledStateOnScreenCheckbox();

    mBtBrowse = findViewById(R.id.btBrowse);
    mBtBrowse.setOnClickListener((v) -> {
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

    mToggleOnOff = findViewById(R.id.toggleOnOff);
    mToggleOnOff.setChecked(isServiceRunning(SERVICE));
    mToggleOnOff.setOnClickListener((v) -> {
      if(!mToggleOnOff.isChecked()) {
        ((SlideshowWallpaperApplication)getApplication()).setSenpuku(true);
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
            killServiceIfRunning(SERVICE);
            ((SlideshowWallpaperApplication) getApplication()).setSenpuku(false);
            startService(new Intent(this, SERVICE));
          }
        }
      }
      changeEnabledStateOnServiceStart();
    });

    tvBrowse.setText(
        mApp.getFolder().equals("") ? getString(R.string.select_folder_none) : mApp.getFolder());
    /* permissions */
    ActivityCompat.requestPermissions(this, new String[]{
        Manifest.permission.SET_WALLPAPER,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
    }, 1);
  }

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
        for(int i = 30; i < 60; i++) {
          mAdapterFrequencyValue.add(i);
          if(i <= val) idx++;
        }
        break;
      case 1:
        for(int i = 1; i < 60; i++) {
          mAdapterFrequencyValue.add(i);
          if(i <= val) idx++;
        }
        break;
      case 2:
        for(int i = 1; i < 24; i++) {
          mAdapterFrequencyValue.add(i);
          if(i <= val) idx++;
        }
        break;
      case 3:
        for(int i = 1; i <= 30; i++) {
          mAdapterFrequencyValue.add(i);
          if(i <= val) idx++;
        }
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
    mBtBrowse.setEnabled(enable);
    tvBrowse.setEnabled(enable);
    if(enable) {
      changeEnabledStateOnScreenCheckbox();
    } else {
      tvFrequency.setEnabled(false);
      mSpFrequencyValue.setEnabled(false);
      mSpFrequencyUnit.setEnabled(false);
    }
  }

  /**
   * Changes the state of the frequency spinners state if the checkbox of the screen is checked.
   */
  private void changeEnabledStateOnScreenCheckbox() {
    boolean enable = !mCkFrequency.isChecked();
    mSpFrequencyValue.setEnabled(enable);
    mSpFrequencyUnit.setEnabled(enable);
    tvFrequency.setEnabled(enable);
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    if(isServiceRunning(SERVICE)) {
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
          tvBrowse.setText(mApp.getFolder());
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
    changeEnabledStateOnScreenCheckbox();
    mApp.setFrequencyScreen(mCkFrequency.isChecked());
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

  /**
   * Test if a specific service is in running state.
   * @param serviceClass The service class
   * @return boolean
   */
  private boolean isServiceRunning(final Class<?> serviceClass) {
    final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
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
   * @param serviceClass The service class.
   */
  private void killServiceIfRunning(final Class<?> serviceClass) {
    if(isServiceRunning(serviceClass))
      stopService(new Intent(this, serviceClass));
  }

}