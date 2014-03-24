package com.coinwtb.coinassistant.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.bean.CoinPriceBean;
import com.coinwtb.coinassistant.utils.Slog;

/**
 * 设置折线图的控件
 *
 */
public class PriceChartRelativeLayout extends RelativeLayout  {

	public static final String TAG = PriceChartRelativeLayout.class.getSimpleName();
	
	public static final int MAX_SIZE = 20;          // 最多显示20个点 
	
	public static final int K_TYPE_1_MINUTES = 0;   // 1分钟K线图
	public static final int K_TYPE_5_MINUTES = 1;   // 5分钟K线图
	private int mKType = K_TYPE_1_MINUTES;          // K线图
	
	private static final int ONE_MINUTES = 60 * 1000;         // 1分钟
	private static final int FIVE_MINUTES = 5 * ONE_MINUTES;  // 5分钟
	
	private CoinBean mCoinBean;      // 显示的数据实体
	
	private TimeSeries mSeries;
	private XYMultipleSeriesDataset mDataset;
	private GraphicalView mChartView;
	private XYMultipleSeriesRenderer mRenderer;
	 
	private final String mChartTitle = "折线图";
	
	public PriceChartRelativeLayout(Context context) {
		this(context, null);
	}
	
	public PriceChartRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PriceChartRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initChart();
	}
	
	/**
	 * 设置实体并刷新
	 * @param coinBean
	 */
	public void setCoinbeanAndRepaint(final CoinBean coinBean) {
		if (coinBean != null) {
			mCoinBean = coinBean;
			filterCoinPriceByKValue();
		}
	}
	
	/**
	 * 更改K线显示类型，1分钟K线，5分钟K线
	 * @param type
	 */
	public void changeKType(int type){
		if (mKType == type && (type != K_TYPE_1_MINUTES || type != K_TYPE_5_MINUTES)) {
			return;
		}
		mKType = type;
		filterCoinPriceByKValue();
	}
	
	// 获得间隙时间值
	private long getChillTime() {
		return mKType == K_TYPE_1_MINUTES ? ONE_MINUTES : FIVE_MINUTES;
	}
	
    private void initChart() {
		
		// 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
		mSeries = new TimeSeries(mChartTitle);
		// 创建一个数据集的实例，这个数据集将被用来创建图表
		mDataset = new XYMultipleSeriesDataset();
		// 将点集添加到这个数据集中
		mDataset.addSeries(mSeries);

		// 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
		mRenderer = buildRenderer(getResources().getColor(R.color.blue), PointStyle.DIAMOND, true);

		// 设置好图表的样式
		setChartSettings(mRenderer, "时间", "价格", 0, 10, 0, 10, 
				getResources().getColor(R.color.yellow_tu), 
				getResources().getColor(R.color.grey_medium));

		// 生成图表
		mChartView = ChartFactory.getTimeChartView(getContext(), mDataset, mRenderer, "h:mm:ss");
		// 将图表添加到布局中去
		addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	private void updateChart(long xMin, long xMax, double yMax, List<TimePoint> timePoints) {
		if (timePoints == null || timePoints.isEmpty()) {
			return;
		}
		mSeries.clear();
		int N = timePoints.size();
		int bengin = 0;
		if (N > MAX_SIZE) {
			bengin = N - MAX_SIZE;
		}
		long maxTime= timePoints.get(bengin).xDate.getTime();          // 显示的最新日期
		long minTime= timePoints.get(bengin).xDate.getTime();          // 显示的最旧日期
		
		for (; bengin < N; bengin++) {
			mSeries.add(timePoints.get(bengin).xDate, timePoints.get(bengin).yPrice);
			long time = timePoints.get(bengin).xDate.getTime();
			if (time > maxTime) {
				maxTime = time;
			} else {
				minTime = time;
			}
		}
		if (minTime >= xMin) {
			minTime = xMin;
		}
		
//		// 模拟真实数据
//		for (int i = 0, N = 10; i < N; i++) {
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.MINUTES, 10);
//			Slog.v(TAG, FormatUtils.makeLongDateStringFromCalendar(calendar));
//			mSeries.add(calendar.getTime() , i);
//			
//			if (i == 0 ) {
//				xMin = calendar.getTime().getTime();
//			}
//			if (i == 9 ) {
//				xMax = calendar.getTime().getTime();
//			}
//		}
		
		//设置Min Max 必须设置, 否则会出现问题
		mRenderer.setXAxisMin(xMin); 
		mRenderer.setXAxisMax(xMax);
		mRenderer.setYAxisMin(0);
		mRenderer.setYAxisMax(yMax * 2);   // 设为2倍长度
	
		int itemCount = mSeries.getItemCount();
		// 动态设置X YLable
		double width = mRenderer.getXAxisMax() - mRenderer.getXAxisMin();
		double height  = mRenderer.getYAxisMax() - mRenderer.getYAxisMin();
		int xLable = (int) width / itemCount ; 
		int yLable = (int) height / itemCount ; 
		Slog.v(TAG, "width=" + width + " @ height=" + height + " @ count = " + mSeries.getItemCount());
		Slog.v(TAG, "xLable=" + xLable + " @ yLable=" + yLable);
		xLable = itemCount;
		// X轴最小为5, 最大为10
		if (itemCount < 2) {
			xLable = 2;
		} else if (itemCount > 5) {
			xLable = 5;
		} 
		// 设置X Y轴等分刻度线
		mRenderer.setXLabels(xLable); 
		// Y轴固定为10 
		mRenderer.setYLabels(5);   
		
		mChartView.repaint();   //  刷新
	}
	
	// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
	private XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(color);  // 颜色
		r.setPointStyle(style); // 曲线点填充样式
		r.setFillPoints(fill); //  是否填充
		r.setLineWidth(3); //曲线粗线
		renderer.setPointSize((float)5.0f);  // 曲线点大小
		renderer.addSeriesRenderer(r);
		return renderer;
	}
	
	// 设置图表中本身样式
	private void setChartSettings(XYMultipleSeriesRenderer renderer,
			String xTitle, String yTitle, double xMin, double xMax,
			double yMin, double yMax, int axesColor, int labelsColor) {
		// 有关对图表的渲染可参看api文档
		renderer.setChartTitle(mChartTitle);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
		renderer.setLabelsTextSize(12f);
		renderer.setLegendTextSize(16f);
		// 设置坐标轴点的颜色
		renderer.setXLabelsColor(getResources().getColor(R.color.grey_medium));
		renderer.setShowGrid(true);
		// 设置ChartView背景色彩
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(getResources().getColor(R.color.screen_bg_color));
		renderer.setMarginsColor(getResources().getColor(R.color.screen_bg_color));
		renderer.setGridColor(Color.GRAY);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setShowLegend(false);
	}
	
	/**
	 *  根据曲线图显示时间值过滤合适的CoinPrice
	 */
	private void filterCoinPriceByKValue() {
		if (mCoinBean == null) {
			return;
		}
		List<CoinPriceBean> coinPriceList = mCoinBean.getCoinPriceList();
		if (coinPriceList == null || coinPriceList.size() == 0) {
			return;
		}
		List<CoinPriceBean> tempCoinList = new ArrayList<CoinPriceBean>();
		tempCoinList.addAll(coinPriceList);
		Slog.v(TAG, "tempCoinList=" + tempCoinList);
		double maxPrice = 0.0f;   // 最高价
		long maxTime= 0;          // 显示的最新日期
		long minTime= 0;          // 显示的最旧日期
		long nextTime = 0;        // 设置为第一个时间
		final int N = tempCoinList.size();
		List<TimePoint> timePoints = new ArrayList<TimePoint>();
		for (int i = 0; i < N; i++) {
			CoinPriceBean coinPriceBean = tempCoinList.get(i);
			long time = coinPriceBean.getRetriveTime();
			if (i == 0) {
				maxTime = nextTime = time;
				maxPrice = coinPriceBean.getLastPrice();
				nextTime = tempCoinList.get(0).getRetriveTime();
			}
			if (time < nextTime) {
				continue;
			}
			if (time > maxTime) {
				maxTime = time;
			} else {
				minTime = time;
			}
			if (coinPriceBean.getLastPrice() > maxPrice) {
				maxPrice = coinPriceBean.getLastPrice();
			} 
			nextTime = time + getChillTime();
			timePoints.add(new TimePoint(time, coinPriceBean.getLastPrice()));
			Slog.v(TAG, "minTime=" + minTime + " @ maxTime=" + maxTime + " @maxPrice=" + maxPrice + " @nextTime=" + nextTime);
		}
		updateChart(minTime, maxTime, maxPrice, timePoints);
		Slog.v(TAG, "minTime=" + minTime + " @ maxTime=" + maxTime + " @maxPrice=" + maxPrice);
	}
	
	static class TimePoint {
		private Date xDate;
		private double yPrice;
		
		public TimePoint( long mills, double price) {
			xDate = new Date(mills);
			yPrice = price;
		}
	}
}
