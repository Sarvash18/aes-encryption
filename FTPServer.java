
import java.net.Socket;
import java.net.ServerSocket;
import java.util.StringTokenizer;
import java.io.FileInputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.DataInputStream;
import java.io.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
 
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


class FTPServer
{
    ServerSocket ss;
    Socket s;
    DataInputStream netin;
    PrintStream netout;
    
    public FTPServer() {
        this.ss = null;
        this.s = null;
        this.netin = null;
        this.netout = null;
        try {
            this.ss = new ServerSocket(2056);
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }
    
    public void sendErrorMessage() {
        try {
            this.netout.println("Bad command, check syntax");
            //this.netout.write(-1);
            this.netout.flush();
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }
    
    public void acceptConnection() {
        try {
            System.out.println("Waiting for connection from client ");
            this.s = this.ss.accept();
            System.out.println("Connected to client");
			DataInputStream din=new DataInputStream(s.getInputStream());
			
			String es=din.readUTF();
			DataInputStream dinkey=new DataInputStream(s.getInputStream());
			String k=dinkey.readUTF();
			
			System.out.println("password from client is in encrypted mode..............."+es);
			System.out.println("key generated from client is......."+k+" ");
			final String secretKey = k;
			String ds = FTPServer.decrypt(es, secretKey);
			System.out.println("actual password from client is..............******"+ds+"******");
			
			
						
            this.netin = new DataInputStream(this.s.getInputStream());
            this.netout = new PrintStream(this.s.getOutputStream());
        }
        catch (Exception x) {
            System.out.println(x);
			System.out.println("client disconnected and server closed");
        }
    }
    
    public String readRequest() {
        try {
            return this.netin.readLine();
        }
        catch (Exception x) {
            System.out.println(x);
            return null;
        }
    }
    
    public void transferFile(final String pathname) {
        try {
            final File file = new File(pathname);
            if (!file.exists()) {
                this.sendErrorMessage();
            }
            else {
                final FileInputStream fis = new FileInputStream(file);
                final byte[] array = new byte[(int)file.length()];
                fis.read(array, 0, array.length);
                this.netout.write(array, 0, array.length);
                //this.netout.write(-1);
                this.netout.flush();
            }
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }
    
    public void rename(final String pathname, final String pathname2) {
        try {
            final File file = new File(pathname);
            if (!file.exists()) {
                this.sendErrorMessage();
            }
            else {
                file.renameTo(new File(pathname2));
                this.netout.println("Successfully renamed");
                this.netout.write(-1);
                this.netout.flush();
            }
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }
    
    public void delete(final String pathname) {
        try {
            final File file = new File(pathname);
            if (!file.exists()) {
                this.sendErrorMessage();
            }
            else {
                if (file.delete()) {
                    this.netout.println("Successfully deleted");
                }
                else {
                    this.netout.println("not deleted");
                }
                this.netout.write(-1);
                this.netout.flush();
            }
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }
    
    public void create(final String pathname) {
        try {
            if (new File(pathname).mkdir()) {
                this.netout.println("Successfully directory created");
            }
            else {
                this.netout.println("directory not created");
            }
            this.netout.write(-1);
            this.netout.flush();
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }
    
    public void list(final String pathname) {
        try {
            final File file = new File(pathname);
            if (!file.isDirectory()) {
                this.sendErrorMessage();
            }
            else {
                final String[] list = file.list();
                String string = new String();
                for (int i = 0; i < list.length; ++i) {
                    string = string + list[i] + "\n";
                }
                this.netout.println(string);
                this.netout.write(-1);
                this.netout.flush();
            }
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }

  public void download(final String pathname)
 {	
		File check=new File(pathname);
		try
	  {
		if (!check.exists()) {
                this.sendErrorMessage();
            }
            else {
		FileInputStream fi=new FileInputStream(pathname);
		byte b;
		String string="";
		while((b=(byte)fi.read())!=-1)
		{
			string=string+(char)b;
		}
		System.out.println(string);
		final String secretKey = "abcd";
		String fileencrypt=FTPServer.encrypt(string,secretKey);
		System.out.println(fileencrypt);
		
		 this.netout.println(fileencrypt);
                		this.netout.write(-1);
                		this.netout.flush();
		DataOutputStream dout=new DataOutputStream(s.getOutputStream());
						dout.writeUTF(fileencrypt);				
}
	}
	catch(Exception e)
	{
		System.out.println(e);
	}
 }
    
    public void perform() {
        try {
            this.acceptConnection();
            while (true) {
                final StringTokenizer st= new StringTokenizer(this.readRequest());
                final String nextToken = st.nextToken();
	            System.out.println(nextToken);
                if (nextToken == null)                                           {   this.sendErrorMessage();	                                 }
      
                if (nextToken.equalsIgnoreCase("transfer"))     {  this.transferFile(st.nextToken());  }
                   
                if (nextToken.equalsIgnoreCase("list"))           {    this.list(st.nextToken());                 }
                  
                if (nextToken.equalsIgnoreCase("rename")) {  this.rename(st.nextToken(), st.nextToken());     }
                    
                if (nextToken.equalsIgnoreCase("create")) {    this.create(st.nextToken());  }
                              
                if (nextToken.equalsIgnoreCase("delete")) {  this.delete(st.nextToken()); }
                  
               if(nextToken.equalsIgnoreCase("download")) {   this.download(st.nextToken());     }
                              
                if (nextToken.equalsIgnoreCase("quit")) {  this.acceptConnection();  }
                
                   }
        }
        catch (Exception x) {
            System.out.println(x);
        }
    }
	private static SecretKeySpec secretKey;
    private static byte[] key;
 
    public static void setKey(String myKey) 
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); 
            secretKey = new SecretKeySpec(key, "AES");
        } 
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
	public static String decrypt(String strToDecrypt, String secret) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
	 public static String encrypt(String strToEncrypt, String secret) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
    public static void main(String ar[])
	{
	FTPServer f=new FTPServer();
	f.perform();
	}
}