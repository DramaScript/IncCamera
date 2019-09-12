package com.dramascript.inccamera.utils;

import android.content.Context;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * Cread By DramaScript on 2019/9/11
 */
public class OpenGLUtils {

    /**
     * 创建纹理并配置
     */
    public static void glGenTextures(int[] textures) {
        //创建
        GLES20.glGenTextures(textures.length, textures, 0);
        //配置
        for (int i = 0; i < textures.length; i++) {
            // opengl的操作 面向过程的操作
            // bind 就是绑定 ，表示后续的操作就是在这一个 纹理上进行
            // 后面的代码配置纹理，就是配置bind的这个纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            /**
             * 过滤参数
             *  当纹理被使用到一个比他大 或者比他小的形状上的时候 该如何处理
             */
            // 放大
            // GLES20.GL_LINEAR  : 使用纹理中坐标附近的若干个颜色，通过平均算法 进行放大
            // GLES20.GL_NEAREST : 使用纹理坐标最接近的一个颜色作为放大的要绘制的颜色
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            /*设置纹理环绕方向*/
            //纹理坐标 一般用st表示，其实就是x y
            //纹理坐标的范围是0-1。超出这一范围的坐标将被OpenGL根据GL_TEXTURE_WRAP参数的值进行处理
            //GL_TEXTURE_WRAP_S, GL_TEXTURE_WRAP_T 分别为x，y方向。
            //GL_REPEAT:平铺
            //GL_MIRRORED_REPEAT: 纹理坐标是奇数时使用镜像平铺
            //GL_CLAMP_TO_EDGE: 坐标超出部分被截取成0、1，边缘拉伸
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            //解绑
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        }
    }

    /**
     * 根据顶点与片元着色器 加载Program
     * @param vSource
     * @param fSource
     * @return
     */
    public static int loadProgram(String vSource,String fSource){
        /**
         * 顶点着色器
         */
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        //加载着色器代码
        GLES20.glShaderSource(vShader,vSource);
        //编译（配置）
        GLES20.glCompileShader(vShader);

        //查看配置 是否成功
        int[] status = new int[1];
        GLES20.glGetShaderiv(vShader,GLES20.GL_COMPILE_STATUS,status,0);
        if(status[0] != GLES20.GL_TRUE){
            //失败
            throw new IllegalStateException("load vertex shader:"+GLES20.glGetShaderInfoLog(vShader));
        }

        /**
         *  片元着色器
         *  流程和上面一样
         */
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        //加载着色器代码
        GLES20.glShaderSource(fShader,fSource);
        //编译（配置）
        GLES20.glCompileShader(fShader);

        //查看配置 是否成功
        GLES20.glGetShaderiv(fShader,GLES20.GL_COMPILE_STATUS,status,0);
        if(status[0] != GLES20.GL_TRUE){
            //失败
            throw new IllegalStateException("load fragment shader:"+GLES20.glGetShaderInfoLog(vShader));
        }

        /**
         * 创建着色器程序
         */
        int program = GLES20.glCreateProgram();
        //绑定顶点和片元
        GLES20.glAttachShader(program,vShader);
        GLES20.glAttachShader(program,fShader);
        //链接着色器程序
        GLES20.glLinkProgram(program);
        //获得状态
        GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,status,0);
        if(status[0] != GLES20.GL_TRUE){
            throw new IllegalStateException("link program:"+GLES20.glGetProgramInfoLog(program));
        }
        GLES20.glDeleteShader(vShader);
        GLES20.glDeleteShader(fShader);
        return program;
    }

    public static String readRawTextFile(Context context, int rawId) {
        InputStream is = context.getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void copyAssets2SdCard(Context context, String src, String dst) {
        try {
            File file = new File(dst);
            if (!file.exists()) {
                InputStream is = context.getAssets().open(src);
                FileOutputStream fos = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[2048];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
