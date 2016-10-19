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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class FileHandler extends Encryption{
	String rootDir;
	File OutputDirectory;
	FileInputStream targetInputStream;
	FileOutputStream targetOutputStream;
	String InputFileName;
	String OutputFileName;
	String SessionID;
	String password;
	int Mode=-1;
	Database databaseObj;
	Encryption encryptionObj;
	public ArrayList<File> fileList = new ArrayList<File>();
	static long totalSize = 0;
	static long currFileSize = 0;
	static long processedSize = 0;
	static long totalFileCount = 0;
	static long remainingFileCount = 0;
	static final int ENCRYPTION_MODE = 1;
	static final int DECRYPTION_MODE = 0;
	static final boolean CREATE_DB_TRUE = true;
	static final boolean CREATE_DB_FALSE = false;
	
	FileHandler(String SessionID,String password,boolean toCreate,String rootDir) throws Exception{
		this.SessionID=SessionID;
		this.password=password;
		this.rootDir=new File(rootDir).getAbsoluteFile().getParent();
		databaseObj = new Database(SessionID, password, toCreate,rootDir);
		encryptionObj= new Encryption();
		//System.out.println(password);
	}
	
	
	boolean recheckFilesOnLoad(){
		
		return true;
	}

	void progressBarHandler(String string){
		processedSize+=currFileSize;
		remainingFileCount--;
		UserInterface.setProgressBar(2,string,String.valueOf((int)((processedSize/totalSize)*100)));
	}
	
	
	static long sizeOfFile(File file){
		return file.getAbsoluteFile().length()/1024; //returns file size in KB
	}
	
	void getFileTreeFromDB() throws SQLException, IOException{
		databaseObj.statementStr ="select File_Name,parent,is_Dir from Data_Table_base order by File_ID";
		databaseObj.queryResult=databaseObj.statement.executeQuery(databaseObj.statementStr);
		DefaultMutableTreeNode rootNode = null;
		DefaultMutableTreeNode tempNode = null;;
		DefaultMutableTreeNode tempParNode,temp = null;
		File currFile;
		while(databaseObj.queryResult.next()){
			currFile = new File(rootDir+File.separator+databaseObj.queryResult.getString("parent"),databaseObj.queryResult.getString("File_Name"));
			tempNode = new DefaultMutableTreeNode(currFile.getAbsoluteFile());
			tempParNode = new DefaultMutableTreeNode(currFile.getAbsoluteFile().getParent());
			if(rootNode!=null)
				temp=rootNode.getLastLeaf();
			if(temp!=null){
				while(!temp.getUserObject().toString().equals(tempParNode.getUserObject().toString()))
					temp=temp.getPreviousNode();
				temp.add(tempNode);
			}
			else
				if(rootNode==null)
					rootNode=tempNode;
				else
					rootNode.add(tempNode);
		}
		FileTree.rootNode=rootNode;
	}
	
	static String createHash(File fileObj) throws FileNotFoundException, IOException{
		if (fileObj.getAbsoluteFile().isFile())
			return Base64.encodeBase64String(DigestUtils.md5(new FileInputStream(fileObj)));
		return "";
	}
	
	public static String randomSessionIDGenerator(){
		SecureRandom random = new SecureRandom();
		return new BigInteger(130,random).toString(32).toUpperCase().substring(0,10);
	}
	
	void createFileList() throws Exception{
		Enumeration<DefaultMutableTreeNode> child = FileTree.rootNode.preorderEnumeration();
		File temp ;
		while(child.hasMoreElements()){
			temp=(File) child.nextElement().getUserObject();
			if(fileList.indexOf(temp)==-1)
				fileList.add(temp);
		}
		for(File x : fileList){
			databaseObj.updateDB(databaseObj.getLastID(Database.BASE_TABLE)+1,x.getAbsoluteFile(),1,null);
		}
		databaseObj.connection.commit();
	}
	
	public void printFileStorage() throws FileNotFoundException, IOException{
		ArrayList<File> tempList = new ArrayList<File>(15);
		tempList = fileList;
		for(File x : tempList){
				System.out.println(x.getAbsolutePath());
		}
	}
	
	public boolean doFinal(int Mode) throws Exception {
		if(Mode==ENCRYPTION_MODE){
			remainingFileCount=0;
			totalFileCount=databaseObj.totalFileCount()-databaseObj.totalErcFileCount();
			totalSize = databaseObj.totalFileSize();
			processedSize=0;
			OutputDirectory=new File(new File(rootDir),SessionID+"_Encrypted");
			OutputDirectory.getAbsoluteFile().mkdirs();
			for(File x : fileList){
				if(x.getAbsoluteFile().isFile()){
					byte[] keyUsed=null;
					byte[] IVUsed=null;
					InputFileName = x.getAbsoluteFile().getName();
					targetInputStream = new FileInputStream(x.getAbsoluteFile());
					OutputFileName = randomSessionIDGenerator();
					File outputFile=new File(OutputDirectory,OutputFileName);
					outputFile.getAbsoluteFile().createNewFile();
					targetOutputStream = new FileOutputStream(outputFile);
					ArrayList<byte[]> cipherKeys = null;
					try {
						cipherKeys = doFinal(targetInputStream,targetOutputStream,ENCRYPTION_MODE,keyUsed);
					} catch (Exception e) {
						e.printStackTrace();
					}
					keyUsed=cipherKeys.get(0);
					IVUsed=cipherKeys.get(1);
					int fileID=databaseObj.getFileID(createHash(x.getAbsoluteFile()));
					if(!databaseObj.checkHash(createHash(x.getAbsoluteFile()),Database.BASE_TABLE))
						databaseObj.updateDB(fileID,x,1,null);
					databaseObj.updateDB(fileID,outputFile,2,keyUsed,IVUsed);
					currFileSize=x.length()/1024;
					encryptionObj.secureDelete(x.getAbsoluteFile());
					progressBarHandler("Encrypting Session");
				}
			}
		return true;
		}
		else
			if(Mode==DECRYPTION_MODE){
				databaseObj.statementStr ="select * from Data_Table_encrypted order by File_ID";
				databaseObj.queryResult=databaseObj.statement.executeQuery(databaseObj.statementStr);
				while(databaseObj.queryResult.next()){
					fileList.add(new File(rootDir+File.separator+SessionID+"_Encrypted",databaseObj.queryResult.getString("File_Name")));
				}
				for(File x : fileList){
						//System.out.println(x.getAbsolutePath());
						ResultSet fileInfo = databaseObj.getFileDetails(createHash(x.getAbsoluteFile()),Database.ENCRYPTED_TABLE);
						fileInfo.next();
						int fileID=fileInfo.getInt("FILE_ID");
						fileInfo = databaseObj.getFileDetails(fileID, Database.ENCRYPTED_TABLE);
						fileInfo.next();
						byte[] keyUsed = fileInfo.getBytes("KEY_VALUE");
						byte[] IVUsed = fileInfo.getBytes("IV");
						ResultSet decryptedFileInfo = databaseObj.getFileDetails(fileID,Database.BASE_TABLE);
						decryptedFileInfo.next();
						OutputFileName = decryptedFileInfo.getString("FILE_NAME");
						OutputDirectory = new File(rootDir,decryptedFileInfo.getString("Parent"));
						OutputDirectory.getAbsoluteFile().mkdirs();
						InputFileName = x.getAbsoluteFile().getName();
						targetInputStream = new FileInputStream(x.getAbsoluteFile());
						File outputFile=new File(OutputDirectory,OutputFileName);
						outputFile.getAbsoluteFile().createNewFile();
						targetOutputStream = new FileOutputStream(outputFile);
						doFinal(targetInputStream,targetOutputStream,DECRYPTION_MODE,keyUsed,IVUsed);
						databaseObj.updateDB(fileID,null,3,null);
						x.getAbsoluteFile().delete();
				}
				new File(rootDir+File.separator+SessionID+"_Encrypted").getAbsoluteFile().delete();
				return true;
		}
		return false;
	}
}