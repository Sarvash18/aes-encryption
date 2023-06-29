import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.awt.event.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class FTPClient extends JFrame implements ActionListener {
	JButton connect, send, disconnect;
	JTextArea text;
	JPanel p1, p2, p3, p4;
	JTextField server, port, request, username, password, yourkey;
	Socket s;

	InputStream in = null;
	OutputStream out = null;

	FTPClient() {
		super("FILE TRANSFER WITH USER AUTHENTICATION FTP IMPLEMENTATION");
		setSize(1000, 800);
		p1 = new JPanel();
		p1.setBackground(Color.green);
		p2 = new JPanel();
		p2.setBackground(Color.orange);
		p1.setLayout(new FlowLayout());
		p2.setLayout(new FlowLayout());
		p3 = new JPanel();
		p3.setLayout(new FlowLayout());
		p3.setBackground(Color.yellow);

		p4 = new JPanel();
		p4.setLayout(new FlowLayout());
		p4.setBackground(Color.pink);

		text = new JTextArea(500, 500);
		server = new JTextField(15);
		server.setText("127.0.0.1");
		port = new JTextField(10);
		port.setText("2056");

		username = new JTextField(15);
		password = new JTextField(15);
		yourkey = new JTextField(15);

		request = new JTextField(20);

		JScrollBar hbar = new JScrollBar(JScrollBar.HORIZONTAL, 30, 20, 0, 500);
		JScrollBar vbar = new JScrollBar(JScrollBar.VERTICAL, 30, 40, 0, 500);

		Container c = getContentPane();

		c.add(hbar, BorderLayout.SOUTH);
		c.add(vbar, BorderLayout.EAST);
		p3.add(new JLabel("you can request 1. list 2. delete 3. rename 4. download 5. create 6. transfer"));

		c.add("Center", text);

		JPanel z = new JPanel();
		c.add("North", z);
		z.setLayout(new GridLayout(4, 2));
		z.add(p4);
		z.add(p1);
		z.add(p3);
		z.add(p2);

		p1.add(new JLabel("Server Name:"));
		p1.add(server);
		p1.add(new JLabel("Port no:"));
		p1.add(port);
		connect = new JButton("Connect");
		p1.add(connect);

		send = new JButton("send");
		disconnect = new JButton("disconnect");
		p3.add(disconnect);
		p4.add(new JLabel("username"));
		p4.add(username);
		p4.add(new JLabel("password"));
		p4.add(password);
		p4.add(new JLabel("secret key for security of password"));
		p4.add(yourkey);

		p2.add(new JLabel("Request"));
		p2.add(request);
		p2.add(send);
		connect.addActionListener(this);
		request.addActionListener(this);
		send.addActionListener(this);
		disconnect.addActionListener(this);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) {
		String ae = e.getActionCommand();
		String server1;
		int portno;
		if (ae.equalsIgnoreCase("connect")) {
			try {
				String s1 = username.getText();
				String s2 = password.getText();
				String k = yourkey.getText();
				String s3 = "mail2sarva2002@gmail.com";
				String s5 = "124003409@sastra.ac.in";
				String s6 = "sarva2002";
				String s4 = "2002";
				if ((s1.equals(s3) && s2.equals(s4)) || (s1.equals(s5) && s2.equals(s6))) {
					JOptionPane.showMessageDialog(null, "user is valid and connecting to server");
					server1 = server.getText();
					portno = Integer.parseInt(port.getText());
					s = new Socket(server1, portno);
					in = s.getInputStream();
					out = s.getOutputStream();
					text.append("Connected to server");

					final String secretKey = k;
					String originalString = s2;
					System.out.println("the original password is ........." + s2 + "........");
					String encryptedString = FTPClient.encrypt(originalString, secretKey);

					DataOutputStream dout = new DataOutputStream(s.getOutputStream());
					dout.writeUTF(encryptedString);
					DataOutputStream doutkey = new DataOutputStream(s.getOutputStream());
					doutkey.writeUTF(k);

				} else {
					JOptionPane.showMessageDialog(null, "user is not valid and not allowed to connect to server");
					System.out.println("user is not valid ");
					text.append("not allowed to connect");
				}
			} catch (Exception e1) {
				text.append(e1.toString());
			}
		} else if (ae.equalsIgnoreCase("disconnect")) {
			JOptionPane.showMessageDialog(null, "you are disconnecting from server");
			try {

				s.close();
				text.append("........disconnected you are no longer able to access files from server.........");
			} catch (Exception i) {
				System.out.println(i);
			}
		} else {
			try {

				String t = request.getText();
				System.out.println(t);
				send(t.getBytes());
				StringTokenizer st = new StringTokenizer(t);
				String cmd = st.nextToken();
				String cmd1 = st.nextToken();
				System.out.println("Res:" + cmd1.substring(cmd1.lastIndexOf("/") + 1));
				System.out.println(cmd);
				System.out.println(cmd1);

				if (cmd.equalsIgnoreCase("download")) {
					download(cmd1.substring(cmd1.lastIndexOf('\\') + 1));
				} else {
					byte b[] = receive();
				}
			} catch (Exception e1) {
				text.append(e1.toString());
			}
		}
	}

	private static SecretKeySpec secretKey;
	private static byte[] key;

	public static void setKey(String myKey) {
		MessageDigest sha = null;
		try {
			key = myKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static String encrypt(String strToEncrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	public static String decrypt(String strToDecrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}

	public static void main(String args[]) {
		FTPClient x = new FTPClient();

	}

	void send(byte b[]) {
		try {
			out.write(b);
			out.write((byte) '\n');
			out.flush();
		} catch (Exception e1) {
			text.append(e1.toString());
		}
	}

	byte[] receive() {
		try {
			byte b;
			String s;
			ByteArrayOutputStream r = new ByteArrayOutputStream();
			String usa = "";
			while ((b = (byte) in.read()) != -1) {

				r.write(b);
				s = String.valueOf((char) b);
				usa = usa + s;

				text.append(s);

			}

			return r.toByteArray();

		} catch (Exception e1) {
			text.append("Error reading");
			return null;
		}
	}

	void download(String file) {
		try {
			System.out.println(file);
			byte b[] = receive();
			DataInputStream din = new DataInputStream(s.getInputStream());

			String es = din.readUTF();
			// System.out.println(es);
			final String secretKey = "abcd";
			String filedecrypt = FTPClient.decrypt(es, secretKey);
			System.out.print("the actual data in file is " + filedecrypt);
			JOptionPane.showMessageDialog(null, "downloaded data from server is :..........." + filedecrypt);

			// BufferedWriter out = null;

			// try {
			// FileWriter fstream = new FileWriter("received.txt", true); //true tells to
			// append data.
			// out = new BufferedWriter(fstream);
			// out.write(filedecrypt);
			// }

			// catch(IOException e) {
			// System.err.println("Error: " + e.getMessage());
			// }
			PrintWriter pw = null;

			try {
				File rfile = new File("fubars.txt");
				FileWriter fw = new FileWriter(rfile, true);
				pw = new PrintWriter(fw);
				// pw.println("Fubars rule!");
				pw.println(filedecrypt);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (pw != null) {
					pw.close();
				}
			}
			if (new String(b).equalsIgnoreCase("error")) {
				text.append("Error");
			} else {
				FileOutputStream f = new FileOutputStream(file);

				f.write(b);
				f.close();

				text.append("Download has been done successfully");
				System.out.println("Downloaded succesfully");
			}
		} catch (Exception e1) {
			text.append("something unexpected error has occured");
		}

	}
}
