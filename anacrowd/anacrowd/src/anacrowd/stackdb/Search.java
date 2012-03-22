package anacrowd.stackdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.*; 

import anacrowd.cache.ResultCache; 
import anacrowd.metrics.coverage.WordDistributions;

import com.sun.rowset.CachedRowSetImpl;

public class Search 
{
	private static String driver = "com.mysql.jdbc.Driver";
	private static String dbName="stackdb";
	private static String connectionURL = "jdbc:mysql://localhost:3306/" + dbName;
	public static Connection conn = null;
	private static Statement s;
	public Connection getConnection() {return conn;}
	
	public ResultCache ResultCache;
	
	public void Init()
	{
		try 
		{
			Class.forName(driver);
			conn = DriverManager.getConnection(connectionURL, "root", "password");
			ResultCache = new ResultCache();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void CreateTemporaryTable(String tag, String name)
	{
		try
		{
			s = conn.createStatement();

			name = name.replace("<", "_").replace(">", "_");
			
			s.execute("CREATE TEMPORARY TABLE " + name + 
					" SELECT Id,OwnerUserId,CreationDate,PostTypeId,BodyAndTitle,Tags FROM POSTS WHERE tags like '%"+ tag + "%'");
			
		}
		catch( SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void CreateTemporaryRelatedTable(String tag, String name,String lookupName) {
		try
		{
			name = name.replace("<", "_").replace(">", "_");
			lookupName = lookupName.replace("<", "_").replace(">", "_");

			
			s.execute("CREATE INDEX temp_index_"+lookupName+" ON "+lookupName+"(Id);");
			s.execute("CREATE TEMPORARY TABLE " + name + 
					" SELECT P.Id,P.OwnerUserId,P.CreationDate,P.PostTypeId,P.ParentId,P.BodyAndTitle,P.tags FROM "+lookupName+" as Q, POSTS as P WHERE P.ParentId=Q.Id");
			s.execute("CREATE INDEX temp_index_"+name+" ON "+name+"(Id);");

		}
		catch( SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void Close()
	{
		try
		{
			conn.close();
		}
		catch (SQLException e) {}
	}

	public Integer GetParentId(String tempTable, int answerId)
	{
		try
		{
			tempTable = tempTable.replace("<", "_").replace(">","_");
			
			//Statement s = conn.createStatement();
			//ResultSet set = s.executeQuery("SELECT Id, ParentId FROM "+tempTable + " WHERE " + 
			//							   "Id = " + answerId);
			ResultSet set = s.executeQuery("SELECT Id, ParentId FROM POSTS WHERE " + 
					   "Id = " + answerId);

			set.next();
			return set.getInt("ParentId");
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Integer> GetIdsViaTags(String tempTable, String klassName)
	{
		tempTable = tempTable.replace("<", "_").replace(">","_");
		try
		{			
			CachedRowSet rowSet = new CachedRowSetImpl();  
			List<Integer> results = new ArrayList<Integer>();

			ResultSet set = ResultCache.CachedQueryExecution("tag",
					"SELECT Id FROM "+ tempTable + " WHERE " + 
					"tags like '%<"+ klassName +"%>'",
					s);
			rowSet.populate(set);
			while( rowSet.next() )
			{
				results.add( rowSet.getInt("Id"));
			}
			return results;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return null;
	}

	public List<Integer> GetIdsViaWordMatches(String tempTable, String klassName)
	{
		tempTable = tempTable.replace("<", "_").replace(">","_");
		try
		{	
			List<Integer> results = new ArrayList<Integer>();
			ResultSet set = ResultCache.CachedQueryExecution("word",
								"SELECT Id FROM "+tempTable + " WHERE " + 
								"BodyAndTitle REGEXP '[[:<:]]"+klassName+"[[:>:]]'",
								s);
			while( set.next() )
			{
				results.add( set.getInt("Id"));
			}
			return results;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public List<Integer> GetIdsViaCodeMatches(String tempTable, String klassName)
	{
		tempTable = tempTable.replace("<", "_").replace(">","_");
		try
		{	
			List<Integer> results = new ArrayList<Integer>();
			ResultSet set = ResultCache.CachedQueryExecution("code",
								"SELECT Id FROM "+tempTable + " WHERE " + 
								"BodyAndTitle REGEXP '<code>[^</]*[[:<:]]"+klassName+"[[:>:]][^</]*</code>'",
								s);
			while( set.next() )
			{
				results.add( set.getInt("Id"));
			}
			return results;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return null;
	}	
	
	public List<Integer> GetIdsViaLinkMatches(String tempTable, String klassName)
	{
		tempTable = tempTable.replace("<", "_").replace(">","_");
		try
		{	
			List<Integer> results = new ArrayList<Integer>();
			ResultSet set = ResultCache.CachedQueryExecution("links",
								"SELECT Id FROM "+tempTable + " WHERE " + 
								"BodyAndTitle REGEXP '<a[^>]*>[[:<:]]"+klassName+"[[:>:]][^</]*</a>'",
								s);
			while( set.next() )
			{
				results.add( set.getInt("Id"));
			}
			return results;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		return null;
	}	


	
	public List<Integer> GetIds(String tempTable, String klassName)
	{
		tempTable = tempTable.replace("<", "_").replace(">","_");
		try
		{			
			File cacheDir = new File("cachedir");
			if( !cacheDir.exists())
			{
				cacheDir.mkdir();
			}
			File cache = new File("cachedir/_"+tempTable+"___"+klassName);
			CachedRowSet rowSet = new CachedRowSetImpl();  
			List<Integer> results = new ArrayList<Integer>();

			// using tags for 1-word names
			if( new WordDistributions().WordLength(klassName) ==1 )
			{
				ResultSet set = ResultCache.CachedQueryExecution("tag",
						"SELECT Id FROM "+ tempTable + " WHERE " + 
						"tags like '%<"+ klassName +"%>'",
						s);
				rowSet.populate(set);
			}
			else if( !cache.exists()) // else below
			{
				ResultSet set = s.executeQuery("SELECT Id FROM "+tempTable + " WHERE " + 
											   "BodyAndTitle like '%"+ klassName +"%'"
				);
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
			while( rowSet.next() )
			{
				results.add( rowSet.getInt("Id"));
			}
			return results;
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
	
	public int QuestionsInBodyTitle(String tempTable, String klassName)
	{
		try
		{
			Statement s = conn.createStatement();
			ResultSet set = s.executeQuery("SELECT count(*) as number FROM "+tempTable + " WHERE " + 
										   "BodyAndTitle like '%"+ klassName +"%'"
			);
			set.next();
			return set.getInt("number");
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public int QuestionsInBodyTitle(String tempTable, String klassName, String methodName)
	{
		try
		{
			Statement s = conn.createStatement();
			ResultSet set = s.executeQuery("SELECT count(*) as number FROM "+tempTable + " WHERE " + 
					   					   "BodyAndTitle like '%"+ klassName +"%'" + 
										   "AND BodyAndTitle like '%"+ methodName +"%'"
			);
			set.next();
			return set.getInt("number");
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return -1;
	}

}
