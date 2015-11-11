package com.zoway.parkmanage.view;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.device.Printer;
import com.landicorp.android.eptapi.device.Printer.Format;
import com.landicorp.android.eptapi.exception.ReloginException;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.exception.ServiceOccupiedException;
import com.landicorp.android.eptapi.exception.UnsupportMultiProcess;
import com.landicorp.android.eptapi.utils.QrCode;
import com.zoway.parkmanage.R;
import com.zoway.parkmanage.bean.LoginBean4Wsdl;
import com.zoway.parkmanage.db.DbHelper;
import com.zoway.parkmanage.http.RegisterVehicleInfoWsdl;
import com.zoway.parkmanage.image.BitmapHandle;
import com.zoway.parkmanage.utils.PathUtils;
import com.zoway.parkmanage.utils.TimeUtil;

public class InputInfoActivity extends Activity implements OnClickListener {

	public final int REQIMG1 = 0X01;
	public final int REQIMG2 = 0X02;
	public final int REQIMG3 = 0X03;
	private String rcid = null;
	private String rcno = null;
	private String sno = null;
	private String rt = null;
	private Date parkTime = null;
	private String hphm = null;
	private String fn = null;
	private int type = 0;
	private String img_path = PathUtils.getSdPath();

	// size 48*48
	private ImageButton img1;
	private ImageButton img2;
	private Button infosure;
	private Button btninfocancel;
	private Bitmap bitmapSelected1 = null;
	private Bitmap bitmapSelected2 = null;
	private ProgressDialog pDia;
	private TextView txtparktime;
	private TextView txtcarnumber;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO 接收消息并且去更新UI线程上的控件内容
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				pDia.dismiss();
				Toast.makeText(InputInfoActivity.this, "处理成功",
						Toast.LENGTH_LONG).show();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Intent intent = new Intent(InputInfoActivity.this,
						MainActivity.class);
				InputInfoActivity.this.startActivity(intent);
				break;
			case 2:

