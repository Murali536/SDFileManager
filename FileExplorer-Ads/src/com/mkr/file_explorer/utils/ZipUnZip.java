package com.mkr.file_explorer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.mkr.file_explorer.FileExtensions;
import com.mkr.file_explorer.ItemOperations;

/**
 * This class is used to ZIP and UNZIP operations
 * 
 * @author Manthena Murali
 *
 */
public class ZipUnZip {

	private int mParentFilePathLength = -1;
	
	/**
	 * Create a new zip file
	 *  
	 * @param sourcePath file path which has to be zipped 
	 * @param zipName zip file name
	 */
	public void createZIP(final String sourcePath, final String zipName) {
		final File src = new File (sourcePath);
		//add the zip file extension
		String outputZIPFileName;
		if (src.isDirectory()) { 
			outputZIPFileName = zipName + FileExtensions.ZIP_EXTENSION;
		}
		else {
			final int lastDot = zipName.lastIndexOf('.');
			if (lastDot != -1) {
				outputZIPFileName = zipName.substring(0, lastDot) + FileExtensions.ZIP_EXTENSION;  
			} else {
				outputZIPFileName = zipName + FileExtensions.ZIP_EXTENSION;
			}
		}
		outputZIPFileName = src.getParent() + "/" +outputZIPFileName;
		createZIP(new String[]{sourcePath}, outputZIPFileName);
	}

	/**
	 * create zip for multiple files
	 * 
	 * @param sourcePaths array of file paths
	 * @param outputZIPFilePath path where zip has to be created
	 * @return true if zip has been created successfully 
	 */
	public boolean createZIP(final String[] sourcePaths, final String outputZIPFilePath) {
		boolean zipCompleted = true; 
		try {
			//just get the first file to determine the path. note that all the files which has to 
			//be zipped are from the same path
			final File tempFile = new File(sourcePaths[0]); 
			mParentFilePathLength = tempFile.getParentFile().getPath().length() + 1;
			final FileOutputStream fileWriter = new FileOutputStream(outputZIPFilePath);
			final ZipOutputStream zip = new ZipOutputStream(fileWriter);
			for(String path : sourcePaths) {
				if(path != null && new File(path).exists()) {
					addFileToZip(path,zip);
				}
			}
			zip.closeEntry();
			zip.close();
		}catch (Exception  e) {
			//e.printStackTrace();
			zipCompleted = false;
		}
		return zipCompleted;
	}

	private void addFileToZip(final String srcFile, final ZipOutputStream zip) throws Exception {
		final File folder = new File(srcFile);
		if (folder.isDirectory()) { 
			addFolderToZip(srcFile, zip);
		} else {
			final byte[] buf = new byte[1024];
			int len;
			final FileInputStream in = new FileInputStream(srcFile);
			final String folderPath = folder.getPath();
			zip.putNextEntry(new ZipEntry(folderPath.substring(mParentFilePathLength)));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}
	}

	private void addFolderToZip(final String srcFolder, final ZipOutputStream zip) throws Exception {
		File[] listFiles = new File(srcFolder).listFiles();
		if(listFiles != null) {
			for (File file : listFiles) {			
				addFileToZip(file.getPath(), zip);
			}
		}
	}     

	/**
	 * unzip a zip file
	 * 
	 * @param zipFilePath path for the zip file
	 * @return true is unzip is successful. 
	 */
	public boolean fileUnzip(String zipFilePath) {
		try
		{
			File sourceZipFile = new File(zipFilePath);
			FileInputStream fileInputStream = new FileInputStream(sourceZipFile);
			File destDirectory = new File(sourceZipFile.getParent() +  "/" +ItemOperations.getOnlyFileName(sourceZipFile.getName()));
			ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

			//Open the ZIP file for reading
			ZipFile zipFile = new ZipFile(sourceZipFile,ZipFile.OPEN_READ);
			ZipEntry zipEntry;

			while((zipEntry = zipInputStream.getNextEntry()) != null)
			{
				//ZipEntry zipEntry = (ZipEntry)enum.nextElement();
				String currName = zipEntry.getName();
				File destFile = new File(destDirectory,currName);
				// grab file's parent directory structure
				File destinationParent = destFile.getParentFile();
				// create the parent directory structure if needed
				destinationParent.mkdirs();

				if(!zipEntry.isDirectory())
				{
					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(zipEntry), 16*1024);
					int currentByte;

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, 16*1024);

					// read and write until last byte is encountered
					while ((currentByte = is.read()) != -1)
					{
						dest.write(currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}
			}
			zipInputStream.close();
		}
		catch(IOException ioe) {
			//ioe.printStackTrace();
			return false;
		}
		return true;
	}
}
