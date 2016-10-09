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
import java.util.Calendar;
import java.util.Date;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sun.org.apache.bcel.internal.generic.NEW;

import sun.util.resources.cldr.ur.CurrencyNames_ur;

public class Database {
	String sessionID;
	Connection connection;
	String statementStr;
	String subStatementStr;
	Statement statement;
	String rootDir;
	PreparedStatement updateQuery;
	PreparedStatement subQuery ;
	ResultSet queryResult;
	static final int  BASE_TABLE = 0;
	static final int ENCRYPTED_TABLE = 1;
	
	Database(String sessionIDNumber,String password,boolean toCreate,String rootDirParent) throws Exception{
		rootDir=rootDirParent=new File(rootDirParent).getAbsoluteFile().getParent();
		sessionID=sessionIDNumber;
		Class.forName("org.h2.Driver").newInstance();
		if(toCreate)
			connection = DriverManager.getConnection("jdbc:h2:"+rootDirParent+File.separator+sessionID+";MV_STORE=FALSE",sessionID,password);
		else
			connection = DriverManager.getConnection("jdbc:h2:"+rootDirParent+File.separator+sessionID+";MV_STORE=FALSE;IFEXISTS=TRUE",sessionID,password);
		
		statement= connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		if(toCreate)
			createDB();
	}
	
	void createDB() throws Exception{
		statementStr="create table Details ("
				+ "session_ID varchar(40) not null,"
				+ "Creation_Date timestamp not null,"
				+ "Last_Access timestamp,"
				+ "Last_Modified timestamp,"
				+ "Enc_Files bigint,"
				+ "Total_Files bigint ,"
				+ "Total_Size_enc bigint,"
				+ "Total_Size bigint"
				+ ")";
		statement.executeUpdate(statementStr);
	
		statementStr="create table Data_Table_base ("
				+ "File_ID int not null,"
				+ "File_Name varchar(255),"
				+ "Parent varchar(255) not null,"
				+ "is_Dir boolean not null,"
				+ "MD5_Sum varchar(128) ,"
				+ "Size bigint,"
				+ "Last_Modified timestamp ,"
				+ "Status char,"
				+ "primary key(File_ID,MD5_Sum))";
		statement.executeUpdate(statementStr);

		statementStr="create table Data_Table_encrypted ("
				+ "File_ID int not null,"
				+ "File_Name varchar(50) not null,"
				+ "MD5_Sum varchar(128) not null ,"
				+ "Key_Value blob not null,"
				+ "IV blob not null,"
				+ "Size bigint,"
				+ "Status char not null,"
				+ "Last_Modified timestamp not null,"
				+ "primary key(File_ID,MD5_Sum))";
		statement.executeUpdate(statementStr);
			
		statementStr="CREATE TRIGGER enc_file_count " 
				+ "AFTER INSERT,DELETE "
				+ "ON data_table_encrypted  "
				+ "FOR EACH ROW "
				+ "CALL \""
				+  com.harshitandro.FileEncryption.DatabaseTriggers.EncFileCount.class.getName()+"\"";
		statement.executeUpdate(statementStr);
		
		statementStr="CREATE TRIGGER enc_file_size "
				+ "AFTER INSERT,UPDATE,DELETE "
				+ "ON data_table_encrypted "
				+ "FOR EACH ROW "
				+ "CALL \""
				+  com.harshitandro.FileEncryption.DatabaseTriggers.EncFileSize.class.getName()+"\"";
		statement.executeUpdate(statementStr);
		
		statementStr="CREATE TRIGGER last_modified "
				+ "AFTER INSERT,UPDATE,DELETE "
				+ "ON data_table_base "
				+ "FOR EACH ROW "
				+ "CALL \""
				+ com.harshitandro.FileEncryption.DatabaseTriggers.LastModified.class.getName()+"\"";
		statement.executeUpdate(statementStr);
		
		statementStr="CREATE TRIGGER total_file_count "
				+ "AFTER INSERT,DELETE "
				+ "ON data_table_base "
				+ "FOR EACH ROW "
				+ "CALL \""
				+ com.harshitandro.FileEncryption.DatabaseTriggers.TotalFileCount.class.getName()+"\"";
		statement.executeUpdate(statementStr);
		
		statementStr="CREATE TRIGGER total_file_size "
				+ "AFTER INSERT,UPDATE,DELETE "
				+ "ON data_table_base "
				+ "FOR EACH ROW "
				+ "CALL \""
				+ com.harshitandro.FileEncryption.DatabaseTriggers.TotalFileSize.class.getName()+ "\"";
		statement.executeUpdate(statementStr);
		updateDB(0, null,0, null);
	}

