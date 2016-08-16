package com.harshitandro.FileEncryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sun.nio.zipfs.ZipDirectoryStream;

public class Testing {
	
	public static void main(String[] args) throws Exception {
		FileHandler fileHandlerObj = new FileHandler("ABCDEFGHI","as",true,"D:\\testing\\Song");
		FileTree.rootNode=(DefaultMutableTreeNode) FileTree.createTree(new File("D:\\testing\\Song").getAbsoluteFile());
		fileHandlerObj.createFileList();
		fileHandlerObj.printFileStorage();
		boolean flag =fileHandlerObj.doFinal(1);
		if(flag)
			System.out.println("Encryption Done");
			
		else 
			System.out.println("Encryption Failed");
		fileHandlerObj.zipDB();
	}			
}
