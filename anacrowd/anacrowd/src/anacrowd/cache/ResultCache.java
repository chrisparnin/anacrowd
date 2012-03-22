package anacrowd.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;

public class ResultCache 
{	
	
	public boolean Exists()
	{
		String cacheDirPath = "cachedir/";
		File cacheDir = new File(cacheDirPath);
		return cacheDir.exists();
	}
	
	private String EncodeQuery(String query)
	{
		try 
		{
	        byte[] bytesOfMessage = query.getBytes("UTF-8");
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] thedigest = md.digest(bytesOfMessage);
	        StringBuilder builder = new StringBuilder();
	        for( byte b : thedigest)
	        	builder.append( String.format("%x",  b) );
	        return builder.toString();
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
		
	public CachedRowSet CachedQueryExecution(String searchMethod, String query, Statement s)
	{
		try
		{			
			String cacheDirPath = "cachedir/" + searchMethod + "/";
			File cacheDir = new File(cacheDirPath);
			if( !cacheDir.exists())
			{
				cacheDir.mkdirs();
			}
			
			String encodedFilePath = EncodeQuery(query);
			if( encodedFilePath == null )
				return null;

			String fileName = cacheDirPath + encodedFilePath;
			
			File cache = new File(fileName);
			CachedRowSet rowSet = new CachedRowSetImpl();  
			
			if( !cache.exists()) // else below
			{
				ResultSet set = s.executeQuery(query);
				rowSet.populate(set);

				// http://java.sun.com/developer/technicalArticles/Programming/serialization/
				// serialize
				FileOutputStream fos = new FileOutputStream(cache.getAbsolutePath());
				ObjectOutputStream out = new ObjectOutputStream(fos);
				out.writeObject(rowSet);
				out.close();
			}
			else
			{
				// deserialize
				FileInputStream fos = new FileInputStream(cache.getAbsolutePath());
				ObjectInputStream in = new ObjectInputStream(fos);
				rowSet = (CachedRowSet)in.readObject();
				in.close();
			}
			return rowSet;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}
