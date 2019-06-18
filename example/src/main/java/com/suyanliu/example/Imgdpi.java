package com.suyanliu.example;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Imgdpi {
	public static void main(String[] args) {
	    String path = "/data/0F90E85822338D10ECBD37D070DA87F3.jpg";
	    File file = new File(path);
	    //handleDpi(file, 300, 300);
	}
	
	/**
	 * 改变图片DPI
	 *
	 * @param file
	 * @param xDensity
	 * @param yDensity
	 */
//	public static void handleDpi(File file, int xDensity, int yDensity) {
//	    try {
//	        BufferedImage image = ImageIO.read(file);
//	        JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(new FileOutputStream(file));
//	        JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(image);
//	        jpegEncodeParam.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
//	        jpegEncoder.setJPEGEncodeParam(jpegEncodeParam);
//	        jpegEncodeParam.setQuality(0.75f, false);
//	        jpegEncodeParam.setXDensity(xDensity);
//	        jpegEncodeParam.setYDensity(yDensity);
//	        jpegEncoder.encode(image, jpegEncodeParam);
//	        image.flush();
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	}
	
}

