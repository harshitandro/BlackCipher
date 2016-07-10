/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class FileHandler {
	File OutputDirectory;
	FileInputStream targetInputStream;
	FileOutputStream targetOutputStream;
	String InputFileName;
	String OutpuFileName;
	String SessionID;
	final int ENCRYPTION_MODE = 1;
	final int DECRYPTION_MODE = 0;
	int Mode=-1;
	Encryption encryptionObj;
	Database databaseObj;
	ArrayList<File> fileList = new ArrayList<File>();
	public int count=0;
	String sessionID;
	
	
	FileHandler(Encryption encObj, Database dbObj){
		encryptionObj=encObj;
		databaseObj=dbObj;
	}
	
	public FileHandler() {
				
	}

	FileInputStream  genStream(File fileObj) throws FileNotFoundException{
		targetInputStream = new FileInputStream(fileObj);
		return targetInputStream;
	}
		
	static String createHash(File fileObj) throws FileNotFoundException, IOException{
		if (fileObj.isFile())
			return Base64.encodeBase64String(DigestUtils.sha256(new FileInputStream(fileObj)));
		return "";
	}
	
	static String createHash(String stringObj) throws FileNotFoundException, IOException{
		return Base64.encodeBase64String(DigestUtils.sha256(stringObj));
	}
	
	public static String randomGenerator(){
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
		
	}
	
	void createFileList(){
		Enumeration<DefaultMutableTreeNode> child = FileTree.rootNode.preorderEnumeration();
		while(child.hasMoreElements()){
			fileList.add((File)child.nextElement().getUserObject());
		}
	}
	
	public void printFileStorage() throws FileNotFoundException, IOException{
		ArrayList<File> tempList = new ArrayList<File>(15);
		tempList = fileList;
		for(File x : tempList){
				System.out.println(x.getAbsolutePath());
		}
	}
	
	public boolean doFinal(){
		return false;
		
	}
	
	public static void updateDB(){}
	
}