# IncCamera
从0开始集相机+滤镜+美颜+大眼+贴纸+拍照+录制+直播推流+直播播放+视频与图像编辑为一体的最底层实现，不使用任何商用API。

## 功能
大致实现抖音核心功能，所有C/C++库都来自开源项目：`OpenGL-ES`，`OpenCV`，`OpenSL`，`LibRtmp`，`FFmpeg`，`X264`，`SeetaFaceEngine`，同时支持软硬解切换，demo写之不易，麻烦给个五星好评，哈哈哈！

## 进度
2019-9-12: 实现了录制，美颜，大眼，贴纸功能

2019-9-19：实现本地视频播放以及拖拽播放，rtmp/http直播流播放。

## 注意事项
本工程由于`OpenCV`是3.4版本，为了支持`-DANDROID_STL=gnustl_static`，所以博主当前NDK版本是17，Android studio版本3.5，如果一定要17+的版本，可以将OpenCV库替换成4.x库满足要求，如遇问题，具体去官网参考NDK版本更新日志。如有问题请联系qq：1109730524。感想支持！
