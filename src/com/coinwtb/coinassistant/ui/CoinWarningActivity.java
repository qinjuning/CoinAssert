package com.coinwtb.coinassistant.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.fragment.CoinWaringPagerAdapter;
import com.coinwtb.coinassistant.ui.base.BaseFragmentActivity;
import com.coinwtb.coinassistant.widget.FlowIndicator;

/**
 * 预警管理
 *
 */
public class CoinWarningActivity extends BaseFragmentActivity{

	private static final String TAG = "CoinWarningActivity";
	
	private ViewPager mViewPager;

	private CoinWaringPagerAdapter mCoinWaringPagerAdapter;
	private FlowIndicator mFlowIndicator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coin_waring_main);
	    mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setOffscreenPageLimit(CoinWaringPagerAdapter.COIN_WARING_SIZE); 
		
		setViewPagerAdapter();
	}
	

	private void setViewPagerAdapter() {
		mFlowIndicator = (FlowIndicator)  findViewById(R.id.pager_indictor);
		mCoinWaringPagerAdapter = new CoinWaringPagerAdapter(getSupportFragmentManager(), null) {
			
			@Override
			public Fragment getItem(int position) {
				Fragment fragment = super.getItem(position);
				return fragment;
			}
			
		};
        mViewPager.setAdapter(mCoinWaringPagerAdapter);
       
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				mFlowIndicator.setSeletion(position);
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
	
}
