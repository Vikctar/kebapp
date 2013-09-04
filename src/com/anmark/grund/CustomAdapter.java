package com.anmark.grund;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ArrayAdapter;

	class CustomAdapter extends ArrayAdapter<CharSequence>{

		Context context; 
		int layoutResourceId;    
		CharSequence data[] = null;
		Typeface tf; 

		public CustomAdapter(Context context, int layoutResourceId, CharSequence[] data, String FONT ) { 
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
			tf = Typeface.createFromAsset(context.getAssets(), FONT);
		}   

	}