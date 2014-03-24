package com.coinwtb.coinassistant.widget;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.fragment.CoinWaringPagerAdapter;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.widget.slidingmenu.SlidingMenu;

/**
 * 预警管理
 *
 */
public class CoinWarningRelativeLayout extends RelativeLayout{

	private static final String TAG = "CoinWarningRelativeLayout";
	
	private SlidingMenu mSlidingMenu;
	private ViewPager mViewPager;

	private CoinWaringPagerAdapter mCoinWaringPagerAdapter;
	private FlowIndicator mFlowIndicator;
	
	public CoinWarningRelativeLayout(Context context) {
		this(context, null);
	}
	
	public CoinWarningRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CoinWarningRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setSlidingMenu(SlidingMenu slidingMenu) {
		mSlidingMenu = slidingMenu;
	}
	
	public void setSupportManager(FragmentManager fragmentManager) {
		setViewPagerAdapter(fragmentManager);
	}
	
	public void focus() {
		changeSlidingMenuState(mViewPager.getCurrentItem());
	}
	
	public void setSelectedCoinBean(int coinId) {
		int selectedItem = mCoinWaringPagerAdapter.getSelectedItem(coinId);
		Slog.v(TAG, "selectedItem " + selectedItem);
		if (selectedItem != -1) {
			mViewPager.setCurrentItem(selectedItem, false);
		}
	}
	
	@Override
	protected void onFinishInflate() {
	    mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mFlowIndicator = (FlowIndicator) findViewById(R.id.pager_indictor);
	}

	private void setViewPagerAdapter(FragmentManager fragmentManager) {
		mCoinWaringPagerAdapter = new CoinWaringPagerAdapter(fragmentManager, null) {
			
			@Override
			public Fragment getItem(int position) {
				Fragment fragment = super.getItem(position);
				return fragment;
			}
			
		};
        mViewPager.setAdapter(mCoinWaringPagerAdapter);
        mViewPager.setOffscreenPageLimit(mCoinWaringPagerAdapter.getCount());
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				mFlowIndicator.setSeletion(position);
				changeSlidingMenuState(position);
			}
			
			@Override
			public void onPageScrolled(int position, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int position) {
				
			}
		});
        mFlowIndicator.setCount(mCoinWaringPagerAdapter.getCount());
	}
	
	private void changeSlidingMenuState(int position) {
		if (mSlidingMenu != null && position == mCoinWaringPagerAdapter.getCount() - 1) {
			mSlidingMenu.setSlidingEnabled(true);
			mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		} else if (mSlidingMenu != null && position == 0) {
			mSlidingMenu.setSlidingEnabled(true);
			mSlidingMenu.setMode(SlidingMenu.LEFT);
		} else {
			mSlidingMenu.setSlidingEnabled(false);
		}
	}
	
}
