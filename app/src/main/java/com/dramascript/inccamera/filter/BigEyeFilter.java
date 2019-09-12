package com.dramascript.inccamera.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.dramascript.inccamera.R;
import com.dramascript.inccamera.filter.face.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/*
 * Cread By DramaScript on 2019/9/11
 *
 * 大眼
 */
public class BigEyeFilter extends AbstractFrameFilter {

    private int left_eye;
    private int right_eye;
    private FloatBuffer left;
    private FloatBuffer right;
    private Face mFace;

    public void setFace(Face face) {
        mFace = face;
    }

    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.bigeye_frag);

        //参数索引
        left_eye = GLES20.glGetUniformLocation(mGLProgramId, "left_eye");
        right_eye = GLES20.glGetUniformLocation(mGLProgramId, "right_eye");

        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
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

        /**
         * 传递眼睛的坐标 给GLSL
         */
        float[] landmarks = mFace.landmarks;
        //左眼的x 、y  opengl : 0-1
        float x = landmarks[2] / mFace.imgWidth;
        float y = landmarks[3] / mFace.imgHeight;
        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        GLES20.glUniform2fv(left_eye,1,left);

        //右眼的x、y
        x = landmarks[4] / mFace.imgWidth;
        y = landmarks[5] / mFace.imgHeight;
        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        GLES20.glUniform2fv(right_eye,1,right);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //因为这一层是摄像头后的第一层，所以需要使用扩展的  GL_TEXTURE_EXTERNAL_OES
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //返回fbo的纹理id
        return mFrameBufferTextures[0];
    }
}
