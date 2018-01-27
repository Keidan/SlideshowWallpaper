package fr.ralala.slideshowwallpaper.ui.chooser;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * File chooser activity
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.MultiChoiceModeListener{
  private static final int SIZE_1KB = 0x400;
  private static final int SIZE_1MB = 0x100000;
  private static final int SIZE_1GB = 0x40000000;
  public static final String FILECHOOSER_TYPE_KEY = "type";
  public static final String FILECHOOSER_TITLE_KEY = "title";
  public static final String FILECHOOSER_MESSAGE_KEY = "message";
  public static final String FILECHOOSER_SHOW_KEY = "show";
  public static final String FILECHOOSER_FILE_FILTER_KEY = "file_filter";
  public static final String FILECHOOSER_DEFAULT_DIR = "default_dir";
  public static final String FILECHOOSER_USER_MESSAGE = "user_message";
  public static final String FILECHOOSER_PREVIEW = "preview";
  public static final String FILECHOOSER_MODE = "mode";
  public static final int FILECHOOSER_TYPE_FILE_ONLY = 0;
  public static final int FILECHOOSER_TYPE_DIRECTORY_ONLY  = 1;
  public static final int FILECHOOSER_TYPE_FILE_AND_DIRECTORY = 2;
  public static final int FILECHOOSER_SHOW_DIRECTORY_ONLY = 1;
  public static final int FILECHOOSER_SHOW_FILE_AND_DIRECTORY = 2;
  public static final String FILECHOOSER_FILE_FILTER_ALL = "*";
  public static final boolean FILECHOOSER_PREVIEW_YES = true;
  public static final boolean FILECHOOSER_PREVIEW_NO = false;
  public static final boolean FILECHOOSER_MODE_SINGLE = false;
  public static final boolean FILECHOOSER_MODE_MULTIPLE = true;
  protected File mCurrentDir = null;
  protected File mDefaultDir = null;
  private FileChooserArrayAdapter mAdapter = null;
  private String mConfirmMessage = null;
  private String mConfirmTitle = null;
  private String mUserMessage = null;
  private String mFileFilter = FILECHOOSER_FILE_FILTER_ALL;
  private boolean mPreview = FILECHOOSER_PREVIEW_NO;
  private boolean mMode = FILECHOOSER_MODE_SINGLE;
  private int mType = FILECHOOSER_TYPE_FILE_AND_DIRECTORY;
  private int mShow = FILECHOOSER_SHOW_FILE_AND_DIRECTORY;
  private ListView mListview = null;
  public static final String FILECHOOSER_MULTI_SEPARATOR = ";";
  public static final String FILECHOOSER_SELECTION_KEY = "selection";
  public static final int FILECHOOSER_SELECTION_TYPE_FILE = 1;
  public static final int FILECHOOSER_SELECTION_TYPE_DIRECTORY = 2;
  private static final int MSG_ERR    = 0;
  private static final int MSG_OK     = 1;
  private static final int MSG_CANCEL = 2;
  private List<FileChooserOption> mOpts = null;
  private Handler mHandler = null;
  private AlertDialog mProgress = null;

  public enum ErrorStatus {
    NO_ERROR, CANCEL, ERROR_NOT_MOUNTED
  }

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    UIHelper.openTransition(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filechooser);
    mListview = findViewById(R.id.list);
    Bundle b = getIntent().getExtras();
    if(b != null) {
      if (b.containsKey(FILECHOOSER_TYPE_KEY))
        mType = Integer.parseInt(b.getString(FILECHOOSER_TYPE_KEY));
      if (b.containsKey(FILECHOOSER_TITLE_KEY))
        mConfirmTitle = b.getString(FILECHOOSER_TITLE_KEY);
      if (b.containsKey(FILECHOOSER_MESSAGE_KEY))
        mConfirmMessage = b.getString(FILECHOOSER_MESSAGE_KEY);
      if (b.containsKey(FILECHOOSER_SHOW_KEY))
        mShow = Integer.parseInt(b.getString(FILECHOOSER_SHOW_KEY));
      if (b.containsKey(FILECHOOSER_FILE_FILTER_KEY))
        mFileFilter = b.getString(FILECHOOSER_FILE_FILTER_KEY);
      if (b.containsKey(FILECHOOSER_DEFAULT_DIR))
        mDefaultDir = new File("" + b.getString(FILECHOOSER_DEFAULT_DIR));
      if (b.containsKey(FILECHOOSER_USER_MESSAGE))
        mUserMessage = b.getString(FILECHOOSER_USER_MESSAGE);
      if (b.containsKey(FILECHOOSER_PREVIEW))
        mPreview = b.getBoolean(FILECHOOSER_PREVIEW);
      if (b.containsKey(FILECHOOSER_MODE))
        mMode = b.getBoolean(FILECHOOSER_MODE);
    } else
      Log.e(getClass().getSimpleName(), "Null intent extras");
    if(mConfirmTitle == null) mConfirmTitle = "title";
    if(mConfirmMessage == null) mConfirmMessage = "message";
    mCurrentDir = mDefaultDir;
    mListview.setLongClickable(true);
    if(mMode == FILECHOOSER_MODE_SINGLE) {
      mListview.setOnItemLongClickListener(this);
      UIHelper.toast(this, R.string.chooser_long_press_message);
    } else {
      mListview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
      mListview.setMultiChoiceModeListener(this);
    }
    mListview.setOnItemClickListener(this);
    fill(mCurrentDir);
    mHandler = new IncomingHandler(this);
  }

  /**
   * Confirm dialog.
   * @param o The selected options.
   */
  private void confirm(final List<FileChooserOption> o) {
    UIHelper.showConfirmDialog(
        this,
        mConfirmTitle, mConfirmMessage,
        (view) -> onFilesSelected(o));
  }


  /**
   * Called when the options item is clicked (cancel).
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == R.id.action_cancel) {
      cancel();
    }
    return false;
  }

  /**
   * Called when the options menu is clicked.
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.filechooser_menu, menu);
    return true;
  }

  /**
   * Called when files zre selected.
   * @param opts The file chooser options.
   */
  private void onFilesSelected(final List<FileChooserOption> opts) {
    mProgress = UIHelper.showProgressDialog(this, R.string.loading);
    mProgress.show();
    // useful code, variables declarations...
    new Thread((() -> {
      // starts the first long operation
      runOnUiThread(() -> {
        Message msg;
        ErrorStatus status = doComputeHandler(opts);
        if (status == ErrorStatus.CANCEL) {
          msg = mHandler.obtainMessage(MSG_CANCEL, this);
          // sends the message to our handler
          mHandler.sendMessage(msg);
        } else if (status != ErrorStatus.NO_ERROR) {
          // error management, creates an error message
          msg = mHandler.obtainMessage(MSG_ERR, this);
          // sends the message to our handler
          mHandler.sendMessage(msg);
        } else {
          msg = mHandler.obtainMessage(MSG_OK, this);
          // sends the message to our handler
          mHandler.sendMessage(msg);
        }
      });
    })).start();
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    File parent = mCurrentDir.getParentFile();
    if (parent == null || parent.equals(mDefaultDir.getParentFile())) {
      UIHelper.closeTransition(this);
      cancel();
    } else {
      mCurrentDir = parent;
      fill(mCurrentDir);
    }
  }

  /**
   * Cancel the file chooser.
   */
  private void cancel() {
    finish();
  }

  /**
   * Called when the activity is destroyed.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    cancel();
  }

  /**
   * Compute the the response.
   * @param userObject The user object.
   * @return ErrorStatus
   */
  public ErrorStatus doComputeHandler(final List<FileChooserOption> userObject) {
    mOpts = userObject;
    if (mOpts == null)
      return ErrorStatus.CANCEL; /* cancel action */
    if (!isMountedSdcard())
      return ErrorStatus.ERROR_NOT_MOUNTED;
    return ErrorStatus.NO_ERROR;
  }

  /**
   * Handle a success response.
   */
  public void onSuccessHandler() {
    final Intent returnIntent = new Intent();
    int result = RESULT_CANCELED;
    if (mOpts != null) {
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < mOpts.size(); i++) {
        sb.append(mOpts.get(i).getPath());
        if(i < mOpts.size() - 1)
          sb.append(FILECHOOSER_MULTI_SEPARATOR);
      }
      returnIntent.putExtra(FILECHOOSER_SELECTION_KEY, sb.toString());
      if(getUserMessage() != null)
        returnIntent.putExtra(FILECHOOSER_USER_MESSAGE, getUserMessage());
      result = RESULT_OK;
    }
    setResult(result, returnIntent);
    mOpts = null;
    cancel();
  }

  /**
   * Handle a cancel request.
   */
  public void onCancelHandler() {

  }

  /**
   * Handle an error.
   */
  public void onErrorHandler() {
    mOpts = null;
    final Intent returnIntent = new Intent();
    setResult(RESULT_CANCELED, returnIntent);
    onBackPressed();
  }

  /**
   * Tests id the sdcard is mounted.
   * @return boolean
   */
  private boolean isMountedSdcard() {
    final String state = Environment.getExternalStorageState();
    return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
  }


  private static class IncomingHandler extends Handler {

    private FileChooserActivity adaptee = null;

    private IncomingHandler(FileChooserActivity adaptee) {
      this.adaptee = adaptee;
    }

    @Override
    public void handleMessage(final Message msg) {
      switch (msg.what) {
        case MSG_ERR:
          final String err = "Activity compute failed !";
          if (adaptee.mProgress.isShowing())
            adaptee.mProgress.dismiss();
          Toast.makeText(adaptee, err, Toast.LENGTH_SHORT).show();
          adaptee.onErrorHandler();
          break;
        case MSG_OK:
          if (adaptee.mProgress.isShowing())
            adaptee.mProgress.dismiss();
          adaptee.onSuccessHandler();
          break;
        case MSG_CANCEL:
          if (adaptee.mProgress.isShowing()) adaptee.mProgress.dismiss();
          adaptee.onCancelHandler();
          break;
        default: // should never happen
          break;
      }
    }
  }
  /**
   * Fill the list.
   * @param f The new root folder.
   */
  @SuppressWarnings("deprecation")
  protected void fill(final File f) {
    final File[] dirs = f.listFiles();
    this.setTitle(f.getAbsolutePath());
    final List<FileChooserOption> dir = new ArrayList<>();
    final List<FileChooserOption> fls = new ArrayList<>();
    int ic_folder = UIHelper.getResIdFromAttribute(this, R.attr.ic_folder);
    int ic_file = UIHelper.getResIdFromAttribute(this, R.attr.ic_file);
    try {
      for (final File ff : dirs) {
        if (ff.isDirectory())
          dir.add(new FileChooserOption(ff.getName(), getString(R.string.chooser_folder), ff.getAbsolutePath(), ContextCompat.getDrawable(this, ic_folder), false));
        else if(mShow != FILECHOOSER_SHOW_DIRECTORY_ONLY) {
          if(isFiltered(ff)) {
            fls.add(new FileChooserOption(ff.getName(), getString(R.string.chooser_file_size) + ": " + getSizeToHuman(ff.length()), ff
                .getAbsolutePath(), ContextCompat.getDrawable(this, ic_file), mPreview));
          }
        }
      }
    } catch (final Exception e) {
      Log.e(getClass().getSimpleName(), "Exception : " + e.getMessage(), e);
    }
    Collections.sort(dir);
    if(!fls.isEmpty()){
      Collections.sort(fls);
      dir.addAll(fls);
    }
    dir.add(
        0,
        new FileChooserOption("..", getString(R.string.chooser_parent_directory), f.getParent(), ContextCompat.getDrawable(this, ic_folder), false));
    mAdapter = new FileChooserArrayAdapter(this, R.layout.file_view, dir);
    mListview.setAdapter(mAdapter);
  }

  /**
   * Converts the value to human representation.
   * @param length The value to convert.
   * @return String
   */
  private String getSizeToHuman(long length) {
    String sf;
    if (length < 1000) {
      sf = String.format(Locale.US, "%d octet%s", length, length > 1 ? "s" : "");
    } else if (length < 1000000)
      sf = String.format(Locale.US, "%.02f", ((float) length / (float)SIZE_1KB)) + " Ko";
    else if (length < 1000000000)
      sf = String.format(Locale.US, "%.02f", ((float) length / (float)SIZE_1MB)) + " Mo";
    else
      sf = String.format(Locale.US, "%.02f", ((float) length / (float)SIZE_1GB)) + " Go";
    return sf;
  }

  /**
   * Tests if the input file is in the filter list.
   * @param file The file to test.
   * @return boolean
   */
  public boolean isFiltered(final File file) {
    StringTokenizer token = new StringTokenizer(mFileFilter, ",");
    while(token.hasMoreTokens()) {
      String filter = token.nextToken().toLowerCase();
      if(filter.equals("*")) return true;
      if(file.getName().toLowerCase().endsWith("." + filter)) return true;
    }
    return false;
  }

  /**
   * Called when an item is clicked.
   * @param l The AdapterView.
   * @param v The clicked view.
   * @param position The position in the AdapterView.
   * @param id Not used.
   */
  @Override
  public void onItemClick(final AdapterView<?> l, final View v,
                          final int position, final long id) {
    final FileChooserOption o = mAdapter.getItem(position);
    if (o == null || o.getPath() == null)
      return;
    if (o.getData().equalsIgnoreCase(getString(R.string.chooser_folder))
        || o.getData().equalsIgnoreCase(getString(R.string.chooser_parent_directory))) {
      mCurrentDir = new File(o.getPath());
      fill(mCurrentDir);
    } else if (mType == FILECHOOSER_TYPE_FILE_ONLY /*&& mMode == FILECHOOSER_MODE_SINGLE*/) {
      confirm(Collections.singletonList(o));
    }
  }

  public final boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
    final FileChooserOption o = mAdapter.getItem(position);
    if (o == null || o.getPath() == null)
      return false;
    boolean folder = false;
    if (o.getData().equalsIgnoreCase(getString(R.string.chooser_folder)))
      folder = true;
    if (!o.getData().equalsIgnoreCase(getString(R.string.chooser_parent_directory))) {
      if (folder && mType == FILECHOOSER_TYPE_FILE_ONLY)
        return false;
      if (!folder && mType == FILECHOOSER_TYPE_DIRECTORY_ONLY)
        return false;
      confirm(Collections.singletonList(o));
    }
    return true;
  }

  /**
   * Returns the user message.
   * @return String
   */
  public String getUserMessage() {
    return mUserMessage;
  }

  /**
   * Called when an item is checked or unchecked during selection mode.
   * @param mode The ActionMode providing the selection mode.
   * @param position Adapter position of the item that was checked or unchecked.
   * @param id Adapter ID of the item that was checked or unchecked.
   * @param checked true if the item is now checked, false if the item is now unchecked.
   */
  @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
    FileChooserOption fco = mAdapter.getItem(position);
    if(fco != null && new File(fco.getPath()).isDirectory()) {
      mode.finish();
      return;
    }
    final int checkedCount = mListview.getCheckedItemCount();
    mode.setTitle(checkedCount + " Selected");
    mAdapter.toggleSelection(position);
  }

  /**
   * Called to report a user click on an action button.
   * @param mode The current ActionMode.
   * @param item The item that was clicked.
   * @return true if this callback handled the event, false if the standard MenuItem invocation should continue.
   */
  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_clear:
        mAdapter.removeSelection();
        mode.finish();
        return true;
      default:
        return false;
    }
  }

  /**
   * Called when action mode is first created. The menu supplied will be used to generate action buttons for the action mode.
   * @param mode ActionMode being created.
   * @param menu Menu used to populate action buttons.
   * @return true if the action mode should be created, false if entering this mode should be aborted.
   */
  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    mode.getMenuInflater().inflate(R.menu.filechooser_menu_action, menu);
    return true;
  }

  /**
   * Called when an action mode is about to be exited and destroyed.
   * @param mode The current ActionMode being destroyed.
   */
  @Override
  public void onDestroyActionMode(ActionMode mode) {
    if(mAdapter.getSelectedCount() != 0) {
      SparseBooleanArray selected = mAdapter.getSelectedIds();
      List<FileChooserOption> options = new ArrayList<>();
      for (int i = 0; i < selected.size(); i++) {
        if (selected.valueAt(i)) {
          options.add(mAdapter.getItem(selected.keyAt(i)));
        }
      }
      onFilesSelected(options);
    } else
      mAdapter.removeSelection();
  }

  /**
   * Called to refresh an action mode's action menu whenever it is invalidated.
   * @param mode ActionMode being prepared.
   * @param menu Menu used to populate action buttons.
   * @return true if the menu or action mode was updated, false otherwise.
   */
  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }
}
