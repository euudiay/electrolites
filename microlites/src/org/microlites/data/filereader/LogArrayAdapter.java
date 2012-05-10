package org.microlites.data.filereader;

import org.microlites.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LogArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	private final String[] sizes;

	public LogArrayAdapter(Context context, String[] values, String[] sizes) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		this.values = values;
		this.sizes = sizes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		textView.setText(values[position]);
		textView = (TextView) rowView.findViewById(R.id.sizeLabel);
		textView.setText(sizes[position]);
		imageView.setImageResource(R.drawable.ecg_icon);

		return rowView;
	}
}