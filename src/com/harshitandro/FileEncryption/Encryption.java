/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	
	public ArrayList<byte[]> doFinal(FileInputStream FileInput,FileOutputStream FileOutput,int Mode,byte[]...keydata) throws Exception{
		int i;
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
		}
		else 
			if(Mode==DECRYPTION_MODE){
				this.key=keydata[0];
				this.IV=keydata[1];
				secretKey = new SecretKeySpec(key, "AES");
				IVSpec= new IvParameterSpec(IV);
				cipher= Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, secretKey,IVSpec);
				cipherInput= new CipherInputStream(FileInput, cipher);
				while((i=cipherInput.read(blockSize))!=-1){
					FileOutput.write(blockSize,0,i);
				}
		}
		ArrayList<byte[]> keyInfo =new ArrayList<byte[]>(2);
		keyInfo.add(key);
		keyInfo.add(IV);
		FileInput.close();
		return keyInfo;
	}
	
	public byte[] genRandomSecretKey() throws NoSuchAlgorithmException{
		keygen=KeyGenerator.getInstance("AES");
		keygen.init(256);
		return keygen.generateKey().getEncoded();
	}
	
}
