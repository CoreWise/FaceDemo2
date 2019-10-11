package com.cw.facedemo;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.EnumSet;

/**
 * 自定义Log类，只有当LEVEL常量的值小于或等于对应日志级别值的时候，才会将日志打印出来.
 * */
public class FileUtil {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void traverseFolder(final String folder, final int depth, SimpleFileVisitor<Path> finder) {

        final Path path = Paths.get(folder);

        try{
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), depth, finder);
        }catch(IOException e){
        }
    }

    public static void saveData(byte[] data, String filePath, String fileName){
        File dir = new File(filePath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
//        String fileNameNew = new SimpleDateFormat("MM-dd-kk-mm-ss").format(new Date()) + "_" + String.format("%04d_",seq)+fileName;

        //File pictureFile = new File(dir, fileName);
        File pictureFile = new File(dir, fileName);
        FileOutputStream fOut = null;
        try {

//    		byte[] bitmap = convertToBitmapArray(data, imageH,imageW);
            fOut = new FileOutputStream(pictureFile);
            fOut.write(data, 0, data.length);
            fOut.close();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (null != fOut) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
