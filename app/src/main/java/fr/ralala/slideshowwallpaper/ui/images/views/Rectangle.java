package fr.ralala.slideshowwallpaper.ui.images.views;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Simple rectangle object.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Rectangle  {
  public int x;
  public int y;
  public int w;
  public int h;

  Rectangle(Rectangle rect) {
    set(rect);
  }

  Rectangle(int x, int y, int w, int h) {
    set(x, y, w, h);
  }

  void set(Rectangle rect) {
    x = rect.x;
    y = rect.y;
    w = rect.w;
    h = rect.h;
  }

  void set(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  void setXY(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    return "Rectangle (" + x + ", " + y + ", " + w + ", " + h + ")";
  }
}
