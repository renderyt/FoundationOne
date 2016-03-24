package com.appdroid.workshop.foundationone;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	// Declare class variables
	private LinearLayout titleContainer;
	private TextView titleText;
	private EditText noteText;
	private ImageButton saveButton;
	private ImageButton deleteButton;
	private FloatingActionButton editButton;
	private InputMethodManager imm;

	final String NOTEFILE = "mynote.txt"; // save file name
	final String TITLEFILE = "mytitle.txt";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // set up the layout

		// Get the views inside the layout
		titleContainer = (LinearLayout) findViewById(R.id.title_container);
		titleText = (TextView) findViewById(R.id.title_text);
		noteText = (EditText) findViewById(R.id.note_text);
		saveButton = (ImageButton) findViewById(R.id.save_button);
		editButton = (FloatingActionButton) findViewById(R.id.edit_button);
		deleteButton = (ImageButton) findViewById(R.id.delete_button);

		// Check if there is a saved note in the program
		if (saveExists(NOTEFILE)) {
			// Get save file content
			StringBuffer content = readFile(NOTEFILE);
			StringBuffer title = readFile(TITLEFILE);
			// Show note on screen
			noteText.setText(content);
			titleText.setText(title);
		}

		// Get soft keyboard
		imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

		// Listen for click events on the save button
		titleContainer.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
		editButton.setOnClickListener(this);
	}

	/**
	 * Click event handler for all clickable views
	 *
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.save_button) {
			System.out.println("Save button was pressed");
			saveFile(NOTEFILE, noteText.getText().toString());
			saveFile(TITLEFILE, titleText.getText().toString());
			disableEdit(noteText);
		} else if (v.getId() == R.id.delete_button) {
			System.out.println("Delete button was pressed");
			// Open dialog to confirm deletion
			confirmDeleteDialog(this).show();
		} else if (v.getId() == R.id.title_container) {
			System.out.println("Title container was pressed");
			// Open dialog to edit the note's title
			editTitleDialog(this).show();
		} else if (v.getId() == R.id.edit_button) {
			System.out.println("Edit button was pressed");
			enableEdit(noteText);
		}

	}

	/**
	 * Enables editing and shows keyboard. Hides the floating action button.
	 */
	private void enableEdit(EditText editText) {
		editText.setEnabled(true);
		editText.setSelection(editText.getText().length());
		showKeyboard(editText);
		editButton.hide();
	}

	/**
	 * Disables editing and hides keyboard. Shows the floating action button.
	 */
	private void disableEdit(EditText editText) {
		editText.setEnabled(false);
		hideKeyboard(editText);
		editButton.show();
	}

	/**
	 * Saving file when onPause is called
	 */
	@Override
	public void onPause() {
		super.onPause();
		saveFile(NOTEFILE, noteText.getText().toString());
		saveFile(TITLEFILE, titleText.getText().toString());
	}

	/**
	 * Returns true if a save file exists.
	 *
	 * @param filename
	 * @return
	 */
	private boolean saveExists(String filename) {
		System.out.println("Checking if save file exists");
		boolean result = false;
		File directory = getFilesDir();
		for (File file : directory.listFiles()) {
			if (file.getName().equals(filename)) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Returns the contents of the save file.
	 *
	 * @param filename
	 * @return
	 */
	private StringBuffer readFile(String filename) {
		System.out.println("Reading file");
		FileInputStream inputStream;
		StringBuffer result = new StringBuffer();
		try {
			inputStream = openFileInput(filename);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Writes note to the save file, return true if it succeeds.
	 *
	 * @param filename
	 * @return
	 */
	private void saveFile(String filename, String output) {
		System.out.println("Writing to file");
		boolean result = true;
		FileOutputStream outputStream;
		try {
			outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(output.getBytes());
			outputStream.close();
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		if (result) {
			Toast.makeText(MainActivity.this, titleText.getText().toString() + " has been saved", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.this, titleText.getText().toString() + " failed to save", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Deletes save file and current note
	 */
	private void deleteAll() {
		String oldTitle = titleText.getText().toString();
		deleteFile(NOTEFILE);
		deleteFile(TITLEFILE);
		noteText.setText("");
		titleText.setText("Unnamed");
		Toast.makeText(MainActivity.this, oldTitle + " has been deleted", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Create a dialog for cofirming save delete
	 *
	 * @param activity
	 * @return
	 */
	private Dialog confirmDeleteDialog(final Activity activity) {
		System.out.println("Create delete dialog");
		// Create the delete dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Delete " + titleText.getText().toString());
		builder.setMessage(R.string.delete_dialog_message);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteAll();
			}
		});
		AlertDialog dialog = builder.create();
		return dialog;
	}

	/**
	 * Create a dialog to edit note title
	 *
	 * @param activity
	 * @return
	 */
	private Dialog editTitleDialog(final Activity activity) {
		System.out.println("Create edit dialog");
		// Create a custom view
		LayoutInflater inflater = this.getLayoutInflater();
		final View editView = inflater.inflate(R.layout.edit_title_diaog, null);
		final EditText editTitle = (EditText) editView.findViewById(R.id.edit_title);
		editTitle.setText(titleText.getText().toString());

		// Create the edit title dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(editView);
		builder.setTitle(R.string.edit_dialog_title);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				hideKeyboard(editTitle);
			}
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				hideKeyboard(editTitle);
				String newTitle = editTitle.getText().toString();
				if (newTitle.isEmpty()) {
					titleText.setText("Unnamed");
				} else {
					titleText.setText(editTitle.getText().toString());
				}
			}
		});

		final AlertDialog dialog = builder.create();
		// Click the positive button when the keyboard is done
		editTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
				return true;
			}
		});
		// Keyboard call, required when showing dialogs
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		return dialog;
	}

	/**
	 * For easy call
	 *
	 * @param view
	 */
	private void showKeyboard(View view) {
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	/**
	 * For easy call
	 *
	 * @param view
	 */
	private void hideKeyboard(View view) {
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
