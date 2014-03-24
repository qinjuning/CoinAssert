package com.coinwtb.coinassistant.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.ui.base.BaseActivity;
import com.coinwtb.coinassistant.utils.Utils;

public class AboutActivity extends BaseActivity implements OnClickListener{

	// 钱包地址
	private int[] mCoinAddresRes = new int[] {
			R.id.tv_btc_address, R.id.tv_ltc_address, R.id.tv_ifc_address,
	};
	
	private int[] mCopyRes = new int[] {
			R.id.btn_btc_copy, R.id.btn_ltc_copy, R.id.btn_ifc_copy,
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setHeaderTitle(R.string.about_title);
		
		for (int i = 0; i < mCopyRes.length; i++) {
			findViewById(mCopyRes[i]).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < mCopyRes.length; i++) {
			if (mCopyRes[i] == v.getId() ) {
				TextView tv = (TextView) findViewById(mCoinAddresRes[i]);
				copyToClipboard(tv.getText().toString());
			}
		}
	}
	
	// 复制到剪切板管理器
	public void copyToClipboard(String content) {  
		ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);  
		cmb.setText(content.trim());  
		Utils.showShortToast(getBaseContext(), R.string.coin_address_clipborad);
	}  
}
