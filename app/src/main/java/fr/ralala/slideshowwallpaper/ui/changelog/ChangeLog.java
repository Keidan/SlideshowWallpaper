package fr.ralala.slideshowwallpaper.ui.changelog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.WebView;

/**
 * Copyright (C) 2011-2013, Karsten Priegnitz
 *
 * Permission to use, copy, modify, and distribute this piece of software
 * for any purpose with or without fee is hereby granted, provided that
 * the above copyright notice and this permission notice appear in the
 * source code of all copies.
 *
 * It would be appreciated if you mention the author in your change log,
 * contributors list or the like.
 *
 * author: Karsten Priegnitz
 * see: http://code.google.com/p/android-change-log/
 */
public class ChangeLog {
  private static final String TAG = "ChangeLog";
  // this is the key for storing the version name in SharedPreferences
  private static final String VERSION_KEY = "PREFS_VERSION_KEY";
  private static final String NO_VERSION  = "";
  private Configuration mConfiguration = null;
  private final Context mContext;
  private String mLastVersion;
  private String mThisVersion;


  /**
   * Constructor
   *
   * Retrieves the version names and stores the new version name in
   * SharedPreferences.
   *
   * @param configuration
   *          The configuration.
   * @param context
   *          The android context.
   *
   */
  public ChangeLog(final Configuration configuration, final Context context) {
    this(configuration, context, PreferenceManager.getDefaultSharedPreferences(context));
  }

  /**
   * Constructor
   *
   * Retrieves the version names and stores the new version name in
   * SharedPreferences.
   *
   * @param configuration
   *          The configuration.
   * @param context
   *          The android context.
   * @param sp
   *          The shared preferences to store the last version name into.
   */
  private ChangeLog(final Configuration configuration, final Context context,
                    final SharedPreferences sp) {
    mContext = context;
    mConfiguration = configuration;

    // get version numbers
    mLastVersion = sp.getString(VERSION_KEY, NO_VERSION);
    Log.d(TAG, "lastVersion: " + mLastVersion);
    try {
      mThisVersion = context.getPackageManager().getPackageInfo(
          context.getPackageName(), 0).versionName;
    } catch (final NameNotFoundException e) {
      mThisVersion = NO_VERSION;
      Log.e(TAG, "could not get version name from manifest!");
      e.printStackTrace();
    }
    Log.d(TAG, "appVersion: " + mThisVersion);
  }

  /**
   * Returns <code>true</code> if this version of your app is started the first time.
   * @return boolean
   */
  public boolean firstRun() {
    return !mLastVersion.equals(mThisVersion);
  }

  /**
   * Returns <code>true</code> if your app including ChangeLog is started the first time ever.
   * Also <code>true</code> if your app was de-installed and installed again.
   * @return boolean
   */
  private boolean firstRunEver() {
    return NO_VERSION.equals(mLastVersion);
  }

  /**
   * Returns an AlertDialog displaying the changes since the previous installed version of your app (what's new).
   * But when this is the first run of your app including ChangeLog then the full log dialog is show.
   * @return AlertDialog
   */
  public AlertDialog getLogDialog() {
    return this.getDialog(this.firstRunEver());
  }

  /**
   * Returns an AlertDialog with a full change log displayed.
   * @return AlertDialog
   */
  public AlertDialog getFullLogDialog() {
    return this.getDialog(true);
  }

  /**
   * Returns an AlertDialog with a full (or not) change log displayed.
   * @param full <code>true</code> for full change log.
   * @return AlertDialog
   */
  private AlertDialog getDialog(final boolean full) {
    final WebView wv = new WebView(mContext);

    wv.setBackgroundColor(Color.parseColor(mContext.getResources().getString(
        mConfiguration.getStringBackgroundColor())));
    wv.loadDataWithBaseURL(null, this.getLog(full), "text/html", "UTF-8", null);

    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    builder
        .setTitle(
            mContext.getResources().getString(
                full ? mConfiguration.getStringChangelogFullTitle() : mConfiguration.getStringChangelogTitle()))
        .setView(wv)
        .setCancelable(false)
        // OK button
        .setPositiveButton(
            mContext.getResources().getString(mConfiguration.getStringChangelogOkButton()),
            (dialog, which) -> updateVersionInPreferences());

    if (!full) {
      // "more ..." button
      builder.setNegativeButton(mConfiguration.getStringChangelogShowFull(),
          (dialog, which) -> getFullLogDialog().show());
    }

    return builder.create();
  }

  /**
   * Updates the version in preferences.
   */
  private void updateVersionInPreferences() {
    // save new version number to preferences
    final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
    final SharedPreferences.Editor editor = sp.edit();
    editor.putString(VERSION_KEY, mThisVersion);
    editor.apply();
  }

  /** modes for HTML-Lists (bullet, numbered) */
  private enum ListMode {
    NONE, ORDERED, UNORDERED,
  }

  private ListMode listMode = ListMode.NONE;
  private StringBuffer sb = null;
  private static final String EOCL = "END_OF_CHANGE_LOG";

  private String getLog(final boolean full) {
    // read changelog.txt file
    sb = new StringBuffer();
    try {
      final InputStream ins = mContext.getResources().openRawResource(
          mConfiguration.getRawChangelog());
      final BufferedReader br = new BufferedReader(new InputStreamReader(ins));

      String line;
      boolean advanceToEOVS = false; // if true: ignore further version
      // sections
      while ((line = br.readLine()) != null) {
        line = line.trim();
        final char marker = line.length() > 0 ? line.charAt(0) : 0;
        if (marker == '$') {
          // begin of a version section
          this.closeList();
          final String version = line.substring(1).trim();
          // stop output?
          if (!full) {
            if (mLastVersion.equals(version)) {
              advanceToEOVS = true;
            } else if (version.equals(EOCL)) {
              advanceToEOVS = false;
            }
          }
        } else if (!advanceToEOVS) {
          switch (marker) {
            case '%':
              // line contains version title
              this.closeList();
              sb.append("<div class='title'>").append(line.substring(1).trim()).append("</div>\n");
              break;
            case '_':
              // line contains version title
              this.closeList();
              sb.append("<div class='subtitle'>").append(line.substring(1).trim()).append("</div>\n");
              break;
            case '!':
              // line contains free text
              this.closeList();
              sb.append("<div class='freetext'>").append(line.substring(1).trim()).append("</div>\n");
              break;
            case '#':
              // line contains numbered list item
              this.openList(ListMode.ORDERED);
              sb.append("<li>").append(line.substring(1).trim()).append("</li>\n");
              break;
            case '*':
              // line contains bullet list item
              this.openList(ListMode.UNORDERED);
              sb.append("<li>").append(line.substring(1).trim()).append("</li>\n");
              break;
            default:
              // no special character: just use line as is
              this.closeList();
              sb.append(line).append("\n");
          }
        }
      }
      this.closeList();
      br.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return sb.toString();
  }

  private void openList(final ListMode listMode) {
    if (this.listMode != listMode) {
      closeList();
      if (listMode == ListMode.ORDERED) {
        sb.append("<div class='list'><ol>\n");
      } else if (listMode == ListMode.UNORDERED) {
        sb.append("<div class='list'><ul>\n");
      }
      this.listMode = listMode;
    }
  }

  private void closeList() {
    if (this.listMode == ListMode.ORDERED) {
      sb.append("</ol></div>\n");
    } else if (this.listMode == ListMode.UNORDERED) {
      sb.append("</ul></div>\n");
    }
    this.listMode = ListMode.NONE;
  }
}