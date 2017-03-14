package com.jcan.reemanpnp.forcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.jcan.reemanpnp.BaseActivity;
import com.jcan.reemanpnp.R;
import com.linj.FileOperateUtil;
import com.linj.album.view.FilterImageView;
import com.linj.camera.view.CameraContainer;
import com.linj.camera.view.CameraContainer.TakePictureListener;
import com.linj.camera.view.CameraView.FlashMode;
import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class CameraAty extends BaseActivity implements View.OnClickListener, TakePictureListener, CameraContainer.MyPreviewCallback {
    public final static String TAG = "CameraAty";
    private boolean mIsRecordMode = false;
    private CameraContainer mContainer;
    private FilterImageView mThumbView;
    private ImageButton mCameraShutterButton;
    private ImageButton mRecordShutterButton;
    private ImageView mFlashView;
    private ImageButton mSwitchModeButton;
    private ImageView mSwitchCameraView;
    private ImageView mVideoIconView;
    private View mHeaderBar;
    private boolean isRecording = false;

    private PreviewThread pt;
    private boolean crun;
    private CommonOperate operate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        mHeaderBar = findViewById(R.id.camera_header_bar);
        mContainer = (CameraContainer) findViewById(R.id.container);
        mThumbView = (FilterImageView) findViewById(R.id.btn_thumbnail);
        mVideoIconView = (ImageView) findViewById(R.id.videoicon);
        mCameraShutterButton = (ImageButton) findViewById(R.id.btn_shutter_camera);
        mRecordShutterButton = (ImageButton) findViewById(R.id.btn_shutter_record);
        mSwitchCameraView = (ImageView) findViewById(R.id.btn_switch_camera);
        mFlashView = (ImageView) findViewById(R.id.btn_flash_mode);
        mSwitchModeButton = (ImageButton) findViewById(R.id.btn_switch_mode);

        mThumbView.setOnClickListener(this);
        mCameraShutterButton.setOnClickListener(this);
        mRecordShutterButton.setOnClickListener(this);
        mFlashView.setOnClickListener(this);
        mSwitchModeButton.setOnClickListener(this);
        mSwitchCameraView.setOnClickListener(this);

        mContainer.setRootPath(FileConfig.mSaveRoot);
    }

    /**
     * 加载缩略图
     */
    private void initThumbnail() {
        String thumbFolder = FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, FileConfig.mSaveRoot);
        List<File> files = FileOperateUtil.listFiles(thumbFolder, ".jpg");
        if (files != null && files.size() > 0) {
            Bitmap thumbBitmap = BitmapFactory.decodeFile(files.get(0).getAbsolutePath());
            if (thumbBitmap != null) {
                mThumbView.setImageBitmap(thumbBitmap);
                //视频缩略图显示播放图案
                if (files.get(0).getAbsolutePath().contains("video")) {
                    mVideoIconView.setVisibility(View.VISIBLE);
                } else {
                    mVideoIconView.setVisibility(View.GONE);
                }
            }
        } else {
            mThumbView.setImageBitmap(null);
            mVideoIconView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_shutter_camera:
                mCameraShutterButton.setClickable(false);
                mContainer.takePicture(this);
                break;
            case R.id.btn_thumbnail:
                startActivity(new Intent(this, AlbumAty.class));
                break;
            case R.id.btn_flash_mode:
                if (mContainer.getFlashMode() == FlashMode.ON) {
                    mContainer.setFlashMode(FlashMode.OFF);
                    mFlashView.setImageResource(R.drawable.btn_flash_off);
                } else if (mContainer.getFlashMode() == FlashMode.OFF) {
                    mContainer.setFlashMode(FlashMode.AUTO);
                    mFlashView.setImageResource(R.drawable.btn_flash_auto);
                } else if (mContainer.getFlashMode() == FlashMode.AUTO) {
                    mContainer.setFlashMode(FlashMode.TORCH);
                    mFlashView.setImageResource(R.drawable.btn_flash_torch);
                } else if (mContainer.getFlashMode() == FlashMode.TORCH) {
                    mContainer.setFlashMode(FlashMode.ON);
                    mFlashView.setImageResource(R.drawable.btn_flash_on);
                }
                break;
            case R.id.btn_switch_mode:
                if (mIsRecordMode) {
                    mSwitchModeButton.setImageResource(R.drawable.ic_switch_camera);
                    mCameraShutterButton.setVisibility(View.VISIBLE);
                    mRecordShutterButton.setVisibility(View.GONE);
                    //拍照模式下显示顶部菜单
                    mHeaderBar.setVisibility(View.VISIBLE);
                    mIsRecordMode = false;
                    mContainer.switchMode(0);
                    stopRecord();
                } else {
                    mSwitchModeButton.setImageResource(R.drawable.ic_switch_video);
                    mCameraShutterButton.setVisibility(View.GONE);
                    mRecordShutterButton.setVisibility(View.VISIBLE);
                    //录像模式下隐藏顶部菜单
                    mHeaderBar.setVisibility(View.GONE);
                    mIsRecordMode = true;
                    mContainer.switchMode(5);
                }
                break;
            case R.id.btn_shutter_record:
                if (!isRecording) {
                    isRecording = mContainer.startRecord();
                    if (isRecording) {
                        mRecordShutterButton.setBackgroundResource(R.drawable.btn_shutter_recording);
                    }
                } else {
                    stopRecord();
                }
                break;
            case R.id.btn_switch_camera:
                if (mContainer.getCameraNum() < 2) {
                    Toast.makeText(this, "相机不支持翻转", Toast.LENGTH_SHORT).show();
                    return;
                }
                mContainer.switchCamera();
                break;
            default:
                break;
        }
    }


    private void stopRecord() {
        mContainer.stopRecord(this);
        isRecording = false;
        mRecordShutterButton.setBackgroundResource(R.drawable.btn_shutter_record);
    }

    @Override
    public void onTakePictureEnd(Bitmap thumBitmap) {
        mCameraShutterButton.setClickable(true);
    }

    @Override
    public void onAnimtionEnd(Bitmap bm, boolean isVideo) {
        if (bm != null) {
            //生成缩略图
            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bm, 213, 213);
            mThumbView.setImageBitmap(thumbnail);
            if (isVideo)
                mVideoIconView.setVisibility(View.VISIBLE);
            else {
                mVideoIconView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initThumbnail();
        mContainer.previewPicture(this);
        operate = new CommonOperate("5CXhn2aipcjIpiTuO_15-XKgl5zdW9ax", "ws5AOcapJXfJvpT8LZN1F6YrjUPN3L22", false);
        pt = new PreviewThread();
        crun = true;
        takedata = true;
        pt.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        crun = false;
        takedata = false;
        pt = null;
        mContainer.previewPicture(null);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

//        Log.v(TAG, "length: " + takedata);
        if (takedata) {
            Log.v(TAG, "length: " + data.length);
            Camera.Parameters parameters = camera.getParameters();
            imageFormat = parameters.getPreviewFormat();
            w = parameters.getPreviewSize().width;
            h = parameters.getPreviewSize().height;
            pdata = data;
            takedata = false;
        }
    }

    private byte[] pdata = new byte[]{};
    private boolean takedata = true;
    private int w;
    private int h;
    private int imageFormat;
    private Rect rect;

    private class PreviewThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (crun) {
                if (pdata.length < 1) {
                    takedata = true;
                    continue;
                }

                try {
                    ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
                    rect = new Rect(0, 0, w, h);
                    YuvImage yuvImg = new YuvImage(pdata, imageFormat, w, h, null);
                    yuvImg.compressToJpeg(rect, 100, outputstream);
                    Bitmap targetBitmap = BitmapFactory.decodeByteArray(outputstream.toByteArray(),
                            0, outputstream.size());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    targetBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] arrayOfByte = baos.toByteArray();
                    outputstream.close();
                    baos.close();
                    Response response = operate.detectByte(arrayOfByte, 1, "none");
                    Log.v(TAG, "PreviewThread: detectByte: " + new String(response.getContent()));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v(TAG, "22222: " + e.getMessage());
                }
                takedata = true;
            }
        }
    }
}