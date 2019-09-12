//
// Created by Lance on 2017/11/6.
//

#include "FaceTrack.h"


FaceTrack::FaceTrack(const char *model, const char *seeta) {
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    DetectionBasedTracker::Parameters detectorParams;
    //追踪器
    tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, detectorParams);

    faceAlignment = makePtr<seeta::FaceAlignment>(seeta);
}


//开启追踪器
void FaceTrack::startTracking() {
    tracker->run();
}

//关闭追踪器
void FaceTrack::stopTracking() {
    tracker->stop();
}


void FaceTrack::detector(Mat src, vector<Rect2f> &rects) {
    vector<Rect> faces;
    //src :图片 gray
    tracker->process(src);
    tracker->getObjects(faces);
    if (faces.size()) {
        Rect face = faces[0];
        rects.push_back(Rect2f(face.x, face.y, face.width, face.height));

        //关键点定位
        //保存5个关键点的坐标
        // 0:左眼  1:右眼  2:鼻头  3:嘴巴左  4:嘴巴右
        seeta::FacialLandmark points[5];
        //图像数据
        seeta::ImageData image_data(src.cols,src.rows);
        image_data.data = src.data;
        //指定人脸部位
        seeta::FaceInfo faceInfo;
        seeta::Rect bbox;
        bbox.x = face.x;
        bbox.y  = face.y;
        bbox.width = face.width;
        bbox.height = face.height;
        faceInfo.bbox = bbox;
        faceAlignment->PointDetectLandmarks(image_data, faceInfo, points);

        for (int i = 0; i < 5; ++i) {
            //把点放入返回的集合
            rects.push_back(Rect2f(points[i].x,points[i].y,0,0));
        }

    }
}
