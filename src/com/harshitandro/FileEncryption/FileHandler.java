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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class FileHandler extends Encryption{
	File OutputDirectory;
	FileInputStream targetInputStream;
	FileOutputStream targetOutputStream;
	String InputFileName;
	String OutputFileName;
	String SessionID;
	String password;
	int Mode=-1;
	Database databaseObj;
	ArrayList<File> fileList = new ArrayList<File>();
	static final int ENCRYPTION_MODE = 1;
	static final int DECRYPTION_MODE = 0;
	static final boolean CREATE_DB_TRUE = true;
	static final boolean CREATE_DB_FALSE = false;
	
	FileHandler(String password,boolean toCreate,String rootDir) throws Exception{
		this.SessionID=randomSessionIDGenerator();
		this.password=password;
		databaseObj = new Database(SessionID, password, toCreate,rootDir);
	}
	
	FileInputStream  genStream(File fileObj) throws FileNotFoundException{
		targetInputStream = new FileInputStream(fileObj.getAbsoluteFile());
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
	
	public static String randomSessionIDGenerator(){
		SecureRandom random = new SecureRandom();
		return new BigInteger(130,random).toString(32).toUpperCase().substring(0,10);
	}
	
	void createFileList() throws Exception{
		Enumeration<DefaultMutableTreeNode> child = FileTree.rootNode.preorderEnumeration();
		while(child.hasMoreElements()){
			fileList.add((File)child.nextElement().getUserObject());
		}
		ArrayList<File> tempList = new ArrayList<File>(15);
		tempList = fileList;
		for(File x : tempList){
			if(x.getAbsoluteFile().isFile()){
				databaseObj.updateDB(databaseObj.getLastID(Database.BASE_TABLE)+1,x.getAbsoluteFile(),1,null);
				databaseObj.connection.commit();
			}
		}
		databaseObj.saveFileTreeToDB(FileTree.rootNode);
	}
	
	public void printFileStorage() throws FileNotFoundException, IOException{
		ArrayList<File> tempList = new ArrayList<File>(15);
		tempList = fileList;
		for(File x : tempList){
				System.out.println(x.getAbsolutePath());
		}
	}
	
	public boolean doFinal(int Mode,ArrayList<File> filesToProcess) throws Exception{
		if(Mode==ENCRYPTION_MODE){
			OutputDirectory=new File(fileList.get(0),"Encrypted");
			OutputDirectory.getAbsoluteFile().mkdirs();
			for(File x : filesToProcess){
				if(x.getAbsoluteFile().isFile()){
					byte[] keyUsed;
					byte[] IVUsed;
					InputFileName = x.getAbsoluteFile().getName();
					targetInputStream = genStream(x);
					OutputFileName = randomSessionIDGenerator();
					File outputFile=new File(OutputDirectory,OutputFileName);
					outputFile.getAbsoluteFile().createNewFile();
					targetOutputStream = new FileOutputStream(outputFile);
					ArrayList<byte[]> cipherKeys = doFinal(targetInputStream,targetOutputStream,ENCRYPTION_MODE,null);
					keyUsed=cipherKeys.get(0);
					IVUsed=cipherKeys.get(1);
					int fileID=databaseObj.getFileID(createHash(x.getAbsoluteFile()));
					if(!databaseObj.checkHash(createHash(x),Database.BASE_TABLE))
						databaseObj.updateDB(fileID,x,1,null);
					databaseObj.updateDB(fileID,outputFile,2,keyUsed,IVUsed);
					
				}
			}
		return true;
		}
		else
			if(Mode==DECRYPTION_MODE){
				for(File x : filesToProcess){
					if(x.getAbsoluteFile().isFile()){
						ResultSet fileInfo = databaseObj.getFileDetails(createHash(x.getAbsoluteFile()),Database.ENCRYPTED_TABLE);
						int fileID=fileInfo.getInt("FILE_ID");
						byte[] keyUsed = fileInfo.getBytes("KEY_VALUE");
						byte[] IVUsed = fileInfo.getBytes("IV");
						ResultSet decryptedFileInfo = databaseObj.getFileDetails(fileID,Database.BASE_TABLE);
						OutputFileName = decryptedFileInfo.getString("FILE_NAME");
						OutputDirectory = new File(decryptedFileInfo.getString("FILE_DIR"));
						OutputDirectory.getAbsoluteFile().mkdirs();
						InputFileName = x.getAbsoluteFile().getName();
						targetInputStream = genStream(x);
						File outputFile=new File(OutputDirectory,OutputFileName);
						outputFile.getAbsoluteFile().createNewFile();
						targetOutputStream = new FileOutputStream(outputFile);
						ArrayList<byte[]> cipherKeys = doFinal(targetInputStream,targetOutputStream,DECRYPTION_MODE,keyUsed,IVUsed);
						databaseObj.updateDB(fileID,null,3,null);
					}
				}
		return true;
		}
		return false;
	}
	
	public static void updateDB(){}
	
}