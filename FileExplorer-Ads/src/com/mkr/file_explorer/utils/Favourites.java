package com.mkr.file_explorer.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;

public class Favourites {

	public static final String FOLDER_NAME = "Favourites";
	public static final String FILE_NAME = "favourites.txt";
	private Context mContext;

	public Favourites(Context context) {
		mContext = context;
	}

	public boolean saveFavouritePath(String favPath) 
	{
		boolean result = false;;
		String internalPath = mContext.getFilesDir().getPath();

		if(!new File(internalPath + FOLDER_NAME).exists()) {
			new File(internalPath + FOLDER_NAME).mkdir();
		}
		File file = new File(mContext.getFilesDir() + FILE_NAME);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file,true));
			BufferedReader br = new BufferedReader(new FileReader(file));
			String temp = null;
			while(( temp = br.readLine() )!= null) {
				if(temp.equals(favPath)) {
					result = true;
				}
			}

			if(!result) {
				bw.write(favPath);
				bw.flush();
				bw.newLine();
			}
			
			br.close();
			bw.close();

		}catch(IOException e){ 
			e.printStackTrace();
			return false;
		}

		return result;
	}

	public String[] getFavouritePaths()  
	{
		String internalPath = mContext.getFilesDir().getPath();
		String[] str;
		try {
			File file = new File(internalPath + FILE_NAME);
			if(file.exists()) {
				if(file.length() == 0) {
					return null;
				} else {
					BufferedReader br = new BufferedReader(new FileReader(file));
					StringBuffer sb = new StringBuffer();
					String temp = null;
					while(( temp = br.readLine())!= null) {
						sb.append(temp+",");
					}
					str = sb.toString().split(",");
					
					br.close();
					return str;
				}
			}
		} catch (Exception  e) { e.printStackTrace(); }

		return null;
	}

	public void deletePath(String path)
	{
		String internalPath = mContext.getFilesDir().getPath();
		File file = new File(internalPath + FILE_NAME);
		File tempFile = new File(internalPath + "favorites.tmp");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			String tempStr = null;
			while ((tempStr = br.readLine()) != null) {
				if (!tempStr.trim().equalsIgnoreCase(path)) {
					pw.println(tempStr);
				}
			}
			pw.flush();
			pw.close();
			br.close();

			tempFile.renameTo(file);

		}catch(IOException e) { e.printStackTrace(); }
	}
}
