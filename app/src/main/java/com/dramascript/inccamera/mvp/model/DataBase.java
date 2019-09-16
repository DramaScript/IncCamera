package com.dramascript.inccamera.mvp.model;

import com.dramascript.inccamera.App;
import com.dramascript.inccamera.mvp.model.gen.DaoMaster;
import com.dramascript.inccamera.mvp.model.gen.DaoSession;
import com.dramascript.inccamera.mvp.model.gen.ImageCacheDao;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/*
 * Cread By DramaScript on 2019/9/16
 */
public class DataBase {

    private static DataBase dataBase;
    private DaoSession daoSession;

    public static DataBase getDataBase() {
        if (dataBase == null) {
            synchronized (DataBase.class) {
                if (dataBase == null) {
                    dataBase = new DataBase();
                }
            }
        }
        return dataBase;
    }

    private DaoSession getDaoSession() {
        if (daoSession == null) {
            DaoMaster.DevOpenHelper helper = new MySQLiteOpenHelper(App.getInstance());
            DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public void insertCache(ImageCache imageCache) {
        getDaoSession().insertOrReplace(imageCache);
    }

    public Observable<List<ImageCache>> getAllCacheList(){
        return Observable.create(new ObservableOnSubscribe<List<ImageCache>>() {
            @Override
            public void subscribe(ObservableEmitter<List<ImageCache>> emitter) throws Exception {
                List<ImageCache> chatList = getDaoSession().queryBuilder(ImageCache.class).list();
                emitter.onNext(chatList);
                emitter.onComplete();
            }
        });
    }

    public List<ImageCache> getAllCache(){
        return getDaoSession().queryBuilder(ImageCache.class).list();
    }

    public void delet(long id){
        getDaoSession().queryBuilder(ImageCache.class).where(ImageCacheDao.Properties.Id.eq(id)).buildDelete().executeDeleteWithoutDetachingEntities();
    }
}
