package org.meta.modele;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import djondb.BSONObj;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 * @author Thomas LAVOCAT
 *	
 * This class correspond to a data on the hard drive. Pointed by a file.
 */
public class DataFile extends Data {

	private File file = null;
	
	/**
	 * needed for java Reflexion
	 */
	public DataFile(){
		super();
	}
	
	/**
	 * Instantiate a new Data -> use in case of creation
	 * @param hashCode
	 * @param file
	 */
	public DataFile(String hashCode, File file) {
		super(hashCode);
		this.file = file;
	}

	/**
	 * Instantiate a (not) new Data -> use in case of data base loading
	 * @param hashCode
	 * @param file
	 */
	public DataFile(String hashCode, File file, BSONObj json) {
		super(hashCode, json);
		this.file = file;
	}
	
	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	public BSONObj toJson() {
		BSONObj json = super.toJson();
		json.add("file", file.getAbsolutePath());
		return json;
	}
	
	@Override
	protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
		//write hash source
		fragment.put("_fileName", file.getName().getBytes());
		//Send the file, it will surrely be bigger than 65 536o
		
		long size	= file.length();
		long count	= size / 65536;  
		if(count<1)
			count=1;
		
		//set size
		fragment.put("_size", (size+"").getBytes());
		//set count
		fragment.put("_count", (count+"").getBytes());
		
		FileInputStream stream;
		try {
			stream = new FileInputStream(file);
			//write every hash results
			for (int i = 1; i <= count;i++) {
				int offset = (i-1)*65536;
				
				//size to read in the file
				int sizeToRead = -1;
				//if i < count, the size is 64ko
				if(i<count){
					sizeToRead = 65536;
				//if not but count was > 1, make the difference
				//original size - nb * 64ko
				}else if(count > 1){
					size = size-i*65536;
				}else{
					//else it's the orinial size
					sizeToRead = (int)size;
				}

				//the byte arry where to put the data
				byte[] bloc 	= new byte[sizeToRead];
				
				//read the bytes from the stream
				stream.read(bloc, offset, sizeToRead);
				
				//Make the hash from the bloc
				String blocHash = Model.hash(bloc);
				
				//write informations to the fragment
				//hash
				fragment.put("_"+i+"_blocHash", blocHash.getBytes());
				//bloc
				fragment.put("_"+i+"_contentPart", bloc);
			}
		} catch (FileNotFoundException e) {
			// TODO do something here
			e.printStackTrace();
		}catch (IOException e) {
			// TODO do something here
			e.printStackTrace();
		}
	}

	@Override
	protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
		//TODO file name
		File 	file     = new File(System.getProperty("java.io.tmpdir")+"/"+super.hash);
		
		try {
			if(file.createNewFile()){
				FileOutputStream fos = new FileOutputStream(file);
				long count = Long.parseLong(new String(fragment.get("_count")));
				fragment.remove("_size");
				fragment.remove("_count");
				
				for(int i=1; i<=count; i++){
					String hash = new String(fragment.get("_"+i+"_blocHash"));
					fragment.remove("_"+i+"_blocHash");
					byte[] bloc = fragment.get("_"+i+"_contentPart");
					if(Model.checkHash(hash, bloc)){
						fos.write(bloc);
					}else{
						//TODO write here the code needed to ask unCorrect blocs.
					}
				}		
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
