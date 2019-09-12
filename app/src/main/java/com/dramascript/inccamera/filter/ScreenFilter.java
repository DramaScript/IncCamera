package com.dramascript.inccamera.filter;

import android.content.Context;

import com.dramascript.inccamera.R;

/*
 * Cread By DramaScript on 2019/9/11
 *
 * 负责往屏幕上渲染
 */
public class ScreenFilter extends AbstractFilter {
    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex,R.raw.base_frag);
    }
}
