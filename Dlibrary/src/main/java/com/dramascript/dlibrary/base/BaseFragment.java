package com.dramascript.dlibrary.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dramascript.dlibrary.Config;

/*
 * Cread By DramaScript on 2019/3/5
 */
public abstract class BaseFragment extends Fragment {

    protected Context mContext;
    protected View rootView;
    private boolean isViewPrepared; // 标识fragment视图已经初始化完毕
    private boolean hasFetchData; // 标识已经触发过懒加载数据
    private int mContentViewId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null) {
            this.mContext = context;
        } else {
            this.mContext = getActivity();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            if (getClass().isAnnotationPresent(DInject.class)) {
                DInject annotation = getClass()
                        .getAnnotation(DInject.class);
                mContentViewId = annotation.contentViewId();
            }else {
                throw new RuntimeException(
                        "Fragment must add annotations of DInitParams.class");
            }
            rootView = inflater.inflate(mContentViewId, container, false);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewPrepared = true;
        init(view);
        lazyFetchDataIfPrepared();
    }

    protected void init(View view) {
    }

    private void lazyFetchDataIfPrepared() {
        // 用户可见fragment && 没有加载过数据 && 视图已经准备完毕
        if (getUserVisibleHint() && !hasFetchData && isViewPrepared) {
            hasFetchData = true;
            lazyFetchData();
        }
    }

    /**
     * 懒加载的方式获取数据，仅在满足fragment可见和视图已经准备好的时候调用一次
     */
    protected void lazyFetchData() {
    }

    protected void onFragmentVisibleChange(boolean isVisible) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            lazyFetchDataIfPrepared();
        }
        if (isViewPrepared){
            onFragmentVisibleChange(isVisibleToUser);
        }
    }

    @Override
    public void onDestroyView() {
        // view被销毁后，将可以重新触发数据懒加载，因为在viewpager下，fragment不会再次新建并走onCreate的生命周期流程，将从onCreateView开始
        hasFetchData = false;
        isViewPrepared = false;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startActivity(Class activityClass) {
        Intent intent = new Intent(mContext, activityClass);
        startActivity(intent);
    }

    public void startActivity(Intent intent, Class activityClass) {
        intent.setClass(mContext, activityClass);
        startActivity(intent);
    }

    public void startActivity(Class activityClass, Bundle bundle) {
        Intent intent = new Intent(mContext, activityClass);
        intent.putExtra(Config.ACTIVITY_BUNDLE, bundle);
        startActivity(intent);
    }
}
