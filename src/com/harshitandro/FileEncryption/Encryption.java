/**
 * @author : Harshit Singh Lodha (harshitandro@gmail.com)
 */
package com.harshitandro.FileEncryption;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
	byte[] salt = new byte[16];
	byte[] blockSize = new byte[512];
	final int ENCRYPTION_MODE =1;
	final int DECRYPTION_MODE =2;
	SecureRandom secureRandGen ;
	SecretKeySpec secretKey;
	IvParameterSpec IVSpec ;
	Cipher cipher;
	FileHandler fileHandler;
	KeyGenerator keygen;
	
	public void setKeys(byte[] secretKey,byte[] Iv){
		key=secretKey;
		IV=Iv;
	}
	
	public void doFinal(FileInputStream FileInput,FileOutputStream FileOutput,int Mode) throws Exception{
		int i;
		if(Mode==ENCRYPTION_MODE){
			secretKey = new SecretKeySpec(key,"AES");
			cipher= Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE,secretKey);
			cipherOutput= new CipherOutputStream(FileOutput, cipher);
			while((i=FileInput.read(blockSize))!=-1){
				cipherOutput.write(blockSize,0,i);
			}
		}
		else if(Mode==DECRYPTION_MODE){
			secretKey = new SecretKeySpec(key, "AES");
			IVSpec= new IvParameterSpec(IV);
			cipher= Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey,IVSpec);
			cipherInput= new CipherInputStream(FileInput, cipher);
			while((i=cipherInput.read(blockSize))!=-1){
				FileOutput.write(blockSize,0,i);
			}
		}
		cipherInput.close();
		cipherOutput.close();
		FileInput.close();
		FileOutput.close();
	}
	
	public byte[] genRandomSecretKey() throws NoSuchAlgorithmException{
		keygen=KeyGenerator.getInstance("AES");
		keygen.init(256);
		key=keygen.generateKey().getEncoded();
		return key;
	}
	
}