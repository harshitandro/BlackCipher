/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.tree.DefaultMutableTreeNode;

public class Database {
	String sessionID;
	Connection connection;
	String statementStr;
	String subStatementStr;
	Statement statement;
	PreparedStatement updateQuery;
	PreparedStatement subQuery ;
	ResultSet queryResult;
	static final int  BASE_TABLE = 0;
	static final int ENCRYPTED_TABLE = 1;
	
	Database(String sessionIDNumber,String password,boolean toCreate,String rootDir) throws Exception{
		sessionID=sessionIDNumber;
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		if(toCreate)
			connection = DriverManager.getConnection("jdbc:derby:"+rootDir+"/"+sessionID+";"+"create=true;"+"user=a"+sessionID+";"+"password="+ password+";");
		else
			connection = DriverManager.getConnection("jdbc:derby:"+rootDir+"/"+sessionID+";"+"user="+sessionID+";"+"password="+ password+";");
		statement= connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		createDB();
	}
	
	void createDB() throws Exception{
		statementStr="create table Details ("
				+ "session_ID varchar(40) not null,"
				+ "Creation_Date timestamp not null,"
				+ "Total_Files int not null)";
		statement.execute(statementStr);
		
		statementStr="create table Data_Table_base ("
				+ "File_ID int not null,"
				+ "File_Name varchar(50),"
				+ "File_Dir varchar(255) not null,"
				+ "is_Dir boolean not null,"
				+ "SHA_Sum varchar(255),"
				+ "Last_Modified timestamp ,"
				+ "Status char,"
				+ "primary key(File_ID))";
		statement.execute(statementStr);
		
		statementStr="create table Data_Table_encrypted ("
				+ "File_ID int not null,"
				+ "File_Name varchar(50) not null,"
				+ "SHA_Sum varchar(256) not null,"
				+ "Key_Value varchar(256) for bit data not null,"
				+ "IV varchar(128) for bit data not null,"
				+ "Status char not null,"
				+ "Last_Modified timestamp not null,"
				+ "primary key(File_ID,SHA_Sum))";
		statement.execute(statementStr);
		
		statementStr="create table File_Tree ("		
				+ "Node blob not null)";
		statement.execute(statementStr);
	}
	
