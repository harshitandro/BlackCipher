package com.harshitandro.FileEncryption.DatabaseTriggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.TriggerAdapter;

public class TotalFileCount extends TriggerAdapter{

	@Override
	public void fire(Connection arg0, ResultSet arg1, ResultSet arg2) throws SQLException {
		String statementStr = "update details set total_files=("
				+ "select count(file_id) from data_table_base where is_dir='FALSE'"
				+ ")";	
		Statement statement=arg0.createStatement();
		statement.executeUpdate(statementStr);
	}



}
