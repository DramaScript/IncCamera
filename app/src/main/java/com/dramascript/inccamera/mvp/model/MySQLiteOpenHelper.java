package com.dramascript.inccamera.mvp.model;

import android.content.Context;

import com.dramascript.inccamera.mvp.model.gen.DaoMaster;
import com.dramascript.inccamera.mvp.model.gen.ImageCacheDao;
import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

/*
 * Cread By DramaScript on 2019/9/16
 */
public class MySQLiteOpenHelper extends DaoMaster.DevOpenHelper {

    private static final String DB_NAME = "image.db";

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.DEBUG = true;
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);
            }
        }, ImageCacheDao.class);//ChatInfoDao是数据库实体类自动生成得，这里需要把所有的都加上
    }
}
