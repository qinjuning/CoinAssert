package com.coinwtb.coinassistant.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.widget.MeetingDialog;

public class UpdateManager {
	
	private final String UPDATE_APK_URL = "http://f.wiseden.cn/version.htm";
	private final String APK_DOWNLOAD_DIRECTORY = "versions";
	
	private Context context;
	private Boolean isRun = true;
	private ProgressDialog proDialog = null;
	private int newVersioncode;
	private String newVersionname;
	private String newVersiontxt;
	private String apkUrl;
	private int curVersion;
	private int fileSize = 0;
	private int downLoadedFileSize = 0;
	private MeetingDialog mDownloadDialog;
	private ProgressBar mProgress;
	private TextView mUpdateNumTv;
	private TextView mUpdateNumPercentTv;
	private Thread mUpdateThread = null;

	private static final int NORMAL = 2;// 正常启动
	private static final int UPDATE = 1;// 有更新
	private static final int UPDATE_ERROR = 0;// 检查更新失败
	private static final int DOWN = 3;// 下载成功
	private static final int DOWN_ERROR = 4;// 下载失败

	private boolean isShowCheckDialog = false;

	public UpdateManager() {
	}

	/*
	 * 开始检测版本
	 */
	public void showCheckVersionDialog(Context context) {
		isShowCheckDialog = true;
		isRun = true;
		proDialog = ProgressDialog.show(context, "检测更新", "正在检测...请稍候...");
		proDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& event.getRepeatCount() == 0) {
					dialog.dismiss();
					isRun = false;
				}
				return false;
			}
		});

		startCheckVersionThread(context);
	}

	private  void startCheckVersionThread(Context context) {
		this.context = context;
		CheckThread cThread = new CheckThread();
		cThread.start();
	}

	public class CheckThread extends Thread {
		public CheckThread() {
		}

		public void run() {
			try {
				if (checkNewVersion()) {
					// 检查到有更新
					Message msg = chandler.obtainMessage();
					Bundle b = new Bundle();
					b.putInt("zt", UPDATE);
					msg.setData(b);
					if (isRun == false)
						return;
					chandler.sendMessage(msg);
				} else {
					// 跳转
					Message msg = chandler.obtainMessage();
					Bundle b = new Bundle();
					b.putInt("zt", NORMAL);
					msg.setData(b);
					if (isRun == false)
						return;
					chandler.sendMessage(msg);
				}
			} catch (Exception e) {
				// 异常时状态及异常信息,异常信息来自checkNewVersion
				Message msg = chandler.obtainMessage();
				Bundle b = new Bundle();
				b.putInt("zt", UPDATE_ERROR);
				msg.setData(b);
				if (isRun == false)
					return;
				chandler.sendMessage(msg);
			}
		}
	}

	Handler chandler = new Handler() {
		public void handleMessage(Message m) {
			int state = m.getData().getInt("zt");
			// progressDialog.dismiss();
			switch (state) {
			case UPDATE:
				isShowCheckDialog = false;
				try {
					if (proDialog != null && proDialog.isShowing()) {
						proDialog.dismiss();
					}
					
					final MeetingDialog meetingDialog = new MeetingDialog(context);
				
					meetingDialog.setTitle(context.getText(R.string.app_update_found));
					meetingDialog.setMessage(newVersiontxt);
					meetingDialog.setCancelable(true);
					meetingDialog.setPostiveClickListener(context.getText(R.string.app_update_do), 
                                 new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										meetingDialog.cancel();
										// 下载更新包,安装
										if (mUpdateThread == null
												|| !mUpdateThread.isAlive()) {
											mUpdateThread = new UpdateThread(
													chandler);
											mUpdateThread.start();
										}
									}
								});
					meetingDialog.setNegativeClickListener(context.getText(R.string.app_update_cancel), 
                            new View.OnClickListener() {
								
								@Override
								public void onClick(View v) {
									meetingDialog.cancel();
								}
							});
					
					meetingDialog.show();
					
				} catch (Exception e) {
					// TODO: handle exception
				}
					
				break;
			case UPDATE_ERROR:        // 收到了调用出错的消息
				isShowCheckDialog = false;
				if (proDialog != null && proDialog.isShowing())
					proDialog.dismiss();
				
				final MeetingDialog meetingDialog = new MeetingDialog(context);
				
				meetingDialog.setTitle(context.getText(R.string.app_update_found));
				meetingDialog.setMessage("网络错误。");
				meetingDialog.setPostiveClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								meetingDialog.cancel();
							}
						});
				meetingDialog.show();
				break;
			case NORMAL:
				if (proDialog != null && proDialog.isShowing())
					proDialog.dismiss();
				if (isShowCheckDialog) {
					Toast.makeText(context, "您所使用的已经是最新版本", Toast.LENGTH_LONG)
							.show();
				}
				isShowCheckDialog = false;
				break;
			case DOWN:
				isShowCheckDialog = false;
				try {
					if (installAPK()) {
						mDownloadDialog.dismiss();
					}
				} catch (Exception e) {
					
				}
				break;
			case DOWN_ERROR:
				isShowCheckDialog = false;
				if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
					mDownloadDialog.dismiss();
				}
                final MeetingDialog meetingDialog1 = new MeetingDialog(context);
				
                meetingDialog1.setTitle(context.getText(R.string.app_update_found));
                meetingDialog1.setMessage("网络错误。");
                Log.v(UpdateManager.class.getSimpleName(), "网络错误 : " + m.getData().getString("error"));
                meetingDialog1.setPostiveClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								meetingDialog1.cancel();
							}
						});
                meetingDialog1.show();
				break;
			}
		}
	};

	public class UpdateThread extends Thread {
		private Handler handler;

		public UpdateThread(Handler handler) {
			this.handler = handler;
		}

		public void run() {
			try {
				if (downloadAPK(apkUrl)) {
					// 检查到有更新
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					b.putInt("zt", DOWN);
					msg.setData(b);
					handler.sendMessage(msg);
				} else {
					// 跳转
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					b.putInt("zt", NORMAL);
					msg.setData(b);
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				// 异常时状态及异常信息,异常信息来自checkNewVersion
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putInt("zt", DOWN_ERROR);
				b.putString("error", e.getMessage());
				msg.setData(b);
				handler.sendMessage(msg);
			} finally {
				if (mDownloadDialog != null) {
					mDownloadDialog.dismiss();
					mDownloadDialog.cancel();
					mDownloadDialog = null;
				}
			}
		}
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.update_view, null);
		mProgress = (ProgressBar) view.findViewById(R.id.update_pb);
		mUpdateNumTv = (TextView) view.findViewById(R.id.update_tv1);
		mUpdateNumPercentTv = (TextView) view.findViewById(R.id.update_tv2);
		mProgress.setProgress(0);
		mProgress.setMax(fileSize);
		
		mDownloadDialog= new MeetingDialog(context);
		mDownloadDialog.setTitle("下载中");
		mDownloadDialog.setView(view);
		mDownloadDialog.setCancelable(false);
		mDownloadDialog.show();
	}

	private Handler mProgressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mUpdateThread != null & mUpdateThread.isAlive()) {
				switch (msg.what) {
				case 0:
					showDownloadDialog();
					break;
				case 1:
					mProgress.setProgress(downLoadedFileSize);
					mUpdateNumTv.setText(FormatUtils.formateWithResId(context, 
							R.string.update_progress_num,
							String.valueOf(downLoadedFileSize),
							String.valueOf(fileSize)));
					mUpdateNumPercentTv
							.setText(String.valueOf(downLoadedFileSize * 100
									/ fileSize)
									+ "%");
					break;
				}
			}
			super.handleMessage(msg);
		}
	};

	private boolean checkNewVersion() throws Exception {
		
		URL url = new URL(UPDATE_APK_URL); 
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setConnectTimeout(2000);
		con.setReadTimeout(5000);
		InputStream is = con.getInputStream();
		InputStreamReader isr = new InputStreamReader(is, "GB2312");
		parser.parse(new InputSource(isr), new DefaultHandler() {
			private String cur = "";
			private int step;

			@Override
			public void startDocument() throws SAXException {
				step = 0;
			}

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				cur = localName;
			}

			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				String str = new String(ch, start, length).trim();
				if (str == null || str.equals(""))
					return;
				if (cur.equals("url")) {
					apkUrl = str;
				}
				if (cur.equals("versioncode")) {
					newVersioncode = Integer.parseInt(str);
				}
				if (cur.equals("versionname")) {
					newVersionname = str;
				}
				if (cur.equals("versionnew")) {
					newVersiontxt = str.replace("\\r\\n", "\r\n");
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				step = step + 1;
			}

			@Override
			public void endDocument() throws SAXException {
				super.endDocument();
			}
		});

		if (diffVersion(newVersioncode)) {
			return true;
		}
		return false;
	}

	private boolean diffVersion(int newVersioncode) {
		try {
			curVersion = context.getPackageManager().getPackageInfo(
					CoinApplication.getInstance().getPackageName(), 0).versionCode;
			if (curVersion < newVersioncode) {
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;

	}

	private boolean downloadAPK(String apkUrl) throws Exception {
		try {
			File folder = Utils.createAppPublicFolderIfRequired(APK_DOWNLOAD_DIRECTORY);
			if (folder == null) {
				return false;
			}
			String fileName = newVersionname + ".apk";
			File fileOut = new File(folder, fileName);
		    if (fileOut.exists()) {
				fileOut.delete();
			}

			URL url = null;
			url = new URL(apkUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(2000);
			con.setReadTimeout(5000);
			con.connect();
			fileSize = con.getContentLength();// 文件大小
			sendMsg(0);
			InputStream in = con.getInputStream();

			FileOutputStream out = new FileOutputStream(fileOut);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = in.read(bytes)) != -1) {
				out.write(bytes, 0, c);
				downLoadedFileSize += c;// 循环记录已下载数据大小
				sendMsg(1);
			}
			in.close();
			out.close();
			return true;
		} catch (Exception e) {
			throw e;
		}

	}

	private void sendMsg(int value) {
		Message msg = new Message();
		msg.what = value;
		mProgressHandler.sendMessage(msg);
	}

	private boolean installAPK() {
		try {
			File folder = Utils.createAppPublicFolderIfRequired(APK_DOWNLOAD_DIRECTORY);
			if (folder == null) {
				return false;
			}
			
			String filePath= folder.getAbsolutePath() + File.separator + newVersionname + ".apk";
			
			System.out.println("filePath @@ " + filePath);
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(filePath)),
					"application/vnd.android.package-archive");
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}

}
