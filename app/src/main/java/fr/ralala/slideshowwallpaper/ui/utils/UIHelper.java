package fr.ralala.slideshowwallpaper.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import fr.ralala.slideshowwallpaper.R;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * UI Helper functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class UIHelper {

  /**
   * Returns the res ID from the attributes.
   * @param activity The context Activity.
   * @param attr The attribute ID.
   * @return int
   */
  public static int getResIdFromAttribute(final Activity activity, final int attr) {
    if(attr==0)
      return 0;
    final TypedValue typedvalueattr = new TypedValue();
    activity.getTheme().resolveAttribute(attr,typedvalueattr,true);
    return typedvalueattr.resourceId;
  }

  /**
   * Starts open transition.
   * @param a Activity.
   */
  public static void openTransition(Activity a) {
    a.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
  }

  /**
   * Starts close transition.
   * @param a Activity.
   */
  public static void closeTransition(Activity a) {
    a.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
  }

  /**
   * Displays a progress dialog.
   * @param context The Android context.
   * @param message The progress message.
   * @return AlertDialog
   */
  public static AlertDialog showProgressDialog(Context context, int message) {
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    final ViewGroup nullParent = null;
    View view = layoutInflater.inflate(R.layout.progress_dialog, nullParent);
    AlertDialog progress = new AlertDialog.Builder(context).create();
    TextView tv = view.findViewById(R.id.text);
    tv.setText(message);
    progress.setCancelable(false);
    progress.setView(view);
    return progress;
  }



  /**
   * Displays a confirm dialog.
   * @param c The Android context.
   * @param title The dialog title.
   * @param message The dialog message.
   * @param yes Listener used when the 'yes' button is clicked.
   */
  public static void showConfirmDialog(final Context c, String title,
                                       String message, final View.OnClickListener yes) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setCancelable(false);
    builder.setIcon(R.mipmap.ic_launcher);
    if(title != null)
      builder.setTitle(title);
    builder.setMessage(message);
    builder.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
      if(yes != null) yes.onClick(null);
    });
    builder.setNegativeButton(android.R.string.no, (dialog, whichButton) -> {

    });
    builder.show();
  }

  /**
   * Displays a toast.
   * @param c The Android context.
   * @param message The toast message.
   */
  public static void toast(final Context c, final String message) {
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
    TextView tv = toast.getView().findViewById(android.R.id.message);
    if (null!=tv) {
      Drawable drawable = ContextCompat.getDrawable(c, R.mipmap.ic_launcher);
      if(drawable != null) {
        final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
        tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
        tv.setCompoundDrawablePadding(5);
      }
    }
    toast.show();
  }

  /**
   * Displays a long toast.
   * @param c The Android context.
   * @param message The toast message.
   */
  public static void toast(final Context c, final int message) {
    toast(c, c.getString(message));
  }


  /**
   * Displays an alert dialog.
   * @param c The Android context.
   * @param title The alert dialog title.
   * @param message The alert dialog message.
   */
  public static void showAlertDialog(final Context c, final int title, final String message) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(message);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
    alertDialog.show();
  }
  /**
   * Displays an alert dialog.
   * @param c The Android context.
   * @param title The alert dialog title.
   * @param message The alert dialog message.
   */
  public static void showAlertDialog(final Context c, final int title, final int message) {
    showAlertDialog(c, title, c.getString(message));
  }

}

