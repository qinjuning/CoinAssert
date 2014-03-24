package com.coinwtb.coinassistant.fragment;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.Slog;

public class CoinWaringPagerAdapter extends FragmentPagerAdapter{
	
	private static final String TAG = "CoinWaringPagerAdapter";
	
	/**
	 * 只能设置5个预警货币
	 */
	public static final int COIN_WARING_SIZE = 5;  
	
	private List<CoinWaringFragment> mCacheFragmets = new ArrayList<CoinWaringFragment>();
	
	public CoinWaringPagerAdapter(FragmentManager fm, List<CoinBean> coinList) {
		super(fm);
	}
	
	@Override
	public Fragment getItem(int position) {
		CoinWaringFragment coinWaringFragment = new CoinWaringFragment(position) ;
		mCacheFragmets.add(coinWaringFragment);
		return coinWaringFragment;
	}
	
	@Override
	public int getCount() {
		return COIN_WARING_SIZE;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
	}
	
	public int getSelectedItem(int coinId) {
		Slog.v(TAG, " size  = " +  mCacheFragmets.size());
		for (int  i = 0; i < mCacheFragmets.size(); i++) {
			Slog.v(TAG, "CoinBean = " + mCacheFragmets.get(i).getCoinBean());
			CoinBean coinBean = mCacheFragmets.get(i).getCoinBean();
			if (coinBean != null && coinBean.getId() == coinId) {
				return i;
			}
		}
		return -1;
	}
	
}
