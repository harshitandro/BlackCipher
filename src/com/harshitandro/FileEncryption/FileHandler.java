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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
	ArrayList<File> fileList = new ArrayList<File>();
	static final int ENCRYPTION_MODE = 1;
	static final int DECRYPTION_MODE = 0;
	static final boolean CREATE_DB_TRUE = true;
	static final boolean CREATE_DB_FALSE = false;
	
	FileHandler(String SessionID,String password,boolean toCreate,String rootDir) throws Exception{
		this.SessionID=SessionID;
		this.password=password;
		this.rootDir=rootDir;
		databaseObj = new Database(SessionID, password, toCreate,rootDir);
		
	}
	
	ArrayList<File> getdbFiles(){
		ArrayList<File> dbfileList = new ArrayList<File>(15);
		DefaultMutableTreeNode dbfiles ;
		System.out.println(rootDir+File.separator+SessionID+"_DB");
		File t = new File(new File(rootDir).getAbsoluteFile().getParent(),SessionID+"_DB");
		dbfiles=(DefaultMutableTreeNode)FileTree.createTree(t);
		Enumeration<DefaultMutableTreeNode> temp = dbfiles.preorderEnumeration();
		while(temp.hasMoreElements()){
			dbfileList.add((File)temp.nextElement().getUserObject());
		}
		return dbfileList;
	}
	
	void zipDB() throws IOException{
		int length = rootDir.length();
		ArrayList<File> fileList = getdbFiles();
		System.out.println(fileList.toArray().length);
		FileOutputStream fileOutStream = new FileOutputStream(rootDir+File.separator+SessionID+"_DB.bcdb");
		ZipOutputStream zipOutStream = new ZipOutputStream(fileOutStream);
		ZipEntry fileEntry ;
		FileInputStream fileInStream ;
		byte[] buffer= new byte[1024];
		for(File x : fileList){
			if(x.getAbsoluteFile().isFile()){
				fileInStream = new FileInputStream(x.getAbsoluteFile());
				fileEntry = new ZipEntry(x.getAbsoluteFile().getAbsolutePath().substring(length));
				zipOutStream.putNextEntry(fileEntry);
				while((fileInStream.read(buffer))>0){
					zipOutStream.write(buffer);
				}
				zipOutStream.closeEntry();
				fileInStream.close();
			}
		
		}
		zipOutStream.close();
	}
	
	void unzipDB(File DB_file,String SessionID,String password){
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
		while(child.hasMoreElements()){
			fileList.add((File)child.nextElement().getUserObject());
		}
		ArrayList<File> tempList = new ArrayList<File>(15);
		tempList = fileList;
		for(File x : tempList){
			if(x.getAbsoluteFile().isFile()){
				try {
					databaseObj.updateDB(databaseObj.getLastID(Database.BASE_TABLE)+1,x.getAbsoluteFile(),1,null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					databaseObj.connection.commit();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			databaseObj.saveFileTreeToDB(FileTree.rootNode);
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			OutputDirectory=new File(new File(rootDir).getAbsoluteFile().getParentFile(),SessionID+"_Encrypted");
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					keyUsed=cipherKeys.get(0);
					IVUsed=cipherKeys.get(1);
					int fileID=databaseObj.getFileID(createHash(x.getAbsoluteFile()));
					if(!databaseObj.checkHash(createHash(x.getAbsoluteFile()),Database.BASE_TABLE))
						databaseObj.updateDB(fileID,x,1,null);
					databaseObj.updateDB(fileID,outputFile,2,keyUsed,IVUsed);
					
				}
			}
		return true;
		}
		else
			if(Mode==DECRYPTION_MODE){
				for(File x : fileList){
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
						targetInputStream = new FileInputStream(x.getAbsoluteFile());
						File outputFile=new File(OutputDirectory,OutputFileName);
						outputFile.getAbsoluteFile().createNewFile();
						targetOutputStream = new FileOutputStream(outputFile);
						doFinal(targetInputStream,targetOutputStream,DECRYPTION_MODE,keyUsed,IVUsed);
						databaseObj.updateDB(fileID,null,3,null);
					}
				}
		return true;
		}
		return false;
	}
}