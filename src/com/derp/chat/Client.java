package com.derp.chat;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
	
	private static Socket socket;
	BufferedReader in;
	PrintWriter out;
	String line;
	String name;
	JFrame frame = new JFrame("Chat Client");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);
	JScrollPane scrollPane = new JScrollPane(messageArea);
	HashMap<String, JLabel> users = new HashMap<String, JLabel>();
	JPanel userTab = new JPanel();
	
	public Client() {
		frame.setPreferredSize(new Dimension(640, 480));
		frame.setResizable(false);
		textField.setEditable(false);
		messageArea.setEditable(false);
		userTab.setLayout(null);
		frame.setLayout(null);
		frame.getContentPane().add(userTab);
		frame.getContentPane().add(scrollPane);
		frame.getContentPane().add(textField);
		
		userTab.setBounds(0,  0,  100,  450);
		messageArea.setBounds(100, 0, 535, 425);
		scrollPane.setBounds(messageArea.getBounds());
		textField.setBounds(100, 425, 535, 25);
		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				out.println(textField.getText());
				if (out.checkError()) {
					System.out.println("Something went wrong...");
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				textField.setText("");
				
			}
		});
	}
	
	private String getHost() {
		return JOptionPane.showInputDialog(frame, "Enter the IP address of the server: ", "Welcome!", JOptionPane.QUESTION_MESSAGE);
	}
	
	private String getName() {
		return JOptionPane.showInputDialog(frame, "Choose a nickname: ", "Select nickname", JOptionPane.PLAIN_MESSAGE);
	}
	
	public void run() throws IOException {
		
		String address = getHost();
		socket = new Socket(address, 50007);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		
		int i = 0;
		while (true) {
			SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
			String formattedDate = df.format(new Date());
			try {
				line = in.readLine();
			} catch (IOException ioe) {}
			if (line != null && !socket.isClosed()) {
				if (line.startsWith("/nick")) {
					name = getName();
					out.println(name);
					frame.setTitle("Chat Client v0.0.6 - " + name);
					frame.setVisible(true);
				} else if (line.startsWith("+nickaccepted")) {
					textField.setEditable(true);
				} else if (line.startsWith("/broadcast")) {
					messageArea.append("[" + formattedDate + "] " + line.substring(11) + "\n");
				} else if (line.startsWith("/adduser")) {
					String[] input = line.split(" ");
					JLabel user = new JLabel();
					user.setText(input[1]);
					users.put(input[1], user);
					System.out.println(users.size());
					user.setBounds(0, users.size() * 16 - 16, 100, 16);
					userTab.add(user);
					userTab.revalidate();
					userTab.repaint();
				} else if (line.startsWith("/removeuser")) {
					String[] input = line.split(" ");
					JLabel toBeRemoved = users.get(input[1]);
					userTab.remove(toBeRemoved);
					userTab.revalidate();
					userTab.repaint();
				}
			}
			out.println("/test");
			if (i > 20) {
				i = 0;
				if (out.checkError()) {
					break;
				}
			}
			i += 1;
		}
		
	}

	public static void main(String[] args) throws IOException {
		
			Client client = new Client();
			client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			client.run();
			socket.close();
			JOptionPane.showMessageDialog(client.frame, "You have been kicked!", "Kicked", JOptionPane.PLAIN_MESSAGE);
			System.exit(0);
	}
	
}
