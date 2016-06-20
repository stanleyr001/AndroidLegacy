package com.topicplaces.android;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Created by Stanley on 09/18/15.
 */
public class AttributesAdapter implements ListAdapter {

    private Context c;
    private String[] options0;
    private String[] options1;

    public AttributesAdapter(Context c, String[] options0, String[] options1) {
        this.c = c;
        this.options0 = options0;
        this.options1 = options1;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return options0.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View list;
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        list = new View(c);
        list = inflater.inflate(R.layout.options_listview, null);
        TextView textView0 = (TextView) list.findViewById(R.id.options_textview_0);
        TextView textView1 = (TextView) list.findViewById(R.id.options_textview_1);

        Log.d("POSISTION", "" + position);

        textView0.setText(options0[position]);
        textView1.setText(options1[position]);

        return list;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return options0.length;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
