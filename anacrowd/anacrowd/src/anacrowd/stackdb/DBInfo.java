package anacrowd.stackdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DBInfo 
{
	private static String driver = "com.mysql.jdbc.Driver";
	private static String dbName="stackdb";
	private static String connectionURL = "jdbc:mysql://localhost:3306/" + dbName;
	private static Connection conn = null;

	Statement _commonStatement;

	public void Init()
	{
		try 
		{
			Class.forName(driver);
			conn = DriverManager.getConnection(connectionURL, "root", "password");
			_commonStatement = conn.createStatement();
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
	
	
	
	public static class UserInfo
	{
		public int Id;
		public String Name;
		public int Reputation;
		public int Age;
		public Date Creation;
		public Date LastActivity;
		public int Views;
		public int UpVotes;
		public int Downvotes;
	}
	
	public static class PostInfo
	{
		public int Id;
		public int PostTypeId;
		public int ParentId;
		public int OwnerUserId;
		public Date CreationDate;
		public Date LastActivityDate;
		public int ViewCount;
		public String Tags;
		public String Title;
		//private String Body;
		private byte[] compressedBody;
		public int Score;
		public int FavoriteCount;
		public int AnswerCount;
		public Integer AcceptedAnswerId;
		
		public String getBody()
		{
			try
			{
				return decompress(compressedBody);
			}
			catch (IOException ex) 
			{
				ex.printStackTrace();
				return null;
			}
		}
		
		public void setBody(String body)
		{
			try 
			{
				compressedBody = compress(body);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		// http://stackoverflow.com/questions/6717165/how-can-i-compress-a-string-using-gzipoutputstream-and-vice-versa
		public static byte[] compress(String string) throws IOException 
		{
		    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
		    GZIPOutputStream gos = new GZIPOutputStream(os);
		    gos.write(string.getBytes());
		    gos.close();
		    byte[] compressed = os.toByteArray();
		    os.close();
		    return compressed;
		}
		
		public static String decompress(byte[] compressed) throws IOException 
		{
		    final int BUFFER_SIZE = 64;
		    ByteArrayInputStream is = new ByteArrayInputStream(compressed);
		    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
		    StringBuilder string = new StringBuilder();
		    byte[] data = new byte[BUFFER_SIZE];
		    int bytesRead;
		    while ((bytesRead = gis.read(data)) != -1) {
		        string.append(new String(data, 0, bytesRead));
		    }
		    gis.close();
		    is.close();
		    return string.toString();
		}
	}

	public List<Integer> GetCurators(Integer id)
	{
		try
		{
			ResultSet set = _commonStatement.executeQuery("SELECT UserId FROM VOTES WHERE PostId ="+ id);
			List<Integer> list = new ArrayList<Integer>();
			if( set.next() )
			{
				list.add(set.getInt("UserId"));
			}
			return list;
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	
	public Integer GetBountyAmount(Integer id)
	{
		try
		{
			ResultSet set = _commonStatement.executeQuery("SELECT BountyAmount FROM VOTES WHERE PostId ="+ id + " AND VoteTypeId = 9");
			if( set.next() )
			{
				return set.getInt("BountyAmount");
			}
			return null;
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		return 0;
	}
	
	public List<PostInfo> Tags(String tag)
	{
		try
		{
			Statement s = conn.createStatement();
			ResultSet set = s.executeQuery("SELECT Id FROM POSTS WHERE Tags like '%"+ tag + "%'");
			set.next();

			List<Integer> ids = new ArrayList<Integer>();
			List<PostInfo> posts = new ArrayList<PostInfo>();
			while( set.next() )
			{
				ids.add(set.getInt("Id"));
			}
			for( int id : ids )
			{
				posts.add(GetPostDetail(id));
			}
			return posts;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public PostInfo GetPostDetail(int id)
	{
		try
		{
			Statement s = conn.createStatement();
			ResultSet set = s.executeQuery("SELECT Id,PostTypeId,OwnerUserId,CreationDate,Body,Score,AcceptedAnswerId,AnswerCount,FavoriteCount,LastActivityDate,ViewCount,Tags,Title FROM POSTS WHERE Id = "+ id);
			set.next();
	
			PostInfo info = new PostInfo();
			info.Id = set.getInt("Id");
			info.PostTypeId = set.getInt("PostTypeId");
			info.OwnerUserId = set.getInt("OwnerUserId");
			info.CreationDate = set.getDate("CreationDate");
			info.LastActivityDate = set.getDate("LastActivityDate");
			info.ViewCount = set.getInt("ViewCount");
			info.Tags = set.getString("Tags");
			info.Title = set.getString("Title");
			info.setBody(set.getString("Body"));
			info.Score = set.getInt("Score");
			info.FavoriteCount = set.getInt("FavoriteCount");
			info.AnswerCount = set.getInt("AnswerCount");
			info.AcceptedAnswerId = (Integer)set.getObject("AcceptedAnswerId");
			return info;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	public List<UserInfo> Users()
	{
		try
		{
			List<UserInfo> results = new ArrayList<UserInfo>();
			Statement s = conn.createStatement();
			ResultSet set = s.executeQuery("SELECT Id,DisplayName,Reputation,Age,CreationDate,LastAccessDate,Views,UpVotes,DownVotes FROM USERS");
			while( set.next() )
			{
				UserInfo info = new UserInfo();
				info.Id = set.getInt("Id");
				info.Name = set.getString("DisplayName");
				info.Reputation = set.getInt("Reputation");
				info.Age = set.getInt("Age");
				info.Creation = set.getDate("CreationDate");
				info.LastActivity = set.getDate("LastAccessDate");
				info.Views = set.getInt("Views");
				info.UpVotes = set.getInt("UpVotes");
				info.Downvotes = set.getInt("DownVotes");
				results.add(info);
			}
			return results;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return null;
	}

	public List<PostInfo> Questions(String tag)
	{
		try
		{
			List<PostInfo> results = new ArrayList<PostInfo>();
			Statement s = conn.createStatement();
			ResultSet set = s.executeQuery("SELECT Id,PostTypeId,OwnerUserId,CreationDate,LastActivityDate,ViewCount,Tags,Title FROM POSTS WHERE Tags like '%"+ tag + "%'");
			while( set.next() )
			{
				PostInfo info = new PostInfo();
				info.Id = set.getInt("Id");
				info.PostTypeId = set.getInt("PostTypeId");
				info.OwnerUserId = set.getInt("OwnerUserId");
				info.CreationDate = set.getDate("CreationDate");
				info.LastActivityDate = set.getDate("LastActivityDate");
				info.ViewCount = set.getInt("ViewCount");
				info.Tags = set.getString("Tags");
				info.Title = set.getString("Title");
				results.add(info);
			}
			return results;
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public List<PostInfo> Answers(Integer parentId)
	{
		List<PostInfo> results = new ArrayList<PostInfo>();

		try
		{
			Statement s = conn.createStatement();
			ResultSet set = s.executeQuery("SELECT Id,PostTypeId,ParentId,OwnerUserId,CreationDate,LastActivityDate,Body,ViewCount,Title FROM POSTS WHERE ParentId = " + parentId);
			while( set.next() )
			{
				PostInfo info = new PostInfo();
				info.Id = set.getInt("Id");
				info.PostTypeId = set.getInt("PostTypeId");
				info.ParentId = set.getInt("ParentId");
				info.OwnerUserId = set.getInt("OwnerUserId");
				info.CreationDate = set.getDate("CreationDate");
				info.LastActivityDate = set.getDate("LastActivityDate");
				info.setBody(set.getString("Body"));
				//info.ViewCount = set.getInt("ViewCount");
				//info.Tags = set.getString("Tags");
				//info.Title = set.getString("Title");
				results.add(info);
			}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return results;
	}

	
	public void Close()
	{
		try
		{
			conn.close();
		}
		catch (SQLException e) {}
	}

}
