package com.videogo.ui.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ZipUtil {
	private static final int BUFFER = 1024; 
	private static final String BASE_DIR = "";
	private static final String PATH = "/";
	
	private static final String SIGN_PATH_NAME = "META-INF";
	private static final String UPDATE_PATH_NAME = "\\res\\raw\\channel";
	private static final String SOURCE_PATH_NAME = "\\source\\";
	private static final String TARGET_PATH_NAME = "\\target\\";
	private static final String RESULT_PATH_NAME = "\\result\\";
	private static final String JDK_BIN_PATH = "C:\\Program Files\\Java\\jdk1.6.0_26\\bin";
	private static final String SECRET_KEY_PATH = "F:\\document\\APK\\";
	private static final String SECRET_KEY_NAME = "sdk.keystore";
	
	@SuppressWarnings("rawtypes")
	public static void unZip(String fileName, String filePath) throws Exception{  
       ZipFile zipFile = new ZipFile(fileName);
       Enumeration emu = zipFile.entries();
        
       while(emu.hasMoreElements()){  
            ZipEntry entry = (ZipEntry) emu.nextElement();  
            if (entry.isDirectory()){  
                new File(filePath+entry.getName()).mkdirs();  
                continue;  
            }  
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));  
             
            File file = new File(filePath + entry.getName());  
            File parent = file.getParentFile();  
            if(parent != null && (!parent.exists())){  
                parent.mkdirs();  
            }  
            FileOutputStream fos = new FileOutputStream(file);  
            BufferedOutputStream bos = new BufferedOutputStream(fos,BUFFER);  
      
            byte [] buf = new byte[BUFFER];  
            int len = 0;  
            while((len=bis.read(buf,0,BUFFER))!=-1){  
                fos.write(buf,0,len);  
            }  
            bos.flush();  
            bos.close();  
            bis.close();  
           }  
           zipFile.close();  
    }  
    
    public static void unZipGetLib(String fileName, String filePath) throws Exception{  
        ZipFile zipFile = new ZipFile(fileName);
        Enumeration emu = zipFile.entries();
         
        while(emu.hasMoreElements()){  
             ZipEntry entry = (ZipEntry) emu.nextElement();  
             if (entry.isDirectory()){   
                 continue;  
             }  
             String entryName = entry.getName();
             Pattern p = Pattern.compile(".*(SO|so)$"); //??????so???????????????  
             Matcher m = p.matcher(entryName);
             if(!m.matches()) {
                 continue; 
             }
             BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));  
              
             File file = new File(filePath + "/" + entry.getName());  
             File parent = file.getParentFile();  
             if(parent != null && (!parent.exists())){  
                 parent.mkdirs();  
             }  
             FileOutputStream fos = new FileOutputStream(file);  
             BufferedOutputStream bos = new BufferedOutputStream(fos,BUFFER);  
       
             byte [] buf = new byte[BUFFER];  
             int len = 0;  
             while((len=bis.read(buf,0,BUFFER))!=-1){  
                 fos.write(buf,0,len);  
             }  
             bos.flush();  
             bos.close();  
             bis.close();  
            }  
            zipFile.close();  
     }  
    
	public static void compress(String srcFile, String destPath) throws Exception {
		compress(new File(srcFile), new File(destPath));
	}
	
	public static void compress(File srcFile, File destFile) throws Exception {
		// ??????????????????CRC32??????
		CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
				destFile), new CRC32());

		ZipOutputStream zos = new ZipOutputStream(cos);
		compress(srcFile, zos, BASE_DIR);

		zos.flush();
		zos.close();
	}
	
	private static void compress(File srcFile, ZipOutputStream zos,
			String basePath) throws Exception {
		if (srcFile.isDirectory()) {
			compressDir(srcFile, zos, basePath);
		} else {
			compressFile(srcFile, zos, basePath);
		}
	}
	
	private static void compressDir(File dir, ZipOutputStream zos,
			String basePath) throws Exception {
		File[] files = dir.listFiles();
		// ???????????????
		if (files.length < 1) {
			ZipEntry entry = new ZipEntry(basePath + dir.getName() + PATH);

			zos.putNextEntry(entry);
			zos.closeEntry();
		}
		
		String dirName = "";
		String path = "";
		for (File file : files) {
			//?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
			if(basePath!=null && !"".equals(basePath)){
				dirName=dir.getName(); 
			}
			path = basePath + dirName + PATH;
			// ????????????
			compress(file, zos, path);
		}
	}

	private static void compressFile(File file, ZipOutputStream zos, String dir)
			throws Exception {
		/**
		 * ???????????????????????????
		 * 
		 * <pre>
		 * ???????????????????????????????????????????????????????????????????????????
		 * ?????????WinRAR?????????????????????????????????????????????
		 * </pre>
		 */
		if("/".equals(dir))dir="";
		else if(dir.startsWith("/"))dir=dir.substring(1,dir.length());
		
		ZipEntry entry = new ZipEntry(dir + file.getName());
		zos.putNextEntry(entry);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		int count;
		byte data[] = new byte[BUFFER];
		while ((count = bis.read(data, 0, BUFFER)) != -1) {
			zos.write(data, 0, count);
		}
		bis.close();

		zos.closeEntry();
	}
	
	public static void main(String[] args)throws Exception{
		StringBuffer buffer = new StringBuffer();
		BufferedReader br =null;
		OutputStreamWriter osw =null;
		String srcPath = "F:\\document\\APK\\new\\iGouShop.apk";
		String channelCode = "channel_id=LD20120926";
		
		File srcFile = new File(srcPath);
		String parentPath = srcFile.getParent();	//???????????????
		String fileName = srcFile.getName();		//???????????????
		String prefixName = fileName.substring(0, fileName.lastIndexOf("."));
		//???????????????????????????
		String sourcePath = buffer.append(parentPath).append(SOURCE_PATH_NAME).
								append(prefixName).append("\\").toString();
		
		//------??????
		unZip(srcPath, sourcePath);
		
		//------??????????????????????????????
		String signPathName = sourcePath+SIGN_PATH_NAME;
		File signFile = new File(signPathName);
		if(signFile.exists()){
			File sonFiles[] = signFile.listFiles();
			if(sonFiles!=null && sonFiles.length>0){
				//????????????????????????????????????
				for(File f : sonFiles){
					f.delete();
				}
			}
			signFile.delete();
		}
		
		//------???????????????
		buffer.setLength(0);
		String path = buffer.append(parentPath).append(SOURCE_PATH_NAME)
				.append(prefixName).append(UPDATE_PATH_NAME).toString();
		br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		while((br.readLine())!=null)
		{
			osw = new OutputStreamWriter(new FileOutputStream(path));  
			osw.write(channelCode,0,channelCode.length());  
			osw.flush();  
		}
		
		//------??????
		String targetPath = parentPath+TARGET_PATH_NAME;
		//?????????????????????
		File targetFile = new File(targetPath);
		if(!targetFile.exists()){
			targetFile.mkdir();
		}
		compress(parentPath+SOURCE_PATH_NAME+prefixName,targetPath+fileName);
		
		//------??????
		File ff =new File(JDK_BIN_PATH);
		String resultPath = parentPath+RESULT_PATH_NAME;
		//?????????????????????
		File resultFile = new File(resultPath);
		if(!resultFile.exists()){
			resultFile.mkdir();
		}
		
		//??????????????????
		buffer.setLength(0);
		buffer.append("cmd.exe /c jarsigner -keystore ")
		.append(SECRET_KEY_PATH).append(SECRET_KEY_NAME)
		.append(" -storepass winadsdk -signedjar ")
		.append(resultPath).append(fileName).append(" ")	//??????????????????????????????
		.append(targetPath).append(fileName).append(" ")	//??????????????????????????????
		.append(SECRET_KEY_NAME);
		//??????????????????JDK????????????????????????
		Process process = Runtime.getRuntime().exec(buffer.toString(),null,ff);
		if(process.waitFor()!=0)System.out.println("???????????????????????????");
	}
}
