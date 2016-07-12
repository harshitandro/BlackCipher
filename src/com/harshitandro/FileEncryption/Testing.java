package com.harshitandro.FileEncryption;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.swing.tree.DefaultMutableTreeNode;

public class Testing {

	public static void main(String[] args) throws Exception {
		FileHandler fileHandlerObj = new FileHandler("hs28071997lodha",FileHandler.CREATE_DB_TRUE,"/home/harshitandro/testing");
		FileTree.rootNode=(DefaultMutableTreeNode) FileTree.createTree(new File("/home/harshitandro/testing/test").getAbsoluteFile());
		fileHandlerObj.createFileList();
		fileHandlerObj.printFileStorage();
		boolean flag =fileHandlerObj.doFinal(1,fileHandlerObj.fileList);
		if(flag)
			System.out.println("Encryption Done");
			
		else 
			System.out.println("Encryption Failed");
		
		
	}

}
