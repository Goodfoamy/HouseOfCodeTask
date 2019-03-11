package houseofcode.chatapplication;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<DataItem> {

   private Context context;
   private int layoutResourceId;
   private List<DataItem> dataItems;


    public CustomAdapter(Context context, int resource, List<DataItem> objects) {
        super(context, resource, objects);

        this.context = context;
        this.layoutResourceId = resource;
        this.dataItems = objects;
    }

    static class DataHolder {

        ImageView ivIcon;
        TextView roomName;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DataHolder dataHolder = null;

        if(convertView == null) {

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();

            convertView = inflater.inflate(layoutResourceId, parent, false);

            dataHolder = new DataHolder();

            dataHolder.ivIcon = (ImageView)convertView.findViewById(R.id.rowIcon);
            dataHolder.roomName = (TextView) convertView.findViewById(R.id.rowTextView);

            convertView.setTag(dataHolder);
        } else {
            dataHolder = (DataHolder) convertView.getTag();
        }

        DataItem dataItem = dataItems.get(position);

        dataHolder.roomName.setText(dataItem.roomName);
        dataHolder.ivIcon.setImageResource(dataItem.resIdIcon);

        return convertView;
    }
}
