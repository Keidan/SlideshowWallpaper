package fr.ralala.slideshowwallpaper.ui.changelog;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Changelog configuration.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Configuration {
  private int mStringBackgroundColor    = 0;
  private int mStringChangelogTitle     = 0;
  private int mStringChangelogFullTitle = 0;
  private int mStringChangelogShowFull  = 0;
  private int mStringChangelogOkButton  = 0;
  private int mRawChangelog             = 0;

  public Configuration(final int rawChangelog, final int stringChangelogOkButton, final int stringBackgroundColor,
                      final int stringChangelogTitle, final int stringChangelogFullTitle,
                      final int stringChangelogShowFull) {
    mStringChangelogOkButton = stringChangelogOkButton;
    mStringBackgroundColor = stringBackgroundColor;
    mStringChangelogTitle = stringChangelogTitle;
    mStringChangelogFullTitle = stringChangelogFullTitle;
    mStringChangelogShowFull = stringChangelogShowFull;
    mRawChangelog = rawChangelog;
  }

  /**
   * Returns the String resource for the label of the OK button.
   * @return int
   */
  int getStringChangelogOkButton() {
    return mStringChangelogOkButton;
  }

  /**
   * Returns the String resource for the background.
   * @return int
   */
  int getStringBackgroundColor() {
    return mStringBackgroundColor;
  }

  /**
   * Returns the String resource for the title.
   * @return int
   */
  int getStringChangelogTitle() {
    return mStringChangelogTitle;
  }

  /**
   * Returns the String resource for the full title.
   * @return int
   */
  int getStringChangelogFullTitle() {
    return mStringChangelogFullTitle;
  }

  /**
   * Returns the String resource for show full field.
   * @return int
   */
  int getStringChangelogShowFull() {
    return mStringChangelogShowFull;
  }


  /**
   * Returns the resource id of the changelog file.
   * @return int
   */
  int getRawChangelog() {
    return mRawChangelog;
  }
}
