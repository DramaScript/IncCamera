package com.dramascript.inccamera.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.dramascript.inccamera.R;
import com.dramascript.inccamera.filter.face.Face;
import com.dramascript.inccamera.utils.OpenGLUtils;

/*
 * Cread By DramaScript on 2019/9/12
 *
 * 贴纸滤镜
 */
public class StickFilter extends AbstractFrameFilter {

    private Bitmap mBitmap;
    private int[] mTextureId;
    private Face mFace;

    public StickFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_frag);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.erduo_000);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        // opengl 纹理 id
        // 把Bitmap 存放到opengl的纹理中
        mTextureId = new int[1];
        OpenGLUtils.glGenTextures(mTextureId);
        //表示后续的操作 就是作用于这个纹理上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId[0]);
        // 将 Bitmap与纹理id 绑定起来
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,mBitmap,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }

    public void setFace(Face face){
        mFace = face;
    }

    @Override
    public int onDrawFrame(int textureId) {
        if (null == mFace) {
            return textureId;
        }
        //设置显示窗口
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        //不调用的话就是默认的操作glsurfaceview中的纹理了。显示到屏幕上了
        //这里我们还只是把它画到fbo中(缓存)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        //使用着色器
        GLES20.glUseProgram(mGLProgramId);

        //传递坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //因为这一层是摄像头后的第一层，所以需要使用扩展的  GL_TEXTURE_EXTERNAL_OES
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        onDrawStick();
        //返回fbo的纹理id
        return mFrameBufferTextures[0];
    }

    private void onDrawStick() {
        //帖纸画上去
        //开启混合模式 ： 将多张图片进行混合(贴图)
        GLES20.glEnable(GLES20.GL_BLEND);
        //设置贴图模式
        // 1：src 源图因子 ： 要画的是源  (耳朵)
        // 2: dst : 已经画好的是目标  (从其他filter来的图像)
        //画耳朵的时候  GL_ONE:就直接使用耳朵的所有像素 原本是什么样子 我就画什么样子
        // 表示用1.0减去源颜色的alpha值来作为因子
        //  耳朵不透明 (0,0 （全透明）- 1.0（不透明）) 目标图对应位置的像素就被融合掉了 不见了
        GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //画画
        // 画耳朵 不是画全屏 定位到人脸的位置
        //设置显示窗口
        //人脸起始的位置
        float x = mFace.landmarks[0];
        float y = mFace.landmarks[1];
        //转换为要画到屏幕上的宽、高
        x = x / mFace.imgWidth * mOutputWidth;
        y = y / mFace.imgHeight * mOutputHeight;
        // mFace.width： 人脸的宽
        GLES20.glViewport((int)x, (int)y- mBitmap.getHeight()/2,
                (int) ((float)mFace.width /mFace.imgWidth * mOutputWidth),
                mBitmap.getHeight());

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        //使用着色器
        GLES20.glUseProgram(mGLProgramId);
        //传递坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //因为这一层是摄像头后的第一层，所以需要使用扩展的  GL_TEXTURE_EXTERNAL_OES
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //关闭
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void release() {
        super.release();
        mBitmap.recycle();
    }
}
