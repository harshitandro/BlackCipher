package com.harshitandro.FileEncryption;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.SQLException;

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
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import jdk.nashorn.internal.scripts.JO;

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
	JTree tree;
	Font font;
	JScrollPane scrollPane;
	char[] password;
	String SessionID;
	String rootLocation;
	JLabel lblSessionId;
	JLabel btnDirAdd;
	JButton btnEncryptSession;
	JLabel btnFileAdd;
	JLabel lblBackground;
	int mode = -1;
	boolean flagUnclean = false;
	private JProgressBar progressBar;
	private JMenu mnSessionOptions;
	private JMenuItem mntmSaveCurrentSession;
	private JButton btnDecryptSession;

	void reset() {
		try {
			if(fileHandlerObj!=null)
			fileHandlerObj.databaseObj.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileHandlerObj = null;
		panelSession.setVisible(false);
		panelWelcome.setVisible(true);
		mnSessionOptions.setEnabled(false);
		mntmSaveCurrentSession.setEnabled(false);
		if(lblSessionId!=null){
			lblSessionId.setText("");
			lblSessionId =null;
		}
		flagUnclean=false;
		FileTree.rootNode =null;
		tree=null;
		scrollPane.setViewportView(null);
		lblBackground.setVisible(true);
		btnDirAdd.setVisible(true);
		btnFileAdd.setVisible(true);
		btnDecryptSession.setEnabled(true);
		btnDecryptSession.setVisible(true);
		btnEncryptSession.setEnabled(true);
		btnEncryptSession.setVisible(true);
	}
	
	void setProgressBar(int toSet,String...values){
		switch(toSet){
		// to set progress bar to "Indeterminent" .
			case 1 : progressBar.setString(values[0]);
					 progressBar.setIndeterminate(true);
					 break;
		// to set progress bar to specified progress percentage
			case 2 : progressBar.setValue(Integer.parseInt(values[1]));
					 progressBar.setString(values[0]+" : " +progressBar.getValue()+"%");
					 break;
		}
	}

	void resetProgressBar(){
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
	}
	
	/**
	 * Create the application.
	 * 
	 * @throws Exception
	 */
	public UserInterface() throws Exception {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws Exception
	 */
	private void initialize() throws Exception {
		UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		frmBlackCipher = new JFrame();
		frmBlackCipher.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmBlackCipher.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(!flagUnclean)
					System.exit(0);
				else{
					String[] options ={"Yes","No"};
					int selection=JOptionPane.showConfirmDialog(null, "Current Session is in dirty state.Any uncommited changes will be discarded.\nDo you wish to proceed ?","Terminate Session",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
					if(selection == 0)
						System.exit(0);
				}				
			}
		});
		frmBlackCipher.setResizable(false);
		frmBlackCipher.getContentPane().setBackground(UIManager.getColor("ArrowButton.background"));
		frmBlackCipher.setTitle("BlackCipher : Folder Locker");
		frmBlackCipher.setForeground(Color.WHITE);
		frmBlackCipher.setBounds(new Rectangle(0, 0, 600, 700));
		frmBlackCipher.getContentPane().setName("WelcomePane");
		frmBlackCipher.getContentPane().setLayout(null);

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
				String[] options = { "OK", "Cancel" };
				int selection = JOptionPane.showOptionDialog(null, panel, "Password For New Session",
						JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (selection == 0 && !(locationField.getText().isEmpty() || passwordField.getPassword().length == 0)) {
					try {
						password = passwordField.getPassword();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					Thread t = new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							int i = 0;
							while(i<100){
								setProgressBar(1, "Progress",Integer.toString(i));
								i++;
								try {
									Thread.sleep(2);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					});
					//t.run();
					mode = FileHandler.ENCRYPTION_MODE;
					panelSession.setVisible(true);
					panelWelcome.setVisible(false);
					SessionID = FileHandler.randomSessionIDGenerator();
					lblSessionId = new JLabel("Session ID : " + SessionID);
					lblSessionId.setHorizontalAlignment(SwingConstants.CENTER);
					lblSessionId.setBounds(174, 21, 226, 16);
					panelSession.add(lblSessionId);
					try {
						fileHandlerObj = new FileHandler(SessionID, new String(password), true, rootLocation);
						mnSessionOptions.setEnabled(true);
						mntmSaveCurrentSession.setEnabled(true);
						btnDecryptSession.setVisible(false);
						flagUnclean = true;
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				} else if (selection == 0
						&& (locationField.getText().isEmpty() || passwordField.getPassword().length == 0))
					JOptionPane.showMessageDialog(null, "Mandatory Field Can't Be Left Empty", "Invaild Input",
							JOptionPane.WARNING_MESSAGE);
			}
		});
		btnNewSession.setBounds(69, 131, 102, 28);
		panelWelcome.add(btnNewSession);

		JButton btnLoadSession = new JButton("Load Session");
		btnLoadSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JPanel panel = new JPanel();
				JLabel lblPassword = new JLabel("Enter Master Password * ");
				JPasswordField passwordField = new JPasswordField(20);
				JLabel lblLocation = new JLabel("Location * ");
				JTextField locationField = new JTextField(20);
				panel.add(lblPassword);
				panel.add(passwordField);
				panel.add(lblLocation);
				panel.add(locationField);
				String[] options = { "OK", "Cancel" };
				JOptionPane dialogue = new JOptionPane();
				dialogue.setLayout(null);
				locationField.setEditable(false);
				locationField.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						JFileChooser locationChooser = new JFileChooser("Choose Session File");
						locationChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						locationChooser.showOpenDialog(null);
						rootLocation = locationChooser.getSelectedFile().getAbsoluteFile().getAbsolutePath();
						locationField.setText(rootLocation);
					}
				});
				int selection = dialogue.showOptionDialog(null, panel, "Load Session", JOptionPane.NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				try {
					if (selection == 0
							&& !(locationField.getText().isEmpty() || passwordField.getPassword().length == 0)) {
						// to build session id from db
						mode = FileHandler.DECRYPTION_MODE;
						SessionID = new File(locationField.getText()).getAbsoluteFile().getName().substring(0, 10);
						password = passwordField.getPassword();
						lblSessionId = new JLabel("Session ID : " + SessionID);
						lblSessionId.setHorizontalAlignment(SwingConstants.CENTER);
						lblSessionId.setBounds(174, 21, 226, 16);
						panelSession.add(lblSessionId);
						// To do : get passwd frm db n verify it with this
						// password.
						fileHandlerObj = new FileHandler(SessionID, new String(password), false, rootLocation);
						mnSessionOptions.setEnabled(true);
						mntmSaveCurrentSession.setEnabled(true);
						fileHandlerObj.getFileTreeFromDB();
						tree = new JTree(FileTree.rootNode);
						tree.setCellRenderer(new DefaultTreeCellRenderer() {
							public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
									boolean expanded, boolean leaf, int row, boolean hasFocus) {
								super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
								Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
								if (userObject instanceof File) {
									setText(((File) userObject).getName());
								}
								return this;
							}
						});
						scrollPane.setViewportView(tree);
						tree.setBackground(new Color(245, 245, 245));
						tree.setBorder(new LineBorder(new Color(169, 169, 169), 4, true));
						tree.setRootVisible(true);
						lblBackground.setVisible(false);
						btnDirAdd.setVisible(false);
						btnFileAdd.setVisible(false);
						//btn enable function here
						panelSession.setVisible(true);
						panelWelcome.setVisible(false);
					}
					if (selection == 0
							&& (locationField.getText().isEmpty() || passwordField.getPassword().length == 0))
						JOptionPane.showMessageDialog(null, "Mandatory Field Can't Be Left Empty", "Invaild Input",
								JOptionPane.WARNING_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Pasaword entered seems incorrect.Access Denied",
							"Invaild Password", JOptionPane.ERROR_MESSAGE);
				}
				flagUnclean=true;
			}
		});
		btnLoadSession.setBounds(304, 131, 105, 28);
		panelWelcome.add(btnLoadSession);
				
		panelSession = new JPanel();
		panelSession.setBounds(6, 6, 582, 624);
		panelSession.setVisible(false);
		panelSession.setBorder(UIManager.getBorder("TitledBorder.border"));
		frmBlackCipher.getContentPane().add(panelSession);
		panelSession.setLayout(null);
				
		lblBackground = new JLabel("No Data To Display");
		lblBackground.setFont(new Font("Yu Gothic UI", Font.PLAIN, 11));
		lblBackground.setHorizontalAlignment(SwingConstants.CENTER);
		lblBackground.setBounds(217, 304, 147, 16);
		panelSession.add(lblBackground);
		
		JLabel lblCurrentFileTree = new JLabel("File Tree");
		lblCurrentFileTree.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentFileTree.setBounds(244, 62, 93, 16);
		panelSession.add(lblCurrentFileTree);
		btnFileAdd = new JLabel("\ue81e");
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
				File[] filesSelected = fileChooser.getSelectedFiles();
				FileTree.rootNode = new DefaultMutableTreeNode(filesSelected[0].getAbsoluteFile().getParentFile());
				for (File temp : filesSelected) {
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
				tree.setCellRenderer(new DefaultTreeCellRenderer() {
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
						boolean expanded, boolean leaf, int row, boolean hasFocus) {
					super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
					if (userObject instanceof File) {
						setText(((File) userObject).getName());
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
				JFileChooser fileChooser = new JFileChooser(new File(rootLocation));
				fileChooser.setAutoscrolls(true);
				fileChooser.setDialogTitle("Select Directory To Encrypt");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.showOpenDialog(null);
				FileTree.rootNode = (DefaultMutableTreeNode) FileTree.createTree(fileChooser.getSelectedFile().getAbsoluteFile());
				try {
					fileHandlerObj.createFileList();
					btnFileAdd.setVisible(false);
					btnDirAdd.setVisible(false);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				tree = new JTree(FileTree.rootNode);
				tree.setCellRenderer(new DefaultTreeCellRenderer() {
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
				boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObject instanceof File) {
					setText(((File) userObject).getName());
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
		
		btnDecryptSession = new JButton("Decrypt Session");
		btnDecryptSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					try {
						fileHandlerObj.doFinal(FileHandler.DECRYPTION_MODE);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		});
		
		btnEncryptSession = new JButton("Encrypt Session");
		btnEncryptSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			try {
					fileHandlerObj.doFinal(FileHandler.ENCRYPTION_MODE);
					flagUnclean=false;
				} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				}
			}
		});
		btnEncryptSession.setBounds(435, 562, 129, 28);
		panelSession.add(btnEncryptSession);
		btnDecryptSession.setBounds(294, 562, 129, 28);
		panelSession.add(btnDecryptSession);
		scrollPane = new JScrollPane();
		scrollPane.setBounds(18, 78, 545, 483);
		scrollPane.setViewportBorder(null);
		panelSession.add(scrollPane);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(8, 592, 565, 19);
		progressBar.setStringPainted(true);
		panelSession.add(progressBar);

		font = Font.createFont(Font.TRUETYPE_FONT, new File("fontello.ttf"));
		font = font.deriveFont(Font.PLAIN, 18f);

		menuBar = new JMenuBar();
		frmBlackCipher.setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenu mnSessionFIle = new JMenu("Session");
		mnFile.add(mnSessionFIle);

		JMenuItem mntmNewSession = new JMenuItem("New Session");
		mnSessionFIle.add(mntmNewSession);

		mntmSaveCurrentSession = new JMenuItem("Save Current Session");
		mntmSaveCurrentSession.setEnabled(false);
		mnSessionFIle.add(mntmSaveCurrentSession);

		JMenuItem mntmLoadSession = new JMenuItem("Load Session");
		mnSessionFIle.add(mntmLoadSession);

		JMenuItem mntmReset = new JMenuItem("Reset");
		mntmReset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!flagUnclean)
					reset();
				else{
					String[] options ={"Yes","No"};
					int selection=JOptionPane.showConfirmDialog(null, "Current Session is in dirty state.Any uncommited changes will be discarded.\nDo you wish to proceed ?","Terminate Session",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
					if(selection == 0)
						reset();
				}
			}
		});
		mnFile.add(mntmReset);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!flagUnclean)
					System.exit(0);
				else{
					String[] options ={"Yes","No"};
					int selection=JOptionPane.showConfirmDialog(null, "Current Session is in dirty state.Any uncommited changes will be discarded.\nDo you wish to proceed ?","Terminate Session",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
					if(selection == 0)
						System.exit(0);
				}
			}
		});
		mnFile.add(mntmExit);

		mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);

		JMenuItem mntmGenerateRandomKey = new JMenuItem("Generate Random Master Password");
		mnOptions.add(mntmGenerateRandomKey);

		mnSessionOptions = new JMenu("Session");
		mnSessionOptions.setEnabled(false);
		mnOptions.add(mnSessionOptions);

		JMenuItem mntmBackup = new JMenuItem("Backup");
		mnSessionOptions.add(mntmBackup);

		JMenuItem mntmMoveToOther = new JMenuItem("Move To Other Location");
		mnSessionOptions.add(mntmMoveToOther);

		JMenuItem mntmPlurgeSession = new JMenuItem("Plurge Session");
		mnSessionOptions.add(mntmPlurgeSession);
		
		JMenuItem mntmChangePassword = new JMenuItem("Change Password");
		mntmChangePassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String password=JOptionPane.showInputDialog(frmBlackCipher,"Enter New Password ","New Password",JOptionPane.QUESTION_MESSAGE);
				if(fileHandlerObj.databaseObj.changeDBPassword(password))
					JOptionPane.showMessageDialog(frmBlackCipher,"Password Changed Successfully","Session's Password Changes",JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(frmBlackCipher,"Password Change Failed","Failed",JOptionPane.WARNING_MESSAGE);
			}
		});
		mnSessionOptions.add(mntmChangePassword);

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
