package fr.ralala.slideshowwallpaper.ui.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import fr.ralala.slideshowwallpaper.R;
import fr.ralala.slideshowwallpaper.sql.Image;

/**
 *******************************************************************************
 * <p><b>Project Slideshow Wallpaper</b><br/>
 * Gridview adapter.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class GridViewAdapter extends ArrayAdapter<Image> {

  private Context mContext;
  private int mLayoutResourceId;
  private List<Image> mData;

  GridViewAdapter(Context context, int layoutResourceId, List<Image> data) {
    super(context, layoutResourceId, data);
    mLayoutResourceId = layoutResourceId;
    mContext = context;
    mData = data;
  }

  /**
   * Returns the current view.
   * @param position The view position.
   * @param convertView The view to convert.
   * @param parent The parent.
   * @return The new view.
   */
  @Override
  public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
    View row = convertView;
    ViewHolder holder;

    if (row == null) {
      LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
      row = inflater.inflate(mLayoutResourceId, parent, false);
      holder = new ViewHolder();
      holder.llItem = row.findViewById(R.id.llItem);
      holder.image = row.findViewById(R.id.image);
      row.setTag(holder);
    } else {
      holder = (ViewHolder) row.getTag();
    }
    Image item = mData.get(position);

    holder.image.setImageBitmap(Bitmap.createScaledBitmap(item.getBitmap(), 100, 100, true));
    holder.llItem.setBackgroundResource(item.isScrollable() ? R.drawable.grid_color_selector_scrollable : R.drawable.grid_color_selector);
    return row;
  }

  /**
   * Tests if the image is already present.
   * @param imageName The image name.
   * @return boolean
   */
  boolean containsName(String imageName) {
    for(int i = 0; i < mData.size(); i++)
      if(mData.get(i).getName().equals(imageName))
        return true;
    return false;
  }

  static class ViewHolder {
    LinearLayout llItem;
    ImageView image;
  }
}