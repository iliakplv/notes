package com.iliakplv.notes.gui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Autor: Ilya Kopylov
 * Date:  30.09.2013
 */
public class NoteDetailsEditText extends EditText {

	public NoteDetailsEditText(Context context) {
		super(context);
	}

	public NoteDetailsEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoteDetailsEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}



	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

			return true;
		}
		return super.dispatchKeyEvent(event);
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	public interface NoteDetailsEditTextBackPressListener {

	}
}