				break;
			}

		}
	};

	public void runOnUiThreadDelayed(Runnable r, int delayMillis) {
		handler.postDelayed(r, delayMillis);
	}

	/**
	 * To gain control of the device service, you need invoke this method before
	 * any device operation.
	 */
	public void bindDeviceService() {
		try {
			DeviceService.login(this);
		} catch (RequestException e) {
			// Rebind after a few milliseconds,
			// If you want this application keep the right of the device service
			runOnUiThreadDelayed(new Runnable() {
				@Override
				public void run() {
					bindDeviceService();
				}
			}, 300);
			e.printStackTrace();
		} catch (ServiceOccupiedException e) {
			e.printStackTrace();
		} catch (ReloginException e) {
			e.printStackTrace();
		} catch (UnsupportMultiProcess e) {
			e.printStackTrace();
		}
	}

	/**
	 * Release the right of using the device.
	 */
	public void unbindDeviceService() {
		DeviceService.logout();
	}

	private Printer.Progress progress = new Printer.Progress() {

		@Override
		public void doPrint(Printer printer) throws Exception {
			// TODO Auto-generated method stub
			Format format = new Format();
			// Use this 5x7 dot and 1 times width, 2 times height
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");
			String datetext = sdf.format(parkTime);
			printer.printText("        路边停车凭条\n");
			format.setAscScale(Format.ASC_SC1x1);
			printer.setFormat(format);
			printer.printText("\n");
			printer.printText("商户名称:\n");
			printer.printText("车牌号码:粤" + hphm + "\n");
			printer.printText("停车位置:南源路\n");
			printer.printText("停车时间:" + datetext + "\n");
			printer.printText("操作员:"
					+ LoginBean4Wsdl.getWorker().getWorkerName() + "\n\n");
			printer.setAutoTrunc(false);
			printer.printText("敬爱的车主，请使用微信扫描下方二维码查询停车时长。");
			printer.printText("\n\n");

			String cUrl = String.format(
					"http://cx.zoway.com.cn:81/ParkRecord/show/%s.do", rcno);
			printer.printQrCode(35, new QrCode(cUrl, QrCode.ECLEVEL_M), 312);
			printer.feedLine(3);
		}

		@Override
		public void onFinish(int arg0) {
			// TODO Auto-generated method stub
			Message msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);
		}

		@Override
		public void onCrash() {
			// TODO Auto-generated method stub
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控/拦截/屏蔽返回键

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		switch (arg0.getId()) {
		case R.id.parkimg1:
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(img_path, "p1ori.jpg")));
			startActivityForResult(intent, REQIMG1);
			break;
		case R.id.parkimg2:
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(img_path, "p2.jpg")));
			startActivityForResult(intent, REQIMG2);
			break;
		case R.id.parkimg3:
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(img_path, "p3.jpg")));
			startActivityForResult(intent, REQIMG3);
			break;
		case R.id.btninfosure:
			pDia = ProgressDialog.show(InputInfoActivity.this, "打印停车纸",
					"正在打印中", true, false);
			try {
				InputStream is = getContentResolver().openInputStream(
						Uri.fromFile(new File(fn)));
				Bitmap b1 = BitmapHandle.getReduceBitmap(is, false, 5, 0);
				BitmapHandle.writeJpgFromBitmap(img_path + File.separator
						+ "p2.jpg", b1, 90);
				b1 = null;

				DbHelper.insertRecord(rcno, hphm, "blue", parkTime, img_path,
						0, 0, 0);
				progress.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.btninfocancel:
			int iiii = 0;
			this.onBackPressed();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityList.pushActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_input_info);
		Intent intent = this.getIntent();
		rcid = intent.getStringExtra("rcid");

		sno = intent.getStringExtra("sno");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		type = intent.getIntExtra("type", 0);
		if (type == 4) {
			rt = intent.getStringExtra("rt");
			try {
				parkTime = sdf.parse(rt);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rcno = intent.getStringExtra("rcno");
		} else {
			parkTime = TimeUtil.getTime();
			String uuid = java.util.UUID.randomUUID().toString();
			rcno = uuid;
		}
		hphm = intent.getStringExtra("hphm");
		fn = intent.getStringExtra("fn");

		img_path = img_path + File.separator + sdf.format(parkTime) + hphm
				+ File.separator;
		File fDir = new File(img_path);
		if (!fDir.exists()) {
			fDir.mkdirs();
		}
		img1 = (ImageButton) this.findViewById(R.id.parkimg1);
		img2 = (ImageButton) this.findViewById(R.id.parkimg2);

		infosure = (Button) this.findViewById(R.id.btninfosure);
		btninfocancel = (Button) this.findViewById(R.id.btninfocancel);

		txtparktime = (TextView) this.findViewById(R.id.txtparktime);
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
		txtparktime.setText(sdf1.format(parkTime));
		txtcarnumber = (TextView) this.findViewById(R.id.txtcarnumber);
		txtcarnumber.setText("粤" + hphm);
		img1.setOnClickListener(this);
		img2.setOnClickListener(this);
		infosure.setOnClickListener(this);
		btninfocancel.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.input_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED)
			return;
		else {
			switch (requestCode) {
			case REQIMG1:
				try {
					InputStream is = getContentResolver().openInputStream(
							Uri.fromFile(new File(img_path, "p1ori.jpg")));
					bitmapSelected1 = BitmapHandle.getReduceBitmap(is, false,
							5, 90);
					this.img1.setImageBitmap(bitmapSelected1);
					BitmapHandle.writeJpgFromBitmap(img_path + File.separator
							+ "p1.jpg", bitmapSelected1, 90);
					File f = new File(img_path + File.separator + "p1ori.jpg");
					if (f.exists()) {
						f.delete();
					}
					if (bitmapSelected1 != null) {
						bitmapSelected1 = null;
					}
				} catch (Exception er) {
					er.printStackTrace();
				}
				break;
			case REQIMG2:
				try {
					InputStream is = getContentResolver().openInputStream(
							Uri.fromFile(new File(img_path, "p2ori.jpg")));
					bitmapSelected2 = BitmapHandle.getReduceBitmap(is, false,
							5, 90);
					this.img2.setImageBitmap(bitmapSelected2);
					BitmapHandle.writeJpgFromBitmap(img_path + File.separator
							+ "p2.jpg", bitmapSelected2, 90);
					if (bitmapSelected2 != null) {
						bitmapSelected2 = null;
					}
				} catch (Exception er) {
					er.printStackTrace();
				}
				break;
			default:
				break;
			}
			System.gc();
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
		unbindDeviceService();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		bindDeviceService();
	}

}
