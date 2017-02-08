/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

/**
 * @author harshitandro
 */
public class FileTree extends JTree {
	static DefaultMutableTreeNode rootNode;
	
	static MutableTreeNode createTreeNew(File node) {
		 DefaultMutableTreeNode ret = new DefaultMutableTreeNode(node);
	      if (node.isDirectory())
	         for (File child: node.listFiles())
	            ret.add(createTreeNew(child));
	      return ret;
    }
	
	
}

