package com.dramascript.inccamera.utils;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.dramascript.inccamera.filter.ScreenFilter;

/*
 * Cread By DramaScript on 2019/9/12
 *
 * EGL配置 与 录制的opengl操作  工具类
 */
public class EGLBase {

    private ScreenFilter mScreenFilter;
    private EGLSurface mEglSurface;
    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;

    /**
     * @param context
     * @param width
     * @param height
     * @param surface    MediaCodec创建的surface 我们需要将其贴到我们的虚拟屏幕上去
     * @param eglContext GLThread的EGL上下文
     */
    public EGLBase(Context context, int width, int height, Surface surface, EGLContext eglContext) {
        //配置EGL环境
        createEGL(eglContext);
        //把Surface贴到  mEglDisplay ，发生关系
        int[] attrib_list = {
                EGL14.EGL_NONE
        };
        // 绘制线程中的图像 就是往这个mEglSurface 上面去画
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, attrib_list, 0);
        // 绑定当前线程的显示设备及上下文， 之后操作opengl，就是在这个虚拟显示上操作
        if (!EGL14.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)) {
            throw  new RuntimeException("eglMakeCurrent 失败！");
        }
        //像虚拟屏幕画
        mScreenFilter = new ScreenFilter(context);
        mScreenFilter.onReady(width,height);
    }

    private void createEGL(EGLContext eglContext) {
        //创建 虚拟显示器
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }
        //初始化显示器
        int[] version = new int[2];
        // 12.1020203
        //major：主版本 记录在 version[0]
        //minor : 子版本 记录在 version[1]
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }
        // egl 根据我们配置的属性 选择一个配置
        int[] attrib_list = {
                EGL14.EGL_RED_SIZE, 8, // 缓冲区中 红分量 位数
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //egl版本 2
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        // attrib_list：属性列表+属性列表的第几个开始
        // configs：获取的配置 (输出参数)
        //num_config: 长度和 configs 一样就行了
        if (!EGL14.eglChooseConfig(mEglDisplay, attrib_list, 0,
                configs, 0, configs.length, num_config, 0)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        mEglConfig = configs[0];
        int[] ctx_attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, //egl版本 2
                EGL14.EGL_NONE
        };
        //创建EGL上下文
        // 3 share_context: 共享上下文 传绘制线程(GLThread)中的EGL上下文 达到共享资源的目的 发生关系
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, eglContext, ctx_attrib_list
                , 0);
        // 创建失败
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL Context Error.");
        }
    }

    /**
     *
     * @param textureId 纹理id 代表一个图片
     * @param timestamp 时间戳
     */
    public void draw(int textureId,long timestamp){
        // 绑定当前线程的显示设备及上下文， 之后操作opengl，就是在这个虚拟显示上操作
        if (!EGL14.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)) {
            throw  new RuntimeException("eglMakeCurrent 失败！");
        }
        //画画 画到虚拟屏幕上
        mScreenFilter.onDrawFrame(textureId);
        //刷新eglsurface的时间戳
        EGLExt.eglPresentationTimeANDROID(mEglDisplay,mEglSurface,timestamp);

        //交换数据
        //EGL的工作模式是双缓存模式， 内部有两个frame buffer (fb)
        //当EGL将一个fb  显示屏幕上，另一个就在后台等待opengl进行交换
        EGL14.eglSwapBuffers(mEglDisplay,mEglSurface);
    }

    /**
     * 回收
     */
    public void release(){
        EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
    }
}
