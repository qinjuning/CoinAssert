package com.coinwtb.coinassistant.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;

public class MeetingDialog extends Dialog {

	private FrameLayout llContentPanel;
	private View llContainer;
	private Button mBtOk;
	private Button mBtCancel;
	private TextView mTvTitle;
	private TextView mTvMessage;
	private View customView;

	private View.OnClickListener mPostiveClickListener = null;
	private View.OnClickListener mNegativeClickListener = null;

	private CharSequence mTitle;
	private CharSequence mMessage;
	private CharSequence mOKTitle;
	private CharSequence mCancelTitle;
	
	public MeetingDialog(Context context) {
		super(context);
	}

	public MeetingDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hollcrm_meeting_dialog);

		llContentPanel = (FrameLayout) getWindow().findViewById(
				R.id.customPanel);
		mBtOk = (Button) getWindow().findViewById(R.id.btOk);
		mBtCancel = (Button) getWindow().findViewById(R.id.btCancel);
		llContainer = findViewById(R.id.llContainer);

		mTvTitle = (TextView) getWindow().findViewById(R.id.tvMeetTitle);
		mTvMessage = (TextView) getWindow().findViewById(R.id.tvMessage);
		if (mTitle != null){
			mTvTitle.setText(mTitle);
		}
		if(mMessage != null){
			mTvMessage.setText(mMessage);
		}
		if(mOKTitle != null){
			mBtOk.setText(mOKTitle);
		}
		if(mCancelTitle != null){
			mBtCancel.setText(mCancelTitle);
		}
		// Hidden it if both of buttons are not setted
		if (mPostiveClickListener == null && mNegativeClickListener == null) {
			llContainer.setVisibility(View.GONE);
		} else {
			if (mPostiveClickListener != null) {
				mBtOk.setVisibility(View.VISIBLE);
				mBtOk.setOnClickListener(mPostiveClickListener);
			}
			else {
				mBtOk.setVisibility(View.GONE);
			}
			
			if (mNegativeClickListener != null) {
				mBtCancel.setVisibility(View.VISIBLE);
				mBtCancel.setOnClickListener(mNegativeClickListener);
			} else {
				mBtCancel.setVisibility(View.GONE);
			}
		}

		if (customView != null) {
			mTvMessage.setVisibility(View.GONE);
			FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT,
					ViewGroup.MarginLayoutParams.MATCH_PARENT);
			ll.gravity = Gravity.CENTER;
			llContentPanel.addView(customView, ll);
		}
		getWindow().getDecorView().setBackgroundDrawable(null);
	}

	/**
	 * Set the Listener and make this view at visible state
	 * 
	 * @param listener
	 */
	public void setPostiveClickListener( View.OnClickListener listener) {
		mPostiveClickListener = listener;
	}

	/**
	 * Set the Listener and make this view at visible state
	 * 
	 * @param listener
	 */
	public void setNegativeClickListener(View.OnClickListener listener) {
		mNegativeClickListener = listener;
	}
	
	/**
	 * Set the Listener and make this view at visible state
	 * 
	 * @param listener
	 */
	public void setPostiveClickListener(CharSequence title, View.OnClickListener listener) {
		this.mOKTitle = title;
		mPostiveClickListener = listener;
	}

	/**
	 * Set the Listener and make this view at visible state
	 * 
	 * @param listener
	 */
	public void setNegativeClickListener(CharSequence title, View.OnClickListener listener) {
		this.mCancelTitle = title;
		mNegativeClickListener = listener;
	}

	public void setView(View view) {
		customView = view;
	}

	public void setTitle(CharSequence title) {
		this.mTitle = title;
	}
	
	public void setMessage(CharSequence message) {
		this.mMessage = message;
		if(mTvMessage != null){
			mTvMessage.setText(mMessage);
		}
	}
	
	public void setMessage(int resId) {
		mTvMessage.setText(resId);
	}
	

}