package fr.ralala.slideshowwallpaper.ui.chooser;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
public abstract class AbstractFileChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
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
  public static final int FILECHOOSER_TYPE_FILE_ONLY = 0;
  public static final int FILECHOOSER_TYPE_DIRECTORY_ONLY  = 1;
  public static final int FILECHOOSER_TYPE_FILE_AND_DIRECTORY = 2;
  public static final int FILECHOOSER_SHOW_DIRECTORY_ONLY = 1;
  public static final int FILECHOOSER_SHOW_FILE_AND_DIRECTORY = 2;
  public static final String FILECHOOSER_FILE_FILTER_ALL = "*";
  protected File mCurrentDir = null;
  protected File mDefaultDir = null;
  private FileChooserArrayAdapter mAdapter = null;
  private String mConfirmMessage = null;
  private String mConfirmTitle = null;
  private String mUserMessage = null;
  private String mFileFilter = FILECHOOSER_FILE_FILTER_ALL;
  private int mType = FILECHOOSER_TYPE_FILE_AND_DIRECTORY;
  private int mShow = FILECHOOSER_SHOW_FILE_AND_DIRECTORY;
  private ListView mListview = null;


  /**
   * Confirm dialog.
   * @param o The selected option.
   */
  private void confirm(final FileChooserOption o) {
    UIHelper.showConfirmDialog(
        this,
        mConfirmTitle, mConfirmMessage + "\n" + o.getPath(),
        (view) -> onFileSelected(o));
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
    if (b != null && b.containsKey(FILECHOOSER_TYPE_KEY))
      mType = Integer.parseInt(b.getString(FILECHOOSER_TYPE_KEY));
    if (b != null && b.containsKey(FILECHOOSER_TITLE_KEY))
      mConfirmTitle = b.getString(FILECHOOSER_TITLE_KEY);
    if (b != null && b.containsKey(FILECHOOSER_MESSAGE_KEY))
      mConfirmMessage = b.getString(FILECHOOSER_MESSAGE_KEY);
    if (b != null && b.containsKey(FILECHOOSER_SHOW_KEY))
      mShow = Integer.parseInt(b.getString(FILECHOOSER_SHOW_KEY));
    if (b != null && b.containsKey(FILECHOOSER_FILE_FILTER_KEY))
      mFileFilter = b.getString(FILECHOOSER_FILE_FILTER_KEY);
    if (b != null && b.containsKey(FILECHOOSER_DEFAULT_DIR))
      mDefaultDir = new File(""+b.getString(FILECHOOSER_DEFAULT_DIR));
    if (b != null && b.containsKey(FILECHOOSER_USER_MESSAGE))
      mUserMessage = b.getString(FILECHOOSER_USER_MESSAGE);
    if(mConfirmTitle == null) mConfirmTitle = "title";
    if(mConfirmMessage == null) mConfirmMessage = "message";
    mCurrentDir = mDefaultDir;
    mListview.setLongClickable(true);
    mListview.setOnItemLongClickListener(this);
    mListview.setOnItemClickListener(this);
    fill(mCurrentDir);
    UIHelper.toast(this, R.string.chooser_long_press_message);
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
          dir.add(new FileChooserOption(ff.getName(), getString(R.string.chooser_folder), ff.getAbsolutePath(), ContextCompat.getDrawable(this, ic_folder)));
        else if(mShow != FILECHOOSER_SHOW_DIRECTORY_ONLY) {
          if(isFiltered(ff))
            fls.add(new FileChooserOption(ff.getName(), getString(R.string.chooser_file_size) + ": " + getSizeToHuman(ff.length()), ff
                .getAbsolutePath(), ContextCompat.getDrawable(this, ic_file)));
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
        new FileChooserOption("..", getString(R.string.chooser_parent_directory), f.getParent(), ContextCompat.getDrawable(this, ic_folder)));
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
      String filter = token.nextToken();
      if(filter.equals("*")) return true;
      if(file.getName().endsWith("." + filter)) return true;
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
    } else if (mType == FILECHOOSER_TYPE_FILE_ONLY)
      confirm(o);
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
      confirm(o);
    }
    return true;
  }

  /**
   * Called when a file is selected.
   * @param opt The selected option.
   */
  protected void onFileSelected(final FileChooserOption opt) {
  }

  /**
   * Returns the user message.
   * @return String
   */
  public String getUserMessage() {
    return mUserMessage;
  }
}
