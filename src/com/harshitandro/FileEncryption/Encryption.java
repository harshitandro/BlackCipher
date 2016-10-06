/**

 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
	CipherInputStream cipherInput;
	CipherOutputStream cipherOutput;
	byte[] key;
	byte[] IV;
	byte[] blockSize = new byte[512];
	static final int ENCRYPTION_MODE =1;
	static final int DECRYPTION_MODE =0;
	SecureRandom secureRandGen ;
	SecretKeySpec secretKey;
	IvParameterSpec IVSpec ;
	Cipher cipher;
	FileHandler fileHandler;
	KeyGenerator keygen;
	
	public void setKeys(byte[] secretKey,byte[] IV){
		key=secretKey;
		this.IV=IV;
	}
	
	boolean secureDelete(File file){
		SecureRandom random = new SecureRandom();
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "rw");
			FileChannel channel = raf.getChannel();
			MappedByteBuffer buffer 
			= channel.map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
			// overwrite with zeros
			while (buffer.hasRemaining()) {
				buffer.put((byte) 0);
			}
			buffer.force();
			buffer.rewind();
			/*// overwrite with ones
			while (buffer.hasRemaining()) {
				buffer.put((byte) 0xFF);
			}
			buffer.force();
			buffer.rewind();
			// overwrite with random data; one byte at a time
			byte[] data = new byte[1];
			while (buffer.hasRemaining()) {
				random.nextBytes(data);
				buffer.put(data[0]);
			}
			buffer.force();*/
			file.delete(); 
			return true;
		}catch (Exception e) {
			return false;
		}
}

	
	public ArrayList<byte[]> doFinal(FileInputStream FileInput,FileOutputStream FileOutput,int Mode,byte[]...keydata) throws Exception{
		int i=0;
		if(Mode==ENCRYPTION_MODE){
			key=genRandomSecretKey();  
			secretKey = new SecretKeySpec(key,"AES");
			cipher= Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE,secretKey);
			this.IV=cipher.getIV();
			cipherOutput= new CipherOutputStream(FileOutput, cipher);
			while((i=FileInput.read(blockSize))!=-1){
				cipherOutput.write(blockSize,0,i);
			}
			cipherOutput.close();
			FileInput.close();
		}
		else 
			if(Mode==DECRYPTION_MODE){
				this.key=keydata[0];
				this.IV=keydata[1];
				secretKey = new SecretKeySpec(key,"AES");
				IVSpec= new IvParameterSpec(IV);
				cipher= Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, secretKey,IVSpec);
				cipherInput= new CipherInputStream(FileInput, cipher);
				while((i=cipherInput.read(blockSize))!=-1){
					FileOutput.write(blockSize,0,i);
				}
				FileOutput.close();
				cipherInput.close();
		}
		ArrayList<byte[]> keyInfo =new ArrayList<byte[]>(2);
		keyInfo.add(key);
		keyInfo.add(IV);
		return keyInfo;
	}
	
	public byte[] genRandomSecretKey() throws NoSuchAlgorithmException{
		keygen=KeyGenerator.getInstance("AES");
		keygen.init(256);
		return keygen.generateKey().getEncoded();
	}
	
}
