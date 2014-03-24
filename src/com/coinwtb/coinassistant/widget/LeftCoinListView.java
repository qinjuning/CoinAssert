package com.coinwtb.coinassistant.widget;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.FormatUtils;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.utils.Utils;

/**
 * 左侧的ListView导航栏
 *
 */
public class LeftCoinListView extends ListView {
	
	private final String TAG = "LeftCoinListView";
	
	private LeftCoinRecordsAdapter mAdapter;
	
	public LeftCoinListView(Context context) {
		this(context, null);
	}
	
	public LeftCoinListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public LeftCoinListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mAdapter = new LeftCoinRecordsAdapter(getContext());
		setAdapter(mAdapter);
	}

	public class LeftCoinRecordsAdapter extends BaseAdapter {

		Context context;

		private List<CoinBean> bterCoins = CoinApplication.getInstance().getBterCoinList();
		
		private int[] mCacheIcons = new int[bterCoins.size()];
		
		public LeftCoinRecordsAdapter(Context context) {
			this.context = context;
			// 初始值为0，表示没有获取资源ID
			Arrays.fill(mCacheIcons, 0, mCacheIcons.length, 0);
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public int getCount() {
			return bterCoins.size();
		}

		@Override
		public CoinBean getItem(int position) {
			return bterCoins.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (null == convertView) {
				convertView = LayoutInflater.from(context).inflate(R.layout.coin_records_item, null);
				viewHolder = ViewHolder.newViewHolder(convertView);
				
				viewHolder.btnAction.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								int position = ((Integer) v.getTag()).intValue();
								Slog.v(TAG, "OnClickListener position" + position);
							}
						});

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			if (getItem(position) != null) {
				CoinBean coinBean =  getItem(position);
				viewHolder.title.setText(FormatUtils.makeFormatString(
						context.getResources().getString(R.string.bter_format_product_name),
						coinBean.getShortName(), coinBean.getTransferType()));
				viewHolder.btnAction.setText("详情");
		    	viewHolder.btnAction.setVisibility(View.INVISIBLE);
		    	viewHolder.image_arrow.setVisibility(View.VISIBLE);
		    	if (mCacheIcons[position] == 0) {
					mCacheIcons[position] = Utils.getCoinDrawableIdentifierSafely(context, 
							coinBean.getShortName().toLowerCase());
					viewHolder.img.setBackgroundResource(mCacheIcons[position]);
		    	} else {
		    		viewHolder.img.setBackgroundResource(mCacheIcons[position]);
		    	}
			}
	    
			viewHolder.btnAction.setTag(position);
			return convertView;
		}
	}
	
	public static class ViewHolder {
		ImageView img;
		TextView title;
		TextView tvNum;
		TextView btnAction;
		View image_arrow;
		
		private ViewHolder() {}
		
		public static ViewHolder newViewHolder(View view) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.img = (ImageView) view.findViewById(R.id.image_coin_icon);
	        viewHolder.title = (TextView) view.findViewById(R.id.tv_coin_name);
	        viewHolder.tvNum = (TextView) view.findViewById(R.id.tv_zengzhi_used_count);
	        viewHolder.btnAction = (TextView) view.findViewById(R.id.btn_zengzhi_action);
	        viewHolder.image_arrow = view.findViewById(R.id.image_arrow);
	        
	        return viewHolder;
		}
	}
	
}
