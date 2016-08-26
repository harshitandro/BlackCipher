package com.harshitandro.FileEncryption;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class UserInterface {

	JFrame frmBlackCipher;
	JMenu mnOptions;
	JMenu mnHelp;
	JMenuBar menuBar;
	JMenu mnFile;
	JMenuItem mntmContent;
	JMenuItem mntmAbout;
	JLabel lblWelcomeToBlackcipher;
	JLabel lblTheUltimateFilefolder;
	JPanel panelWelcome;
	JPanel panelSession;
	FileHandler fileHandlerObj;
	Encryption encryptionObj;
	Database dbObj;
	JTree tree;
	Font font;
	JScrollPane scrollPane;
	char[] password;
	String SessionID;
	String rootLocation;
	JLabel lblSessionId;
	JLabel btnDirAdd;
	
	public void reset(){}
	
	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public UserInterface() throws Exception {
		initialize();
	}
		
	/**
	 * Initialize the contents of the frame.
	 * @throws Exception 
	 */
	private void initialize() throws Exception {
		UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		frmBlackCipher = new JFrame();
		frmBlackCipher.setResizable(false);
		frmBlackCipher.getContentPane().setBackground(UIManager.getColor("ArrowButton.background"));
		frmBlackCipher.setTitle("BlackCipher : Folder Locker");
		frmBlackCipher.setForeground(Color.WHITE);
		frmBlackCipher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmBlackCipher.setBounds(new Rectangle(0, 0, 600, 700));
		frmBlackCipher.getContentPane().setName("WelcomePane");
		frmBlackCipher.getContentPane().setLayout(null);
		
		panelSession = new JPanel();
		panelSession.setVisible(false);
		panelSession.setBorder(UIManager.getBorder("TitledBorder.border"));
		panelSession.setBounds(6, 6, 582, 624);
		frmBlackCipher.getContentPane().add(panelSession);
		panelSession.setLayout(null);
		
		JLabel lblBackground = new JLabel("No Data To Display");
		lblBackground.setFont(new Font("Yu Gothic UI", Font.PLAIN, 11));
		lblBackground.setHorizontalAlignment(SwingConstants.CENTER);
		lblBackground.setBounds(217, 304, 147, 16);
		panelSession.add(lblBackground);
		
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(8, 592, 565, 19);
		progressBar.setStringPainted(true);
		panelSession.add(progressBar);
		
		JButton btnEncryptSession = new JButton("Encrypt Session");
		btnEncryptSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					fileHandlerObj.doFinal(FileHandler.ENCRYPTION_MODE);
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnEncryptSession.setBounds(435, 562, 129, 28);
		panelSession.add(btnEncryptSession);
		
		JLabel lblCurrentFileTree = new JLabel("File Tree");
		lblCurrentFileTree.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentFileTree.setBounds(244, 62, 93, 16);
		panelSession.add(lblCurrentFileTree);
		

		font = Font.createFont(Font.TRUETYPE_FONT,new File("fontello.ttf"));
		font = font.deriveFont(Font.PLAIN,18f);
		JLabel btnFileAdd = new JLabel("\ue81e");
		btnFileAdd.setBounds(540, 58, 22, 20);
		btnFileAdd.setFont(font);
		btnFileAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				JFileChooser fileChooser = new JFileChooser(new File(rootLocation));
				fileChooser.setDialogTitle("Add Files");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.showOpenDialog(null);
				File[] filesSelected=fileChooser.getSelectedFiles();
				FileTree.rootNode=new DefaultMutableTreeNode(filesSelected[0].getAbsoluteFile().getParentFile());
				for(File temp : filesSelected){
					FileTree.rootNode.add(new DefaultMutableTreeNode(temp.getAbsoluteFile()));
				}
				try {
					fileHandlerObj.createFileList();
					btnFileAdd.setVisible(false);
					btnDirAdd.setVisible(false);
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
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
				lblBackground.setVisible(false);
			}
		});
		panelSession.add(btnFileAdd);
		
		btnDirAdd = new JLabel("\ue81c");
		btnDirAdd.setBounds(510, 58, 22, 20);
		btnDirAdd.setFont(font);
		btnDirAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				JFileChooser fileChooser = new JFileChooser(new File(rootLocation));
				fileChooser.setAutoscrolls(true);
				fileChooser.setDialogTitle("Select Directory To Encrypt");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.showOpenDialog(null);
				FileTree.rootNode=(DefaultMutableTreeNode) FileTree.createTree(fileChooser.getSelectedFile().getAbsoluteFile());
				try {
					fileHandlerObj.createFileList();
					btnFileAdd.setVisible(false);
					btnDirAdd.setVisible(false);
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
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
				lblBackground.setVisible(false);
			}
		});
		panelSession.add(btnDirAdd);
		
		JLabel btnSearch = new JLabel("\ue805");
		btnSearch.setBounds(480, 58, 22, 20);
		btnSearch.setFont(font);
		panelSession.add(btnSearch);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(18, 78, 545, 483);
		scrollPane.setViewportBorder(null);
		panelSession.add(scrollPane);
		
		panelWelcome = new JPanel();
		panelWelcome.setBounds(50, 239, 484, 159);
		frmBlackCipher.getContentPane().add(panelWelcome);
		panelWelcome.setVisible(true);
		panelWelcome.setLayout(null);
		
		lblWelcomeToBlackcipher = new JLabel("Welcome to BlackCipher");
		lblWelcomeToBlackcipher.setBounds(0, 0, 484, 48);
		panelWelcome.add(lblWelcomeToBlackcipher);
		lblWelcomeToBlackcipher.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcomeToBlackcipher.setHorizontalTextPosition(SwingConstants.CENTER);
		lblWelcomeToBlackcipher.setFont(new Font("Consolas", Font.BOLD, 40));
		
		lblTheUltimateFilefolder = new JLabel("The Simplified File/Folder Encryption utility");
		lblTheUltimateFilefolder.setBounds(57, 55, 370, 25);
		panelWelcome.add(lblTheUltimateFilefolder);
		lblTheUltimateFilefolder.setFont(new Font("Candara", Font.PLAIN, 20));
		lblTheUltimateFilefolder.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton btnNewSession = new JButton("New Session");
		btnNewSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				encryptionObj= new Encryption();
				JPanel panel = new JPanel();
				panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
				JLabel lblPassword = new JLabel("New Password : ");
				JLabel lblLocation = new JLabel("Root Location :"); 
				JTextField locationField = new JTextField(20);
				locationField.setEditable(false);
				JPasswordField passwordField = new JPasswordField(20);
				JButton btnLocationBrowse = new JButton("Browse");
				panel.add(lblPassword);
				panel.add(passwordField);
				panel.add(lblLocation);
				panel.add(locationField);
				panel.add(btnLocationBrowse);
				btnLocationBrowse.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						JFileChooser locationChooser = new JFileChooser("Choose Root Directory");
						locationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						locationChooser.showOpenDialog(null);
						rootLocation = locationChooser.getSelectedFile().getAbsoluteFile().getAbsolutePath();
						locationField.setText(rootLocation);
					}
				});
				String[] options = {"OK","Cancel"};
				int selection = JOptionPane.showOptionDialog(null,panel,"Password For New Session",JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,null, options, options[0]);
				if(selection==0 && !locationField.getText().isEmpty()){
					try {
						password=passwordField.getPassword();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					panelSession.setVisible(true);
					panelWelcome.setVisible(false);
					SessionID=FileHandler.randomSessionIDGenerator();
					lblSessionId = new JLabel("Session ID : " +  SessionID);
					lblSessionId.setHorizontalAlignment(SwingConstants.CENTER);
					lblSessionId.setBounds(174, 21, 226, 16);
					panelSession.add(lblSessionId);
					try {
						fileHandlerObj = new FileHandler(SessionID,password.toString(),true,rootLocation);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			
			}
		});
		btnNewSession.setBounds(69, 131, 102, 28);
		panelWelcome.add(btnNewSession);
		
		JButton btnLoadSession = new JButton("Load Session");
		btnLoadSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				encryptionObj= new Encryption();
				JPanel panel = new JPanel();
				JLabel lblPassword =new JLabel("Enter Master Password * ");
				JPasswordField passwordField = new JPasswordField(20);
				panel.add(lblPassword);
				panel.add(passwordField);
				String[] options = {"OK","Cancel"};
				int selection = JOptionPane.showOptionDialog(null,panel,"Load Session",JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,null, options, options[0]);
				try {
					if(selection==0 && passwordField.getPassword().length!=0){
						panelSession.setVisible(true);
						panelWelcome.setVisible(false);
						// to build seesion id from db
						lblSessionId = new JLabel("Session ID : " +  SessionID);
						lblSessionId.setHorizontalAlignment(SwingConstants.CENTER);
						lblSessionId.setBounds(174, 21, 226, 16);
						panelSession.add(lblSessionId);
						// To do : get passwd frm db n verify it with this password.
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if( selection==0 && (passwordField.getPassword().length == 0 )){
					JOptionPane.showMessageDialog(null,"Mandatory Field Can't Be Left Empty","Invaild Input", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnLoadSession.setBounds(304, 131, 105, 28);
		panelWelcome.add(btnLoadSession);
		
		menuBar = new JMenuBar();
		frmBlackCipher.setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenu mnSession = new JMenu("Session");
		mnFile.add(mnSession);
		
		JMenuItem mntmNewSession = new JMenuItem("New Session");
		mnSession.add(mntmNewSession);
		
		JMenuItem mntmSaveCurrentSession = new JMenuItem("Save Current Session");
		mnSession.add(mntmSaveCurrentSession);
		
		JMenuItem mntmLoadSession = new JMenuItem("Load Session");
		mnSession.add(mntmLoadSession);
		
		JMenuItem mntmReset = new JMenuItem("Reset");
		mnFile.add(mntmReset);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);
		
		JMenuItem mntmGenerateRandomKey = new JMenuItem("Generate Random Master Password");
		mnOptions.add(mntmGenerateRandomKey);
		
		JMenu mnDatabase = new JMenu("Database");
		mnOptions.add(mnDatabase);
		
		JMenuItem mntmBackup = new JMenuItem("Backup");
		mnDatabase.add(mntmBackup);
		
		JMenuItem mntmMoveToOther = new JMenuItem("Move To Other Location");
		mnDatabase.add(mntmMoveToOther);
		
		JMenuItem mntmPlurgeSession = new JMenuItem("Plurge Session");
		mnDatabase.add(mntmPlurgeSession);
		
		JMenu mnDecryption = new JMenu("Decryption");
		mnOptions.add(mnDecryption);
		
		JMenu mnDecryptSession = new JMenu("Decrypt Session");
		mnDecryption.add(mnDecryptSession);
		
		JMenuItem mntmToDefaultLocation = new JMenuItem("To Default Location");
		mnDecryptSession.add(mntmToDefaultLocation);
		
		JMenuItem mntmToOtherLocation = new JMenuItem("To Other Location");
		mnDecryptSession.add(mntmToOtherLocation);
		
		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		mntmContent = new JMenuItem("Help Contents");
		mnHelp.add(mntmContent);
		
		mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		
	}
	public JMenu getMnOptions() {
		return mnOptions;
	}
	public JMenu getMnHelp() {
		return mnHelp;
	}
	public JMenuBar getMenuBar() {
		return menuBar;
	}
	public JMenu getMnFile() {
		return mnFile;
	}
	public JMenuItem getMntmContent() {
		return mntmContent;
	}
	public JMenuItem getMntmAbout() {
		return mntmAbout;
	}
	public JTree getTree() {
		return tree;
	}
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	public JPanel getPanelSession() {
		return panelSession;
	}
}
