package com.harshitandro.FileEncryption.DatabaseTriggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.TriggerAdapter;

public class LastModified extends TriggerAdapter {

	@Override
	public void fire(Connection arg0, ResultSet arg1, ResultSet arg2) throws SQLException {
		// TODO Auto-generated method stub
		String statementStr = "update details set Last_Modified=CURRENT_TIMESTAMP()";
		Statement statement=arg0.createStatement();
		statement.executeUpdate(statementStr);
	}
	
	
}
