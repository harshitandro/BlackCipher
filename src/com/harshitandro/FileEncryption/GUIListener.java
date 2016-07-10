/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
/**
 * @author harshitandro
 *
 */
public class GUIListener implements ActionListener , MouseListener{
	
	public GUIListener() throws IOException, FontFormatException {
		// TODO Auto-generated constructor stub
    }

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		JFileChooser fileChooser = new JFileChooser();
		if(((JLabel)e.getSource()).getText().equals("\ue81e")){
			fileChooser.setDialogTitle("temp");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		if(((JLabel)e.getSource()).getText().equals("\ue81c")){
			fileChooser.setDialogTitle("temp");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		fileChooser.showOpenDialog(null);
		FileTree.rootNode=(DefaultMutableTreeNode) FileTree.createTree(fileChooser.getSelectedFile().getAbsoluteFile());
		fileHandlerObj.createFileList();
		try {
			fileHandlerObj.printFileStorage();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tree = new JTree(FileTree.rootNode);
		tree.setCellRenderer(new DefaultTreeCellRenderer(){
			 public Component getTreeCellRendererComponent(JTree tree, Object value,boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			        if (userObject instanceof File) {
			            setText(((File)userObject).getName());
			        }
			        return this;
			    }
		});
		scrollPane.setViewportView(tree);
		tree.setBackground(new Color(245, 245, 245));
		tree.setBorder(new LineBorder(new Color(169, 169, 169), 4, true));
		tree.setRootVisible(true);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
