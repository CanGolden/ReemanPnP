package com.jcan.reemanpnp.forcamera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jcan.reemanpnp.BaseActivity;
import com.jcan.reemanpnp.R;
import com.linj.FileOperateUtil;
import com.linj.album.view.AlbumViewPager;
import com.linj.album.view.AlbumViewPager.OnPlayVideoListener;
import com.linj.album.view.MatrixImageView.OnSingleTapListener;
import com.linj.video.view.VideoPlayerContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumItemAty extends BaseActivity implements OnClickListener, OnSingleTapListener
        , OnPlayVideoListener {
    public final static String TAG = "AlbumDetailAty";
    private AlbumViewPager mViewPager;//显示大图
    private VideoPlayerContainer mContainer;
    private ImageView mBackView;
    private TextView mCountView;
    private View mHeaderBar, mBottomBar;
    private Button mDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumitem);

        mViewPager = (AlbumViewPager) findViewById(R.id.albumviewpager);
        mContainer = (VideoPlayerContainer) findViewById(R.id.videoview);
        mBackView = (ImageView) findViewById(R.id.header_bar_photo_back);
        mCountView = (TextView) findViewById(R.id.header_bar_photo_count);
        mHeaderBar = findViewById(R.id.album_item_header_bar);
        mBottomBar = findViewById(R.id.album_item_bottom_bar);
        mDeleteButton = (Button) findViewById(R.id.delete);

        mBackView.setOnClickListener(this);
        mCountView.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);

        mViewPager.setOnPageChangeListener(pageChangeListener);
        mViewPager.setOnSingleTapListener(this);
        mViewPager.setOnPlayVideoListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlbum();
    }

    /**
     * 加载图片
     */
    public void loadAlbum() {

        String currentFileName = null;
        if (getIntent().getExtras() != null)
            currentFileName = getIntent().getExtras().getString("path");
        if (currentFileName != null) {
            File file = new File(currentFileName);
            currentFileName = file.getName();
            if (currentFileName.indexOf(".") > 0)
                currentFileName = currentFileName.substring(0, currentFileName.lastIndexOf("."));
        }
        //获取根目录下缩略图文件夹
        String folder = FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_IMAGE, FileConfig.mSaveRoot);
        String thumbnailFolder = FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, FileConfig.mSaveRoot);
        //获取图片文件大图
        List<File> imageList = FileOperateUtil.listFiles(folder, ".jpg");
        //获取视频文件缩略图
        List<File> videoList = FileOperateUtil.listFiles(thumbnailFolder, ".jpg", "video");
        List<File> files = new ArrayList<File>();
        //将视频文件缩略图加入图片大图列表中
        if (videoList != null && videoList.size() > 0) {
            files.addAll(videoList);
        }
        if (imageList != null && imageList.size() > 0) {
            files.addAll(imageList);
        }
        FileOperateUtil.sortList(files, false);
        if (files.size() > 0) {
            List<String> paths = new ArrayList<String>();
            int currentItem = 0;
            for (File file : files) {
                if (currentFileName != null && file.getName().contains(currentFileName))
                    currentItem = files.indexOf(file);
                paths.add(file.getAbsolutePath());
            }
            mViewPager.setAdapter(mViewPager.new ViewPagerAdapter(paths));
            mViewPager.setCurrentItem(currentItem);
            mCountView.setText((currentItem + 1) + "/" + paths.size());
        } else {
            mCountView.setText("0/0");
        }
    }


    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if (mViewPager.getAdapter() != null) {
                String text = (position + 1) + "/" + mViewPager.getAdapter().getCount();
                mCountView.setText(text);
            } else {
                mCountView.setText("0/0");
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    @Override
    public void onSingleTap() {
        if (mHeaderBar.getVisibility() == View.VISIBLE) {
            AlphaAnimation animation = new AlphaAnimation(1, 0);
            animation.setDuration(300);
            mHeaderBar.startAnimation(animation);
            mBottomBar.startAnimation(animation);
            mHeaderBar.setVisibility(View.GONE);
            mBottomBar.setVisibility(View.GONE);
        } else {
            AlphaAnimation animation = new AlphaAnimation(0, 1);
            animation.setDuration(300);
            mHeaderBar.startAnimation(animation);
            mBottomBar.startAnimation(animation);
            mHeaderBar.setVisibility(View.VISIBLE);
            mBottomBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_bar_photo_back:
                finish();
                break;
            case R.id.delete:
                String result = mViewPager.deleteCurrentPath();
                if (result != null)
                    mCountView.setText(result);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mContainer.getVisibility() == View.VISIBLE)
            mContainer.stopPlay();
        else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        if (mContainer.getVisibility() == View.VISIBLE)
            mContainer.stopPlay();
        super.onStop();
    }

    @Override
    public void onPlay(String path) {
        try {
            mContainer.playVideo(path);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}