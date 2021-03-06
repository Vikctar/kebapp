package com.anmark.grund;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SinglePlaceCommentActivity extends Activity {

	private EditText txtCommentValue;
	private DBAdapter db;
	private String placeID;
	private Typeface tf;
	private Button buttonSaveComment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_place_comment);

		// application font
		tf = Typeface.createFromAsset(getAssets(), "fonts/slapstick.ttf");

		Intent i = getIntent();
		placeID = i.getStringExtra("placeID");

		txtCommentValue = (EditText) findViewById(R.id.Single_Place_CommentValue);
		buttonSaveComment = (Button) findViewById(R.id.buttonSaveComment);
		txtCommentValue.setTypeface(tf);
		buttonSaveComment.setTypeface(tf);

		db = new DBAdapter(getApplicationContext());
		db.open();
		txtCommentValue.setText(db.getRow(placeID).getComment());
		db.close();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}
	
	// on button press, save update comment in db 
	public void saveComment(View view) {
		String tempComment = txtCommentValue.getText().toString();
		db.open();
		db.updateRowCommentById(placeID, tempComment);
		db.close();

		Intent returnIntent = new Intent();
		returnIntent.putExtra("placeID", placeID);
		setResult(RESULT_OK, returnIntent);     
		finish();
	}

}
