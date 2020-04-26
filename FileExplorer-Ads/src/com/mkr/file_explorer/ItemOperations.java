package com.mkr.file_explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * this class performs the basic operations like creating new folder, renaming etc..
 * @author Manthena Murali
 *
 */
public class ItemOperations {

	/**
	 * list of all items(both files and folders) which are currently being displayed 
	 */
	private ArrayList<File> mListOfAllItems;
	
	/**
	 * list of ONLY folder which are currently displayed
	 */
	private ArrayList<File> mListOfOnlyFolders;
	
	/**
	 * list of ONLY files which are currently displayed
	 */
	private ArrayList<File> mListOfOnlyFiles;

	public ItemOperations() {
		mListOfAllItems = new ArrayList<File>();
		mListOfOnlyFolders = new ArrayList<File>();
		mListOfOnlyFiles = new ArrayList<File>();
	}

	/**
	 * Loads all the files and folders in a folder. Also note the by default
	 * First folders are displayed then files
	 * 
	 * @param folderPath the folder path in which all the files and folders
	 * 			has to be displayed
	 * @return the list of all items in the folder
	 */
	public List<File> loadFiles(String folderPath) {

		clear();

		File[] allFiles = new File(folderPath).listFiles();
		if(allFiles == null || allFiles.length == 0) {
			return null;
		}

		for (File file : allFiles) {
			//display hiddedn files/folders based on the user prefernces
			if(!FileExpActivity.mDisplayHiddenFiles) {
				if(file.isHidden()) {
					continue;
				}
			}

			//add to files list or folders list
			if(file.isFile()) {
				mListOfOnlyFiles.add(file);
			} else {
				mListOfOnlyFolders.add(file);
			}
		}

		/*
		 * sort the files and folder based on the user preferences
		 */
		switch (FileExpActivity.mSortItemsBy) {
		case Settings.SORT_BY_NAME:
			sortByName();
			break;
		case Settings.SORT_BY_TYPE:

			break;
		case Settings.SORT_BY_MODIFIED_DATE:
			sortByModifiedDate();
			break;
		}
		
		return mListOfAllItems;
	}

	/**
	 * sort files and folder in alphabetical order
	 */
	private void sortByName() {

		Collections.sort(mListOfOnlyFolders);
		Collections.sort(mListOfOnlyFiles);

		//First display folders then files
		mListOfAllItems.addAll(mListOfOnlyFolders);
		mListOfAllItems.addAll(mListOfOnlyFiles);
	}

	/**
	 * sort files and folders based on the modified date. Currently displaying in 
	 * ascending order
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sortByModifiedDate() {

		File[] allFiles = new File[mListOfOnlyFiles.size()];
		allFiles = mListOfOnlyFiles.toArray(allFiles);
		File[] allFolders = new File[mListOfOnlyFolders.size()];
		allFolders = mListOfOnlyFolders.toArray(allFolders);

		Arrays.sort(allFolders, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (((File) o1).lastModified() > (((File) o2).lastModified())) {
					return -1;
				} else if (((File) o1).lastModified() < (((File) o2).lastModified())) {
					return +1;
				} else {
					return 0;
				}
			}
		});

		Arrays.sort(allFiles, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (((File) o1).lastModified() > (((File) o2)
						.lastModified())) {
					return -1;
				} else if (((File) o1).lastModified() < (((File) o2)
						.lastModified())) {
					return +1;
				} else {
					return 0;
				}
			}
		});

		//First display folders then files
		mListOfAllItems.addAll(Arrays.asList(allFolders));
		mListOfAllItems.addAll(Arrays.asList(allFiles));
	}
	
	/**
	 * create a new folder
	 * @param folderPath where the folder has to be created
	 * @param folderName what has to be the file name
	 * 
	 * @return true if folder has been created
	 */
	public boolean createNewFolder(String folderPath, String folderName) {
		File file = new File(folderPath, folderName);
		if(isItemExists(file)) {
			return false;
		} else {
			File f = new File(folderPath+"/"+folderName);
			boolean b = f.mkdir();
			return b;
		}
	}

	/**
	 * create a new file
	 * @param folderPath where the file has to be created
	 * @param fileName what has to be the file name
	 * 
	 * @return true if file has been created
	 */
	public boolean createNewFile(String folderPath, String fileName) {
		File file = new File(folderPath, fileName);
		if(isItemExists(file)) {
			return false;
		} else {
			File f = new File(folderPath+"/"+fileName);
			boolean b = false;
			try {
				b = f.createNewFile();
				f.setReadable(true);
				f.setWritable(true);
				f.setExecutable(true);
			} catch (Exception e) { e.printStackTrace(); }
			return b;
		}
	}

	/**
	 * check if the file is already present in the current folder
	 * 
	 * @param file file that has to be checked
	 * @return true if already exists
	 */
	private boolean isItemExists(File file) {
		if(file.isDirectory()) {
			return mListOfOnlyFolders.contains(file);	
		} else {
			return mListOfOnlyFiles.contains(file);	
		}
	}

	/**
	 * check whether path is readable or not
	 * 
	 * @param pathToCheck path to be checked
	 * @return true if readable
	 */
	public boolean isFolderReadable(String pathToCheck) {
		if(pathToCheck != null) {
			File f = new File(pathToCheck); 
			if(f.isDirectory() && f.exists()) {
				return f.canRead();
			}
		}
		return false;
	}

	/**
	 * get the file extension
	 * 
	 * @param fileName file name
	 * @return file extension
	 */
	public static String getFileExtension(String fileName) {
		if(!fileName.contains(".")) {
			return null;
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1); 
	}

	/**
	 * return only the file name without any extension
	 * 
	 * @param fileName file name
	 * @return only file name without extension 
	 */
	public static String getOnlyFileName(String fileName) {
		if(!fileName.contains(".")) {
			return fileName;
		}

		int lastIndex = fileName.lastIndexOf('.');
		//if dot is in 0th place it is hidden file so display full name
		if(lastIndex == 0) {
			lastIndex = fileName.length() - 1;
		}
		return fileName.substring(0, lastIndex); 
	}

	public ArrayList<File> getOnlyFolders() {
		return mListOfOnlyFolders;
	}

	/**
	 * check if the folder exists
	 * @param path
	 * @return true if exists
	 */
	public boolean isFolderExists(String path) {
		return new File(path).exists();
	}

	/**
	 * return the parent for the file
	 * @param path file path
	 * @return
	 */
	public static String getParentPath(String path) {
		File file = new File(path);
		if(file.exists()) {
			return file.getParent();
		}
		return null;
	}

	/**
	 * rename a file or folder
	 * 
	 * @param fileParentPath the path in which the rename file/folder is present, nothing but current displaying folder
	 * @param oldName already existing file name
	 * @param newName name to which the file has to be renames
	 * @return true if rename is successful
	 */
	public boolean rename(String fileParentPath, String oldName, String newName)
	{
		boolean retVal;
		File oldFile = new File(fileParentPath, oldName);
		File newFile;
		if(oldFile.isDirectory()) {
			newFile = new File(fileParentPath, newName);
		} else {
			if(oldName.contains(".")) {
				newFile = new File(fileParentPath, newName + "."+ getFileExtension(oldName));
			} else {
				newFile = new File(fileParentPath, newName);
			}
		}
		
		if(isItemExists(newFile)) { 
			retVal = false;
		} else {
			retVal = oldFile.renameTo(newFile);
		}

		return retVal;
	}

	/**
	 * clear all the list items
	 */
	private void clear() {
		mListOfAllItems.clear();
		mListOfOnlyFolders.clear();
		mListOfOnlyFiles.clear();
	}
}
