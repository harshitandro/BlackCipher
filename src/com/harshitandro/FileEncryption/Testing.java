package com.harshitandro.FileEncryption;

import java.io.File;
import java.util.Random;

public class Testing {

	public static void main(String[] args) throws Exception {
		Database db = new Database("hello123","123456789",false);
		System.out.println("hell ok ");
		//db.createDB();
		db.updateDB(new Random().nextInt(),new File("/home/harshitandro/new-fxml-settings.xml"),1,0,null);
		System.out.println(db.totalFileCount());
	}

}
