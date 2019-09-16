package com.dramascript.inccamera.mvp.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ImageCache {

    @Id(autoincrement = true)
    Long id;

    @Unique
    String imagePath;// 存储地址
    int type;// 视频还是图片
    @Generated(hash = 1061016400)
    public ImageCache(Long id, String imagePath, int type) {
        this.id = id;
        this.imagePath = imagePath;
        this.type = type;
    }
    @Generated(hash = 258803250)
    public ImageCache() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getImagePath() {
        return this.imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }

}
