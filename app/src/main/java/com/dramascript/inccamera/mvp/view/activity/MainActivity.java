package com.dramascript.inccamera.mvp.view.activity;


import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.dramascript.dlibrary.base.DInject;
import com.dramascript.dlibrary.base.adapter.listadapter.CommonAdapter;
import com.dramascript.dlibrary.base.adapter.listadapter.ViewHolder;
import com.dramascript.dlibrary.imageloader.ImageLoader;
import com.dramascript.inccamera.App;
import com.dramascript.inccamera.R;
import com.dramascript.inccamera.imp.ImpMvpActivity;
import com.dramascript.inccamera.mvp.model.ImageCache;
import com.dramascript.inccamera.mvp.presenter.CachePresenter;
import com.dramascript.inccamera.mvp.presenter.contract.CacheContract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

@DInject(
        contentViewId = R.layout.activity_main
)
public class MainActivity extends ImpMvpActivity<CachePresenter> implements CacheContract.View {

    @BindView(R.id.gridView)
    GridView gridView;
    @BindView(R.id.tvEmpty)
    TextView tvEmpty;
    private List<ImageCache> lists;
    private CommonAdapter<ImageCache> adapter;

    @Override
    protected void initToolbar(String title, boolean isWhite) {
        super.initToolbar("", isWhite);// title不为null显示toolbar
    }

    @Override
    protected void init() {
        super.init();
        setToolbarColor(getResources().getColor(R.color.colorPrimary));
        setToolbarTitle(getString(R.string.app_name));
        setToolbarTitleColor(getResources().getColor(R.color.white));
        setBackGone();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(CameraActivity.class);
            }
        });
        lists = new ArrayList<>();
        adapter = new CommonAdapter<ImageCache>(this, R.layout.item_album, lists) {
            @Override
            protected void convert(ViewHolder viewHolder, ImageCache item, int position) {
                ImageView ivPriv = viewHolder.getView(R.id.ivPriv);
                if (item.getType() == 1) {
                    viewHolder.setVisible(R.id.ivPlay,true);
                    Glide.with(context).load(Uri.fromFile(new File(item.getImagePath()))).placeholder(R.drawable.bg_greg).into(ivPriv);
                } else {
                    ImageLoader.load(App.getInstance(),item.getImagePath(),ivPriv,R.drawable.bg_greg,R.drawable.bg_greg);
                    viewHolder.setVisible(R.id.ivPlay,false);
                }
            }
        };
        gridView.setAdapter(adapter);
        mPresenter.getCache();
    }

    @Override
    protected CachePresenter initPresenter() {
        return new CachePresenter();
    }

    @Override
    public void cahceResult(List<ImageCache> list) {
        LogUtils.e(list.get(0).getImagePath());
        if (list.size() != 0) {
            tvEmpty.setVisibility(View.GONE);
            lists.clear();
            lists.addAll(list);
            adapter.notifyDataSetChanged();
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("直播播放");
        menu.add("直播推流");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getTitle()+""){
            case "直播播放":
                Intent intent = new Intent(this,IncPlayerActivity.class);
                intent.putExtra("url","rtmp://rtmp.miaobolive.com/live/5319085ed4a787408fbe486586f52540");
                ActivityUtils.startActivity(intent);
                break;
            case "直播推流":

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
