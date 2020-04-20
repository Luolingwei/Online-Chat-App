package com.imooc.controller;

import com.imooc.utils.QRCodeUtils;

import java.io.File;

public class test extends BasicController{

    public static void main(String[] args) {
        // 为每个用户生成唯一的二维码
        String uploadPathDB = "/" + "abc123" + "/qrcode/qrcode.png";
        String localqrCodePath = FILE_SPACE + uploadPathDB;
        String dirPath = FILE_SPACE + "/" + "abc123" + "/qrcode";

        QRCodeUtils qrCodeUtils = new QRCodeUtils();

        File file = new File(dirPath);
        boolean result = file.mkdirs();

        qrCodeUtils.createQRCode(localqrCodePath,"InstaChat_qrcode:" + "aaa");
        System.out.println("make dirs: " + result);

    }

}
