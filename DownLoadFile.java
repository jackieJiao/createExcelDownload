package com.houtai.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;

import sun.misc.BASE64Encoder;

public class DownLoadFile { 
	
	public static void checkDoc(HttpServletRequest request, HttpServletResponse response,String docPath) {
		File f=new File(docPath);
		System.out.println(f.exists());
		String filename = docPath.substring(docPath.lastIndexOf("/") + 1);
		try {
			String downloadFilename = filename;
			String agent = request.getHeader("User-Agent");
			if (agent.contains("MSIE")) {
				downloadFilename = URLEncoder.encode(filename, "utf-8");
				downloadFilename = filename.replace("+", " ");
			} else if (agent.contains("Firefox")) {
				BASE64Encoder base64Encoder = new BASE64Encoder();
				downloadFilename = "=?utf-8?B?" + base64Encoder.encode(filename.getBytes("utf-8")) + "?=";
			} else {
				downloadFilename = URLEncoder.encode(filename, "utf-8");

			}
			response.setContentType(request.getSession().getServletContext().getMimeType(filename));
			response.setHeader("Content-Disposition", "attachment;filename=" + downloadFilename);

//			InputStream in = new FileInputStream("/" + docPath);
			InputStream in = new FileInputStream(docPath);
			OutputStream out = response.getOutputStream();
			IOUtils.copy(in, out);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