	void saveFileTreeToDB(DefaultMutableTreeNode rootNode) throws SQLException, IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(rootNode);
		oos.flush();
		oos.close();
		byte[] objectBlob = bos.toByteArray();
		System.out.println(objectBlob.length +"B");
		statementStr="insert into File_Tree values(?)";
		updateQuery=connection.prepareStatement(statementStr);
		updateQuery.setObject(1,objectBlob);
		updateQuery.execute();		
	}
	
	DefaultMutableTreeNode getFileTreeFromDB() throws SQLException, IOException, ClassNotFoundException{
		statementStr="select Node from File_Tree";
		statement.execute(statementStr);
		queryResult=statement.getResultSet();
		queryResult.next();
		ByteArrayInputStream bis = new ByteArrayInputStream(queryResult.getBytes(1));
		ObjectInputStream ois = new ObjectInputStream(bis);
		return new DefaultMutableTreeNode(ois.readObject());
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
							//updateQuery.close();
							break;
						}
						else{
							statementStr="update Details set Total_Files=?,Creation_Date=? where session_ID=?";
							updateQuery=connection.prepareStatement(statementStr);
							updateQuery.setString(3,sessionID);
							updateQuery.setTimestamp(2,new Timestamp(new Date().getTime()));
							updateQuery.setInt(1,totalFileCount());
							updateQuery.execute();
							//updateQuery.close();
							break;
						}
					
			//Adds new file data to "base" table. 
			case 1 :    System.out.println(getLastID(BASE_TABLE));
						System.out.println(obj.getAbsoluteFile().getName()+FileHandler.createHash(obj.getAbsoluteFile()));
						System.out.println(obj.getAbsoluteFile().getParent());
						System.out.println(obj.getAbsoluteFile().isDirectory());
						System.out.println(new Timestamp(obj.getAbsoluteFile().lastModified()));
						statementStr="insert into Data_Table_base values (?,?,?,?,?,?,?)";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,getLastID(BASE_TABLE));
						if(!obj.getAbsoluteFile().isDirectory()){
								updateQuery.setString(2,obj.getAbsoluteFile().getName());
								updateQuery.setString(5,FileHandler.createHash(obj.getAbsoluteFile()));
						}
						else{
							updateQuery.setString(2,obj.getAbsoluteFile().getName());
							updateQuery.setString(5,null);
						}
						updateQuery.setString(3,obj.getAbsoluteFile().getParent());
						updateQuery.setBoolean(4,obj.getAbsoluteFile().isDirectory());
						updateQuery.setTimestamp(6,new Timestamp(obj.getAbsoluteFile().lastModified()));
						updateQuery.setString(7,"D");
						updateQuery.execute();
						//updateQuery.close();
						updateDB(0,null,0,null);
						//connection.commit();
						break;
			
			//upon encryption , generate "encrypted" table entry for the given file 
			case 2 :	statementStr="insert into Data_Table_encrypted values(?,?,?,?,?,?,?)";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,File_ID);
						updateQuery.setString(2,FileHandler.randomSessionIDGenerator());
						updateQuery.setString(3, FileHandler.createHash(obj.getAbsoluteFile()));
						updateQuery.setBytes(4,keyData[0]);
						updateQuery.setBytes(5,keyData[1]);
						updateQuery.setString(6,"E");
						updateQuery.setTimestamp(7,new Timestamp(obj.getAbsoluteFile().lastModified()));
						updateQuery.execute();
						//updateQuery.close();
						//reset data values in 'base' table
						statementStr="update Data_Table_base set status='E', Last_Modified=? where File_ID=?";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setTimestamp(1,new Timestamp(obj.getAbsoluteFile().lastModified()));
						updateQuery.setInt(2,File_ID);
						updateQuery.execute();
						//updateQuery.close();
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
						//updateQuery.close();
						break;
			
			//permanent removal of file from DB after decryption.
			case 4 :	statementStr="delete from Data_Table_base where File_ID=?";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,File_ID);
						updateQuery.execute();
						//updateQuery.close();
						break;
		}
	}
	
	boolean checkHash(String SHAStr, int table) throws Exception{
		 if(table==0){
			 statementStr="select File_ID from Data_Table_base where SHA_Sum=?";
			 updateQuery=connection.prepareStatement(statementStr);
			 updateQuery.setString(1,SHAStr);
			 queryResult=updateQuery.executeQuery();
			 return queryResult.next();
			 }
		 else{
			 if(table==1){
				 statementStr="select File_ID from Data_Table_encrypted where SHA_Sum=?";
				 updateQuery=connection.prepareStatement(statementStr);
				 updateQuery.setString(1,SHAStr);
				 queryResult=updateQuery.executeQuery();
				 return queryResult.next();
			 }
			 return false;
		 }
	}
	
	String getName(String SHAStr,int table) throws Exception{
		 if(table==0){
			 statementStr="select File_Name from Data_Table_base where SHA_Sum=?";
			 updateQuery=connection.prepareStatement(statementStr);
			 updateQuery.setString(1,SHAStr);
			 queryResult=updateQuery.executeQuery();
			 return queryResult.getString(1);
			 }
		 else{
			 if(table==1){
				 statementStr="select File_ID from Data_Table_encrypted where SHA_Sum=?";
				 updateQuery=connection.prepareStatement(statementStr);
				 updateQuery.setString(1,SHAStr);
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
			 System.out.println(queryResult.getInt(1)+" this");
			 return queryResult.getInt(1);
		}
		else{
			 if(table==ENCRYPTED_TABLE){
				 statementStr="select count(FILE_ID) from Data_Table_Encrypted";
				 queryResult=statement.executeQuery(statementStr);
				 queryResult.next();
				 System.out.println(queryResult.getInt(1)+" this");
				 return queryResult.getInt(1);
		 	 }
		return -1;
		}
	}
	
	int totalFileCount() throws SQLException{
		 statementStr="select Count(ALL File_ID) from Data_Table_base where is_Dir=false";
		 queryResult=statement.executeQuery(statementStr);
		 queryResult.first();
		 return queryResult.getInt(1);
		
	}
	
	int totalErcFileCount() throws SQLException{
		 statementStr="select Count(File_ID) from Data_Table_encrypted,";
		 queryResult=statement.executeQuery(statementStr);
		 return queryResult.getInt(1);
		
	}
	
	int getFileID(String SHAStr) throws SQLException{
		statementStr="select File_ID from Data_Table_base where SHA_Sum=?";
		updateQuery=connection.prepareStatement(statementStr);
		updateQuery.setString(1, SHAStr);
		queryResult=updateQuery.executeQuery();
		queryResult.next();
		return queryResult.getInt(1);
	}
	
	ResultSet getFileDetails(String SHAStr, int table) throws SQLException{
		if(table==0){
			statementStr="select * from Data_Table_base where SHA_Sum="+SHAStr;
			queryResult=statement.executeQuery(statementStr);
		}
		if(table==1){
			statementStr="select * from Data_Table_encrypted where SHA_Sum="+SHAStr;
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

