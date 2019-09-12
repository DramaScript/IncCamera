package com.dramascript.dlibrary.base;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dramascript.dlibrary.Config;
import com.dramascript.dlibrary.R;
import com.dramascript.dlibrary.utils.ActivityUtils;
import com.dramascript.dlibrary.utils.StatusBarUtils;
import com.jaeger.library.StatusBarUtil;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/*
 * Cread By DramaScript on 2019/3/5
 */
public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    protected Context context;
    private LinearLayout content;
    private Toolbar toolbar;
    protected ActionBar actionBar;
    protected TextView toolbarTitle, rightTitle;
    protected ImageView rightIv;
    protected LinearLayout toolbarRightLayout;
    private boolean isWhiteStatusBar;
    protected int mContentViewId;
    private boolean isNoActionBar;

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置app统一的背景色
        ViewGroup vg = (ViewGroup) findViewById(android.R.id.content);
        if (vg != null) {
            vg.removeAllViews();
            content = new LinearLayout(this);
            //统一设置背景为白色
            content.setBackgroundColor(getResources().getColor(R.color.white));
            content.setOrientation(LinearLayout.VERTICAL);
            vg.addView(content);
        }
        if (getSupportActionBar() == null) {
            isNoActionBar = true;
            initToolbar(null, false);
        }
        if (getClass().isAnnotationPresent(DInject.class)) {
            DInject annotation = getClass()
                    .getAnnotation(DInject.class);
            mContentViewId = annotation.contentViewId();
        } else {
            throw new RuntimeException(
                    "Activity must add annotations of DInitParams.class");
        }
        setContentView(mContentViewId);
        context = this;
        init();
        ActivityUtils.addActivity(getLocalClassName(), this);
    }

    protected void init() {

    }

    public void setToolbarColor(int color) {
        if (!isWhiteStatusBar && color != -1 && toolbar != null && isNoActionBar) { // 只有设置非白色的 isWhiteStatusBar为false时才生效
            toolbar.setBackgroundColor(color);
            StatusBarUtil.setColor(this, color, 0);
        }

    }

    public void setToolbarTitle(String title) {
        if (toolbarTitle != null && isNoActionBar)
            toolbarTitle.setText(title);
    }

    public void setToolbarTitleColor(int color) {
        if (toolbarTitle != null && isNoActionBar)
            toolbarTitle.setTextColor(color);
    }

    public void setRightTitle(String title) {
        if (rightTitle != null && isNoActionBar)
            rightTitle.setText(title);
    }

    public void setRightIv(int resId) {
        if (rightIv != null && isNoActionBar)
            rightIv.setBackgroundResource(resId);
    }

    public void setBackGone() {
        if (toolbar != null && isNoActionBar)
            toolbar.setNavigationIcon(null);// 隐藏返回按钮
    }

    @Override
    public void setContentView(int layoutResID) {
        if (layoutResID != -1)
            LayoutInflater.from(this).inflate(layoutResID, content, true);
    }

    @Override
    public void setContentView(View view) {
        content.addView(view);
    }

    public LinearLayout getContentView() {
        return this.content;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        ActivityUtils.removeActivity(getLocalClassName());
        super.onDestroy();
    }

    /**
     * @param title   为null就是不显示返回栏，为""就是显示不过标题为空
     * @param isWhite
     */
    protected void initToolbar(String title, boolean isWhite) {
        isWhiteStatusBar = isWhite;
        if (title != null) {
            if (isWhiteStatusBar) {
                StatusBarUtils.setWhiteStateBar(this);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    StatusBarUtils.addStatusBar(this, content, -1);
            } else {

            }

            View view = LayoutInflater.from(this).inflate(R.layout.toolbar, content, true);
            toolbar = view.findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            toolbarTitle = view.findViewById(R.id.toolbar_title);
            rightTitle = view.findViewById(R.id.right_title);
            rightIv = view.findViewById(R.id.right_image);
            toolbarRightLayout = view.findViewById(R.id.right_layout);

            if (actionBar != null) {
                toolbarTitle.setText(title);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
            }

            //给标题栏添加阴影效果
            LayoutInflater.from(this).inflate(R.layout.view_toolbar_shadow, content, true);
        } else {
            if (!(StatusBarUtils.isFlyme() && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)) {
                StatusBarUtil.setTranslucentForImageViewInFragment(this, null);
            }
        }

    }

    public void startActivity(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    public void startActivity(Intent intent, Class activityClass) {
        intent.setClass(this, activityClass);
        startActivity(intent);
    }

    public void startActivity(Class activityClass, Bundle bundle) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra(Config.ACTIVITY_BUNDLE, bundle);
        startActivity(intent);
    }

    public void startActivityForResult(Class activityClass, Bundle bundle, int type) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra(Config.ACTIVITY_BUNDLE, bundle);
        startActivityForResult(intent, type);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        StringBuffer sb = new StringBuffer();
        for (String str : perms) {
            sb.append(str);
            sb.append("\n");
        }
        sb.replace(sb.length() - 2, sb.length(), "");
        //用户点击拒绝并不在询问时候调用
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Toast.makeText(this, "已拒绝权限" + sb + "并不再询问", Toast.LENGTH_SHORT).show();
            new AppSettingsDialog.Builder(this)
                    .setRationale("此功能需要" + sb + "权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("好")
                    .setNegativeButton("不行")
                    .build()
                    .show();
        }
    }
}