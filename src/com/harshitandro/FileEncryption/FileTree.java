/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.File;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * @author harshitandro
 *
 */
public class FileTree extends JTree {
	static DefaultMutableTreeNode rootNode;
	
	static MutableTreeNode createTree(File node){
		DefaultMutableTreeNode ret = null;
		if (node.getAbsoluteFile().isDirectory()){
	    	ret = new DefaultMutableTreeNode(node.getAbsoluteFile());
	    	for (File child: node.getAbsoluteFile().listFiles()){
	    		ret.add(createTree(child.getAbsoluteFile()));
	    	}
		}
		if (node.getAbsoluteFile().isFile())
			ret =  new DefaultMutableTreeNode(node.getAbsoluteFile());
		return ret;
	}
	
}

class FileTreeRenderer{}