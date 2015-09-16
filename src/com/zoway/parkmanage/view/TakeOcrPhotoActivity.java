package com.zoway.parkmanage.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zoway.parkmanage.R;
import com.zoway.parkmanage.image.BitmapHandle;
import com.zoway.parkmanage.utils.PathUtils;

public class TakeOcrPhotoActivity extends Activity implements OnClickListener {

	private final String TAG = "TakePhotoActivity";
	private String rcid = null;
	private String rcno = null;
	private String sno = null;
	private String rt = null;
	private ProgressDialog pDia1 = null;
	private Camera mCamera;
	private CameraPreview mPreview;
	private Button btnTakePhoto;
	private Button btnUpZoom;
	private Button btnDownZoom;
	private PictureCallback mPicture;
	private FrameLayout preview;
	private String imgBasePath = PathUtils.getTmpImagePath();
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO 接收消息并且去更新UI线程上的控件内容
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:

				break;
			case 2:

				Intent intent = new Intent(TakeOcrPhotoActivity.this,
						ShowOcrPhotoActivity.class);
				intent.putExtra("rcid", rcid);
				intent.putExtra("rcno", rcno);
				intent.putExtra("sno", sno);
				intent.putExtra("rt", rt);
				intent.putExtra("fn", (String) msg.obj);
				TakeOcrPhotoActivity.this.startActivity(intent);
				break;
			}

		}
	};

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent ii = new Intent(this, MainActivity.class);
		this.startActivity(ii);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btncapture:
			mCamera.takePicture(null, null, mPicture);
			break;
		case R.id.btnupzoom:
			mPreview.setZoomView(8);
			break;
		case R.id.btndownzoom:
			mPreview.setZoomView(-8);
			break;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityList.pushActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_take_photo);
		// Create an instance of Camera
		mCamera = getCameraInstance();
		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);

		preview = (FrameLayout) findViewById(R.id.camera_preview);

		LinearLayout tkephotoly1 = (LinearLayout) findViewById(R.id.tkephotoly2);

		btnTakePhoto = (Button) this.findViewById(R.id.btncapture);
		btnTakePhoto.setOnClickListener(this);

		btnUpZoom = (Button) this.findViewById(R.id.btnupzoom);
		btnUpZoom.setOnClickListener(this);

		btnDownZoom = (Button) this.findViewById(R.id.btndownzoom);
		btnDownZoom.setOnClickListener(this);

		preview.addView(mPreview);
		Intent i = this.getIntent();
		rcid = i.getStringExtra("rcid");
		rcno = i.getStringExtra("rcno");
		sno = i.getStringExtra("sno");
		rt = i.getStringExtra("rt");
		if (sno != null && rt != null) {
			TextView tv = new TextView(this);
			tv.setText("车位位置:" + sno + "\n" + rt.replace("T", "  "));

			tv.setTextSize(25);
			tv.setTextColor(Color.rgb(255, 0, 0));
			preview.addView(tv);
		}
		DrawRectView drv = new DrawRectView(this);

		preview.addView(drv);
		preview.bringChildToFront(tkephotoly1);
		mPicture = new pitcCallback();
	}

	private class pitcCallback implements PictureCallback {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			int ii = data.length;
			pDia1 = ProgressDialog.show(TakeOcrPhotoActivity.this, "处理车牌图片",
					"正在处理中", true, false);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String in = sdf.format(Calendar.getInstance().getTime());
			String fname = imgBasePath + in + ".jpg";
			// String fname = imgBasePath + "20150824104730.jpg";
			Log.i(TAG, fname + "	" + ii);
			Thread t1 = new Thread(new SavePit(fname, data));
			t1.start();
		}
	}

	public class SavePit implements Runnable {

		public String fn;
		public byte[] dat;

		public SavePit(String fn, byte[] dat) {
			this.fn = fn;
			this.dat = dat;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (TakeOcrPhotoActivity.this.mCamera != null) {
				Size ss = TakeOcrPhotoActivity.this.mCamera.getParameters()
						.getPictureSize();
				BitmapHandle.writeJpgFromBytes2(fn, ss.width, ss.height, dat,
						100);
			}
			Message msg = new Message();
			msg.what = 2;
			msg.obj = fn;
			handler.sendMessage(msg);

		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
		mCamera.stopPreview();
		mCamera.release();
		pDia1.dismiss();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

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

	public static Camera getCameraInstance() {
		int i = Camera.getNumberOfCameras();
		Camera c = null;
		try {

			c = Camera.open(0); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

}
