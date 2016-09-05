/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sun.org.apache.bcel.internal.generic.NEW;

import sun.util.resources.cldr.ur.CurrencyNames_ur;

public class Database {
	String sessionID;
	Connection connection;
	String statementStr;
	String subStatementStr;
	String rootDir;
	Statement statement;
	PreparedStatement updateQuery;
	PreparedStatement subQuery ;
	ResultSet queryResult;
	static final int  BASE_TABLE = 0;
	static final int ENCRYPTED_TABLE = 1;
	
	Database(String sessionIDNumber,String password,boolean toCreate,String rootDirParent) throws Exception{
		this.rootDir=new File(rootDirParent).getAbsoluteFile().getParent();
		rootDirParent=new File(rootDirParent).getAbsoluteFile().getParent();
		sessionID=sessionIDNumber;
		Class.forName("org.h2.Driver").newInstance();
		if(toCreate)
			connection = DriverManager.getConnection("jdbc:h2:"+rootDirParent+File.separator+sessionID+";MV_STORE=FALSE","blackcipher",password);
		else
			System.out.println(sessionID);
			System.out.println(password);
			connection = DriverManager.getConnection("jdbc:h2:"+rootDirParent+File.separator+sessionID+";MV_STORE=FALSE;IFEXISTS=TRUE","blackcipher",password);
		
		statement= connection.createStatement();
		if(toCreate)
			createDB();
	}
	
	void createDB() throws Exception{
		statementStr="create table Details ("
				+ "session_ID varchar(40) not null,"
				+ "Creation_Date timestamp not null,"
				+ "Total_Files int not null)";
		statement.executeUpdate(statementStr);
	
		statementStr="create table Data_Table_base ("
				+ "File_ID int not null,"
				+ "File_Name varchar(255),"
				+ "File_Dir varchar(255) not null,"
				+ "is_Dir boolean not null,"
				+ "MD5_Sum varchar(128),"
				+ "Last_Modified timestamp ,"
				+ "Status char,"
				+ "primary key(File_ID,MD5_Sum))";
		statement.executeUpdate(statementStr);

		statementStr="create table Data_Table_encrypted ("
				+ "File_ID int not null,"
				+ "File_Name varchar(50) not null,"
				+ "MD5_Sum varchar(128) not null,"
				+ "Key_Value blob not null,"
				+ "IV blob not null,"
				+ "Status char not null,"
				+ "Last_Modified timestamp not null,"
				+ "primary key(File_ID,MD5_Sum))";
		statement.executeUpdate(statementStr);

		statementStr="create table File_Tree ("		
				+ "Node blob not null)";
		statement.executeUpdate(statementStr);
	}

