package com.coinwtb.coinassistant.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.bean.CoinPriceBean;
import com.coinwtb.coinassistant.ui.base.BaseActivity;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.FormatUtils;
import com.coinwtb.coinassistant.widget.CoinRealPriceRelativeLayout;

/**
 * 价格历史记录表
 *
 */
public class CoinPriceHistoryActivity extends BaseActivity{

	private static final String TAG = "CoinWarningActivity";

	private ListView mListView;
	private View mEmptyView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coin_price_history_list);
		
		mListView = (ListView) findViewById(android.R.id.list);
		mEmptyView = findViewById(android.R.id.empty);
		
		int selectedIndex = CoinApplication.getInstance().getLastShowPriceIndex();
		if (selectedIndex >= 0 && selectedIndex < CoinApplication.getInstance().getBterCoinList().size()) {
			CoinBean coinBean = CoinApplication.getInstance().getBterCoinList().get(selectedIndex);
		    if (coinBean == null) {
		    	setEmptyHistory();
		    } else {
		    	setHeaderTitle(coinBean.getFullTradingName());
		    	List<CoinPriceBean> priceList = coinBean.getCoinPriceList();
		    	if (priceList == null || priceList.isEmpty()) {
		    		setEmptyHistory();
		    	} else {
		    		List<CoinPriceBean> tempList = new ArrayList<CoinPriceBean>();
		    		tempList.addAll(priceList);
		    		Collections.sort(tempList);
		    		mListView.setEmptyView(mEmptyView);
		    		mListView.setAdapter(new PriceHistoryAdapter(CoinPriceHistoryActivity.this, tempList));
		    	}
		    }
		} else {
			setEmptyHistory();
		}
	}
	
	/**
	 * 出错或列表为空
	 */
	private void setEmptyHistory() {
		mListView.setVisibility(View.GONE);
		mEmptyView.setVisibility(View.VISIBLE);
	}

	public class PriceHistoryAdapter extends BaseAdapter {

		Context context;
		private List<CoinPriceBean> priceList;
		
		public PriceHistoryAdapter(Context context, List<CoinPriceBean> priceList) {
			this.context = context;
			this.priceList = priceList;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public int getCount() {
			return priceList == null ? 0 : priceList.size(); 
		}

		@Override
		public CoinPriceBean getItem(int position) {
			return priceList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		        ViewHolder viewHolder = null;
				if (null == convertView) {
					convertView = LayoutInflater.from(context).inflate(R.layout.coin_price_history_item, null);
					viewHolder = ViewHolder.newViewHolder(convertView);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}
				if (getItem(position) != null) {
					CoinPriceBean priceBean = getItem(position);
					viewHolder.mCoinRealPriceRl.setLastPrice(priceBean.getLastPrice());
					viewHolder.mCoinRealPriceRl.setHighPrice(priceBean.getHightPrice());
					viewHolder.mCoinRealPriceRl.setLowerPrice(priceBean.getLowPrice());
					if (priceBean.getRetriveTime() != 0) {
						viewHolder.tvTime.setVisibility(View.VISIBLE);
						viewHolder.tvTime.setText(FormatUtils.makeLongDateStringFromDate(new Date(priceBean.getRetriveTime())));
					} else {
						viewHolder.tvTime.setVisibility(View.INVISIBLE);
					}
					viewHolder.tvIndex.setText(String.valueOf(position + 1));
				}

		        return convertView;
		}
	}
	
	public static class ViewHolder {
		TextView tvIndex;
		CoinRealPriceRelativeLayout mCoinRealPriceRl;
		TextView tvTime;
		
		private ViewHolder() {}
		
		public static ViewHolder newViewHolder(View view) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvIndex = (TextView) view.findViewById(R.id.tv_position);
	        viewHolder.mCoinRealPriceRl = (CoinRealPriceRelativeLayout) view.findViewById(R.id.coin_real_price_rl);
	        viewHolder.tvTime = (TextView) view.findViewById(R.id.tv_coin_update_time);
	        
	        return viewHolder;
		}
	}
}