	void updateDB(int File_ID,File obj , int target , byte[]...keyData) throws Exception{
		switch(target){
			//input DB detail table
			case 0 : 		statementStr="insert into Details(session_id,Creation_date) values(?,?)";
							updateQuery=connection.prepareStatement(statementStr);
							updateQuery.setString(1,sessionID);
							updateQuery.setTimestamp(2,new Timestamp(new Date().getTime()));
							updateQuery.execute();
							break;
						
					
			//Adds new file data to "base" table. 
			case 1 :   	statementStr="insert into Data_Table_base values (?,?,?,?,?,?,?,?)";
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
						//System.out.println("Test " +obj.getAbsoluteFile().getParent().substring(rootDir.length()));
						updateQuery.setString(3,obj.getAbsoluteFile().getParent().substring(rootDir.length()));
						updateQuery.setBoolean(4,obj.getAbsoluteFile().isDirectory());
						updateQuery.setLong(6,FileHandler.sizeOfFile(obj.getAbsoluteFile()));
						updateQuery.setTimestamp(7,new Timestamp(obj.getAbsoluteFile().lastModified()));
						updateQuery.setString(8,"D");
						updateQuery.execute();
						break;
			
			//upon encryption , generate "encrypted" table entry for the given file 
			case 2 :	statementStr="insert into Data_Table_encrypted values(?,?,?,?,?,?,?,?)";
						updateQuery=connection.prepareStatement(statementStr);
						updateQuery.setInt(1,File_ID);
						updateQuery.setString(2,obj.getAbsoluteFile().getName());
						updateQuery.setString(3, FileHandler.createHash(obj.getAbsoluteFile()));
						updateQuery.setBlob(4,new ByteArrayInputStream(keyData[0]));
						updateQuery.setBlob(5,new ByteArrayInputStream(keyData[1]));
						updateQuery.setLong(6,FileHandler.sizeOfFile(obj.getAbsoluteFile()));
						updateQuery.setString(7,"E");
						updateQuery.setTimestamp(8,new Timestamp(obj.getAbsoluteFile().lastModified()));
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
						updateQuery.execute();
						statementStr="update Data_Table_base set status='D', Last_Modified=? where File_ID=?";
						updateQuery= connection.prepareStatement(statementStr);
						updateQuery.setTimestamp(1,new Timestamp(Calendar.getInstance().getTimeInMillis()));
						updateQuery.setInt(2,File_ID);
						updateQuery.execute();
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
		 queryResult.next();
		 return queryResult.getInt(1);
		
	}
	
	int totalErcFileCount() throws SQLException{
		 statementStr="select Count(File_ID) from Data_Table_encrypted where status='E'";
		 queryResult=statement.executeQuery(statementStr);
		 queryResult.next();
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
	
	ResultSet findFileDetails(String fileName,String parent) throws SQLException{
			statementStr="select * from Data_Table_base where File_Name='"+fileName+"' ";//and Parent='"+parent+"'";
			//System.out.println(statementStr);
			queryResult=statement.executeQuery(statementStr);
		return queryResult;
	}
	
	ResultSet getFileDetails(String MD5Str, int table) throws SQLException{
		if(table==0){
			statementStr="select * from Data_Table_base where MD5_Sum='"+MD5Str+"'";
			queryResult=statement.executeQuery(statementStr);
		}
		if(table==1){
			statementStr="select * from Data_Table_encrypted where MD5_Sum='"+MD5Str+"'";
			//System.out.println(statementStr);
			queryResult=statement.executeQuery(statementStr);
		}
		return queryResult;
	}
	
	ResultSet getFileDetails(int File_ID, int table) throws SQLException, InterruptedException{
		queryResult=null;
		if(table==0){
			statementStr="select * from Data_Table_base where FILE_ID='"+File_ID+"'";
			queryResult=statement.executeQuery(statementStr);
		}
		if(table==1){
			statementStr="select * from Data_Table_encrypted where FILE_ID='"+File_ID+"'";
			queryResult=statement.executeQuery(statementStr);
		}
		Thread.currentThread().sleep(40);
		return queryResult;
	}
	
	ResultSet getDBDetails() throws SQLException{
		statementStr="select * from Details";
		queryResult=statement.executeQuery(statementStr);
		return queryResult;
	}
	
	boolean changeDBPassword(String password) {
		statementStr = "alter user "+sessionID+" set password '"+password+"'";
		try {
			statement.execute(statementStr);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