	void saveFileTreeToDB(DefaultMutableTreeNode rootNode) throws SQLException, IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(rootNode);
		oos.flush();
		oos.close();
		byte[] objectBlob = bos.toByteArray();
		statementStr="insert into File_Tree values(?)";
		updateQuery=connection.prepareStatement(statementStr);
		updateQuery.setObject(1,objectBlob);
		updateQuery.execute();		
	}
	
	void getFileTreeFromDB() throws SQLException, IOException{
		statementStr = "select File_Name,File_Dir,Is_Dir from Data_Table_base";
		queryResult=statement.executeQuery(statementStr);
		/*DefaultMutableTreeNode parNode;
		DefaultMutableTreeNode tempNode = null;
		String parLocation;
		DefaultMutableTreeNode currNode =new DefaultMutableTreeNode(new File(queryResult.getString("File_Dir"),queryResult.getString("File_Name")));
		FileTree.rootNode=currNode;
		parNode=currNode;
		parLocation=queryResult.getString("File_Dir");
		while(queryResult.next()){
			currNode=new DefaultMutableTreeNode(new File(queryResult.getString(2),queryResult.getString(1)));
			if(parLocation==queryResult.getString("File_Dir")){
				parNode.add(currNode);
			}
			else{
				
			}
			FileTree.rootNode.getChildAt(FileTree.rootNode.getIndex(tempNode));
			FileTree.rootNode.getLastLeaf().add(tempNode);
		}*/
		ArrayList<File>dirList = new ArrayList<>();
		ArrayList<File>fileList = new ArrayList<>();
		ArrayList<DefaultMutableTreeNode>dirNodeList = new ArrayList<>();
		ArrayList<DefaultMutableTreeNode>fileNodeList = new ArrayList<>();
		File temp = new File(".temp");
		temp.mkdir();
		int len = rootDir.length();
		System.out.println(len);
		while(queryResult.next()){
			if(queryResult.getBoolean("Is_Dir")){
				dirList.add(new File(queryResult.getString("File_Dir"),queryResult.getString("File_Name")));
				new File(temp,queryResult.getString("File_Dir").substring(len)+File.separator+queryResult.getString("File_Name")).mkdirs();
			}
			else{
				fileList.add(new File(queryResult.getString("File_Dir"),queryResult.getString("File_Name")));
				new File(temp,queryResult.getString("File_Dir").substring(len)+File.separator+queryResult.getString("File_Name")).createNewFile();
			}
		}
		FileTree.rootNode=(DefaultMutableTreeNode) FileTree.createTree(temp.getAbsoluteFile());
		/*int i=0,j=0;
		for(File curr : dirList){
			dirNodeList.add(new DefaultMutableTreeNode(curr));
			for(File currFile : fileList){
				if(currFile.getAbsoluteFile().getPath()==curr.getAbsolutePath()){
					dirNodeList.get(i).add(new DefaultMutableTreeNode(currFile));
					
				}
			}
			i++;
		}
		
		for(i=dirList.size()-1;i>=0;i--){
			File temp =dirList.get(i);
			for(j=dirList.size()-1;j>=0;j--){
				File curr = dirList.get(j);
				if(curr.getAbsoluteFile().getParent()==temp.getAbsolutePath()){
					dirNodeList.get(i).add(dirNodeList.get(j));
					//dirNodeList.remove(j);
					//dirList.remove(j);
					//j--;
				}
			}
		}
		FileTree.rootNode=dirNodeList.get(0);
		*/
	}
	
	void updateDB(int File_ID,File obj , int target , byte[]...keyData) throws Exception{
		switch(target){
			//input DB detail table
			case 0 : 	if(!(statement.executeQuery("select * from Details").first())){
							statementStr="insert into Details values(?,?,?)";
							updateQuery=connection.prepareStatement(statementStr);
							updateQuery.setString(1,sessionID);
							updateQuery.setTimestamp(2,new Timestamp(new Date().getTime()));
							updateQuery.setInt(3,totalFileCount());
							updateQuery.execute();
							break;
						}
						else{
							statementStr="update Details set Total_Files=?,Creation_Date=? where session_ID=?";
							updateQuery=connection.prepareStatement(statementStr);
							updateQuery.setString(3,sessionID);
							updateQuery.setTimestamp(2,new Timestamp(new Date().getTime()));
							updateQuery.setInt(1,totalFileCount());
							updateQuery.execute();
							break;
						}
					
			//Adds new file data to "base" table. 
			case 1 :   	statementStr="insert into Data_Table_base values (?,?,?,?,?,?,?)";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,getLastID(BASE_TABLE));
						if(!obj.getAbsoluteFile().isDirectory()){
								updateQuery.setString(2,obj.getAbsoluteFile().getName());
								updateQuery.setString(5,FileHandler.createHash(obj.getAbsoluteFile()));
						}
						else{
							updateQuery.setString(2,obj.getAbsoluteFile().getName());
							updateQuery.setString(5,"");
						}
						updateQuery.setString(3,obj.getAbsoluteFile().getParent());
						updateQuery.setBoolean(4,obj.getAbsoluteFile().isDirectory());
						updateQuery.setTimestamp(6,new Timestamp(obj.getAbsoluteFile().lastModified()));
						updateQuery.setString(7,"D");
						updateQuery.execute();
						updateDB(0,null,0,null);
						break;
			
			//upon encryption , generate "encrypted" table entry for the given file 
			case 2 :	statementStr="insert into Data_Table_encrypted values(?,?,?,?,?,?,?)";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,File_ID);
						updateQuery.setString(2,FileHandler.randomSessionIDGenerator());
						updateQuery.setString(3, FileHandler.createHash(obj.getAbsoluteFile()));
						updateQuery.setBlob(4,new ByteArrayInputStream(keyData[0]));
						updateQuery.setBlob(5,new ByteArrayInputStream(keyData[1]));
						updateQuery.setString(6,"E");
						updateQuery.setTimestamp(7,new Timestamp(obj.getAbsoluteFile().lastModified()));
						updateQuery.execute();
						//reset data values in 'base' table
						statementStr="update Data_Table_base set status='E', Last_Modified=? where File_ID=?";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setTimestamp(1,new Timestamp(obj.getAbsoluteFile().lastModified()));
						updateQuery.setInt(2,File_ID);
						updateQuery.execute();
						break;
			
			//upon decryption , alter "base" table entry for the given file & delete "encrypted" table entry
			case 3:		statementStr="delete from Data_Table_encrypted where File_ID=?";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,File_ID);
						updateQuery.executeQuery();
						subStatementStr="update Data_Table_base set status='D',Last_Modified=? where File_ID=?";
						subQuery = connection.prepareStatement(subStatementStr);
						subQuery.setTimestamp(1,new Timestamp(obj.getAbsoluteFile().lastModified()));
						subQuery.setInt(2,File_ID);
						subQuery.executeQuery();
						break;
			
			//permanent removal of file from DB after decryption.
			case 4 :	statementStr="delete from Data_Table_base where File_ID=?";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,File_ID);
						updateQuery.execute();
						break;
		}
	}
	
	boolean checkHash(String MD5Str, int table) throws Exception{
		 if(table==0){
			 statementStr="select File_ID from Data_Table_base where MD5_Sum=?";
			 updateQuery=connection.prepareStatement(statementStr);
			 updateQuery.setString(1,MD5Str);
			 queryResult=updateQuery.executeQuery();
			 return queryResult.next();
			 }
		 else{
			 if(table==1){
				 statementStr="select File_ID from Data_Table_encrypted where MD5_Sum=?";
				 updateQuery=connection.prepareStatement(statementStr);
				 updateQuery.setString(1,MD5Str);
				 queryResult=updateQuery.executeQuery();
				 return queryResult.next();
			 }
			 return false;
		 }
	}
	
	String getName(String MD5Str,int table) throws Exception{
		 if(table==0){
			 statementStr="select File_Name from Data_Table_base where MD5_Sum=?";
			 updateQuery=connection.prepareStatement(statementStr);
			 updateQuery.setString(1,MD5Str);
			 queryResult=updateQuery.executeQuery();
			 return queryResult.getString(1);
			 }
		 else{
			 if(table==1){
				 statementStr="select File_ID from Data_Table_encrypted where MD5_Sum=?";
				 updateQuery=connection.prepareStatement(statementStr);
				 updateQuery.setString(1,MD5Str);
				 queryResult=updateQuery.executeQuery();
				 return queryResult.getString(1);
			 }
			 return null;
		 }
	}
	
	int getLastID(int table) throws SQLException{
		if(table==BASE_TABLE){
			 statementStr="select count(FILE_ID) from Data_Table_base";
			 queryResult=statement.executeQuery(statementStr);
			 queryResult.next();
			 return queryResult.getInt(1);
		}
		else{
			 if(table==ENCRYPTED_TABLE){
				 statementStr="select count(FILE_ID) from Data_Table_Encrypted";
				 queryResult=statement.executeQuery(statementStr);
				 queryResult.next();
				 return queryResult.getInt(1);
		 	 }
		return -1;
		}
	}
	
	int totalFileCount() throws SQLException{
		 statementStr="select Count(File_ID) from Data_Table_base where is_Dir=false";
		 queryResult=statement.executeQuery(statementStr);
		 queryResult.first();
		 return queryResult.getInt(1);
		
	}
	
	int totalErcFileCount() throws SQLException{
		 statementStr="select Count(File_ID) from Data_Table_encrypted,";
		 queryResult=statement.executeQuery(statementStr);
		 return queryResult.getInt(1);
		
	}
	
	int getFileID(String MD5Str) throws SQLException{
		statementStr="select File_ID from Data_Table_base where MD5_Sum=?";
		updateQuery=connection.prepareStatement(statementStr);
		updateQuery.setString(1, MD5Str);
		queryResult=updateQuery.executeQuery();
		queryResult.next();
		return queryResult.getInt(1);
	}
	
	ResultSet getFileDetails(String MD5Str, int table) throws SQLException{
		if(table==0){
			statementStr="select * from Data_Table_base where MD5_Sum="+MD5Str;
			queryResult=statement.executeQuery(statementStr);
		}
		if(table==1){
			statementStr="select * from Data_Table_encrypted where MD5_Sum="+MD5Str;
			queryResult=statement.executeQuery(statementStr);
		}
		return queryResult;
	}
	
	ResultSet getFileDetails(int File_ID, int table) throws SQLException{
		queryResult=null;
		if(table==0){
			statementStr="select * from Data_Table_base where FILE_ID="+File_ID;
			queryResult=statement.executeQuery(statementStr);
		}
		if(table==1){
			statementStr="select * from Data_Table_encrypted where FILE_ID="+File_ID;
			queryResult=statement.executeQuery(statementStr);
		}
		return queryResult;
	}
	
	ResultSet getDBDetails() throws SQLException{
		statementStr="select * from Details";
		queryResult=statement.executeQuery(statementStr);
		return queryResult;
	}
}

