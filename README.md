[![](https://jitpack.io/v/sovegetables/qrcode-android.svg)](https://jitpack.io/#sovegetables/qrcode-android)

## Fork:[BGAQRCode-Android](https://github.com/bingoogolapple/BGAQRCode-Android)
## 使用文档请参考[BGAQRCode-Android](https://github.com/bingoogolapple/BGAQRCode-Android)
```code
    maven { url 'https://jitpack.io' }

    dependencies {
	        implementation 'com.github.sovegetables:qrcode-android:0.2.0'
	}
```
## 优化
- 移除一维条码支持
- 移除zbar的支持
- 更新zxing至3.3.2
- 优化QRCodeView.onPreviewFrame方法逻辑，不断并行解析图片帧
- 弃用PlanarYUVLuminanceSource, 改用bitmap.getPixels获取像素byte[]数据

## License

    Copyright (C) 2012 The Android Open Source Project
    Copyright 2014 sovegetables

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
