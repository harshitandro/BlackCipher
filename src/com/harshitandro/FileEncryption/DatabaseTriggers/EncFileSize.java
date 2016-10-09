package com.harshitandro.FileEncryption.DatabaseTriggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.TriggerAdapter;

public class EncFileSize extends TriggerAdapter {

	@Override
	public void fire(Connection arg0, ResultSet arg1, ResultSet arg2) throws SQLException {
		String statementStr = "update details set total_size_enc=("
				+ "select sum(Size) from data_table_encrypted"
				+ ")";	
		Statement statement=arg0.createStatement();
		statement.executeUpdate(statementStr);
	}
}
