package com.huiyuenet.faceCheck;

public class THFQ_Result {

    public int brightness;  // 人脸亮度，只有3种可能的值:[-1->太暗，0->正常，1->太亮]，亮度结果会受亮度阈值参数brightness_min和brightness_max影响
    public int occlusion;  // 人脸遮挡度，范围值为0-100,越大表示人脸遮挡程度越高
    public int hat;  // 带帽子,范围为0-100，越大表示越可能有佩戴帽子，建议判别阈值为50
    public int blur;  // 人脸模糊度,范围值为0-100,越大表示图像越模糊，建议人脸模糊度判别阈值为70
    public int glasses;  // 带眼镜,范围为0-100，越大表示越可能有戴眼镜，建议判别阈值为70

}
