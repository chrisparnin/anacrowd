package importdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import sun.security.acl.OwnerImpl;

//import argo.jdom.JdomParser;
//import argo.jdom.JsonNode;
//import argo.jdom.JsonRootNode;



public class DBImport 
{
	private static String driver = "com.mysql.jdbc.Driver";
	//private static String driver = "org.apache.derby.jdbc.ClientDriver";
	private static String dbName="stackdb";
	//private static String connectionURL = "jdbc:derby://localhost:1527/" + dbName+ ";create=true";

	private static String creationURL = "jdbc:mysql://localhost:3306/";
	private static String connectionURL = "jdbc:mysql://localhost:3306/" + dbName;
	
	private static PreparedStatement createUsers;
	private static PreparedStatement createPosts;

	private static PreparedStatement createBulkPosts;
	
	private static Connection conn = null;
	private static Statement s;

	private static FileWriter _fw;
	
	public static void createDatabase() 
	{
		try 
		{
			s = conn.createStatement();
			try
			{
				s.executeUpdate("DROP TABLE USERS");
				s.executeUpdate("DROP TABLE POSTS");
			} 
			catch (Exception e){}

			String createUsers = "CREATE TABLE USERS (" +
				"Id INT NOT NULL," +
				"Reputation INT NOT NULL," +
				"CreationDate DATETIME NOT NULL," +
				"DisplayName VARCHAR(200) NOT NULL," +
				"EmailHash VARCHAR(200)," +
				"LastAccessDate DATETIME NOT NULL," + 
				"WebsiteUrl VARCHAR(200)," + 
				"Location VARCHAR(200)," + 
				"Age INT NOT NULL," + 
				"Views INT NOT NULL," + 
				"UpVotes INT NOT NULL," + 
				"DownVotes INT NOT NULL," + 
				"PRIMARY KEY (ID)" +
				")";
			s.execute(createUsers);
			
			String createPosts = "CREATE TABLE POSTS (" +
				"Id INT NOT NULL," +
				"PostTypeId INT NOT NULL," +
				"ParentID INT NOT NULL," +
				"AcceptedAnswerId INT," +
				"CreationDate DATETIME NOT NULL," +
				"Score INT," + 
		        "ViewCount INT," +
		        "Body TEXT," +
		        "OwnerUserId INT," + 
		        "LastEditorUserId INT," + 
		        "LastEditorDisplayName VARCHAR(200)," +  
		        "LastEditDate DATETIME," +
		        "LastActivityDate DATETIME NOT NULL," + 
		        "CommunityOwnedDate DATETIME," + 
		        "ClosedDate DATETIME," + 
		        "Title VARCHAR(500) NOT NULL," +
		        "Tags VARCHAR(200) NOT NULL," + 
		        "AnswerCount INT NOT NULL," +
		        "CommentCount INT NOT NULL," +
		        "FavoriteCount INT NOT NULL," +
		        "BodyAndTitle TEXT," +
				"PRIMARY KEY (ID)" +
				")";
			s.execute(createPosts);
			
			
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	public static class PostsHandler extends DefaultHandler
	{
		public int PostCount = 0;
		java.text.DateFormat _df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		public void startDocument()
		{
			try 
			{
				createPosts = conn.prepareStatement("INSERT INTO POSTS " +
						"(Id,PostTypeId,ParentID,AcceptedAnswerId,CreationDate,Score,ViewCount,Body,OwnerUserId," + 
						"LastEditorUserId,LastEditorDisplayName,LastEditDate,LastActivityDate,CommunityOwnedDate," +
						"ClosedDate,Title,Tags,AnswerCount,CommentCount,FavoriteCount,BodyAndTitle" +
						")" + 
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				
//				createBulkPosts = conn.prepareStatement( 
//						"LOAD DATA INFILE 'C:/Users/cp125/Documents/soPosts.txt' " +
//						"INTO TABLE POSTS " + 
//						"FIELDS TERMINATED BY '��' LINES TERMINATED BY '��'");
//				
//				
//				_fw = new FileWriter("C:/Users/cp125/Documents/soPosts.txt");
				
				s.execute("set unique_checks = 0");
				//s.execute("set autocommit = 0");
				s.execute("set foreign_key_checks = 0");
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			} 
			//catch (IOException e) {
			//	e.printStackTrace();
			//}
		}
		
		public void endDocument()
		{
			/*try
			{
				_fw.close();
				System.out.println("Bulk update started");
				s.execute("set unique_checks = 0");
				s.execute("set autocommit = 0");
				s.execute("set foreign_key_checks = 0");
				createBulkPosts.execute();
				System.out.println("Bulk update ended");
			}
			catch( IOException e)
			{
				e.printStackTrace();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}*/
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
		{
			try 
			{
				if( qName.equals("row"))
				{
					int id = Integer.parseInt(attributes.getValue("Id")); 
					
					if( attributes.getValue("PostTypeId").trim() == "" )
						return;
					
				    int postType = Integer.parseInt(attributes.getValue("PostTypeId"));
				          // - 1: Question
				          // - 2: Answer
				    String parentId = attributes.getValue("ParentId"); // (only present if PostTypeId is 2)
				    String acceptedId = attributes.getValue("AcceptedAnswerId"); // (only present if PostTypeId is 1)
				    String creationDate = attributes.getValue("CreationDate");
				    int score = Integer.parseInt(attributes.getValue("Score"));
				    String viewCount = attributes.getValue("ViewCount");
				    String body = attributes.getValue("Body");
				    String ownerUserId = attributes.getValue("OwnerUserId");
				    String lastEditorUserId = attributes.getValue("LastEditorUserId"); 
				    String lastEditorDisplayName = attributes.getValue("LastEditorDisplayName");
				    String lastEditDate = attributes.getValue("LastEditDate"); // 2009-03-05T22:28:34.823
				    String lastActivityDate = attributes.getValue("LastActivityDate");
				    String communityOwnedDate = attributes.getValue("CommunityOwnedDate"); 
				    String closedDate = attributes.getValue("ClosedDate"); 
				    String title = attributes.getValue("Title") == null ? "" : attributes.getValue("Title"); 
				    String tags = attributes.getValue("Tags") == null ? "" : attributes.getValue("Tags"); 
				    String answerCount = attributes.getValue("AnswerCount");
				    String commentCount = attributes.getValue("CommentCount");
				    String favoriteCount = attributes.getValue("FavoriteCount");
							    
					AddRow
					(
							id, 
							postType, 
							parentId, 
							acceptedId, 
							creationDate,
							score, 
							viewCount == null? "0": viewCount, 
							body, 
							ownerUserId,
							lastEditorUserId, 
							lastEditorDisplayName,
							lastEditDate, 
							lastActivityDate, 
							communityOwnedDate,
							closedDate, 
							title, 
							tags,
							answerCount, 
							commentCount,
							favoriteCount
					);
					
				    /*AddBulkRow
					(
							id, 
							postType, 
							parentId == null ? "-1" : parentId + "", 
							acceptedId == null ? "-1" : acceptedId +"", 
							creationDate,
							score, 
							viewCount.equals("") ? "0" : viewCount, 
							body, 
							ownerUserId == null ? "-1" : ownerUserId,
							lastEditorUserId == null ? "-1" : lastEditorUserId, 
							lastEditorDisplayName,
							lastEditDate == null ? null : lastEditDate, 
							lastActivityDate, 
							communityOwnedDate == null ? null : communityOwnedDate,
							closedDate == null ? null : closedDate, 
							title, 
							tags, 
							answerCount == null ? "0" : answerCount, 
							commentCount == null ? "0" : commentCount,
							favoriteCount == null ? "0" : favoriteCount);
*/
					if( PostCount % 10000 == 0 )
					{
						System.out.println(PostCount);
					}
					PostCount++;

				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			} 
			//catch (IOException e) {
			//	e.printStackTrace();
			//}
		}

		private void AddBulkRow(int id, int postType, String parentId,
				String acceptedId, String creationDate, int score,
				String viewCount, String body, String ownerUserId,
				String lastEditorUserId, String lastEditorDisplayName,
				String lastEditDate, String lastActivityDate,
				String communityOwnedDate, String closedDate, String title,
				String tags, String answerCount, String commentCount,
				String favoriteCount) throws SQLException, ParseException, IOException 
		{
			String[] row = new String[]{id + "",postType +"",parentId,acceptedId,creationDate, score +"",
					viewCount,body,ownerUserId,lastEditorUserId,lastEditorDisplayName,
					lastEditDate,lastActivityDate,communityOwnedDate,closedDate,title,tags,
					answerCount,commentCount,favoriteCount};
			ArrayList<String> list = new ArrayList<String>();
			for( String r : row )
			{
				list.add(r);
			}
			String rowStr = join(list,"��") + "��";
			_fw.write(rowStr);
		}
		
		 static String join(Collection<?> s, String delimiter) {
		     StringBuilder builder = new StringBuilder();
		     Iterator iter = s.iterator();
		     while (iter.hasNext()) {
		    	 Object val = iter.next();
		    	 if( val == null)
		    	 {
		    		 val = "\\N";
		    	 }
		         builder.append(val);
		         if (!iter.hasNext()) {
		           break;                  
		         }
		         builder.append(delimiter);
		     }
		     return builder.toString();
		 }
		
		private void AddRow(int id, int postType, String parentId,
				String acceptedId, String creationDate, int score,
				String viewCount, String body, String ownerUserId,
				String lastEditorUserId, String lastEditorDisplayName,
				String lastEditDate, String lastActivityDate,
				String communityOwnedDate, String closedDate, String title,
				String tags, String answerCount, String commentCount,
				String favoriteCount) throws SQLException, ParseException {
			createPosts.setInt(1, id);
			createPosts.setInt(2, postType);
			createPosts.setInt(3, parentId == null ? -1 : Integer.parseInt(parentId));		
			createPosts.setObject(4, acceptedId == null ? null : Integer.parseInt(acceptedId));		
			createPosts.setDate(5, new Date(_df.parse(creationDate).getTime()));
			createPosts.setInt(6, score);
			createPosts.setInt(7, viewCount.equals("") ? 0 : Integer.parseInt(viewCount));
			createPosts.setString(8, body);
			createPosts.setInt(9,ownerUserId == null ? -1 : Integer.parseInt(ownerUserId));
			createPosts.setInt(10, lastEditorUserId == null ? -1 : Integer.parseInt(lastEditorUserId));
			createPosts.setString(11, lastEditorDisplayName);
			createPosts.setDate(12, lastEditDate == null ? null : new Date(_df.parse(lastEditDate).getTime()));
			createPosts.setDate(13, new Date(_df.parse(lastActivityDate).getTime()));
			createPosts.setDate(14, communityOwnedDate == null ? null : new Date(_df.parse(communityOwnedDate).getTime()));
			createPosts.setDate(15, closedDate == null ? null : new Date(_df.parse(closedDate).getTime()));
			createPosts.setString(16, title);
			createPosts.setString(17, tags);
			createPosts.setInt(18, answerCount == null ? 0 : Integer.parseInt(answerCount));
			createPosts.setInt(19, commentCount == null ? 0 : Integer.parseInt(commentCount));
			createPosts.setInt(20,  favoriteCount == null ? 0 : Integer.parseInt(favoriteCount));
			createPosts.setString(21, title + " " + body);

			createPosts.executeUpdate();
		}
	}
	
	public static class UsersHandler extends DefaultHandler 
	{
		public int UserCount = 0;
		java.text.DateFormat _df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		public void startDocument()
		{
			try 
			{
				createUsers = conn.prepareStatement("INSERT INTO USERS " +
						"(Id,Reputation,CreationDate,DisplayName,EmailHash,LastAccessDate,WebsiteUrl,Location,Age,Views,UpVotes,DownVotes)" + 
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}

		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
		{
			try 
			{
				if( qName.equals("row"))
				{
					int id                = Integer.parseInt(attributes.getValue("Id"));
				    int rep               = Integer.parseInt(attributes.getValue("Reputation"));
				    String createDate     = attributes.getValue("CreationDate"); 
				    String displayName    = attributes.getValue("DisplayName"); 
				    String emailHash      = attributes.getValue("EmailHash");
				    String lastAccessDate = attributes.getValue("LastAccessDate");
				    String webUrl         = attributes.getValue("WebsiteUrl");
				    String location       = attributes.getValue("Location");
				    String age            = attributes.getValue("Age");
				    //String about          = attributes.getValue("AboutMe");
				    int views             = Integer.parseInt(attributes.getValue("Views"));
				    int upvotes           = Integer.parseInt(attributes.getValue("UpVotes"));
				    int downvotes         = Integer.parseInt(attributes.getValue("DownVotes"));
				    
					createUsers.setInt(1, id);
					createUsers.setInt(2, rep);
					createUsers.setDate(3,  new Date(_df.parse(createDate).getTime()));
					createUsers.setString(4, displayName);
					createUsers.setString(5, emailHash);
					createUsers.setDate(6, new Date(_df.parse(lastAccessDate).getTime()));
					createUsers.setString(7, webUrl);
					createUsers.setString(8, location);
					createUsers.setInt(9, age == null ? 0 : Integer.parseInt(age));
					createUsers.setInt(10, views);
					createUsers.setInt(11, upvotes);
					createUsers.setInt(12, downvotes);
										
					createUsers.executeUpdate();
				    
					if( UserCount % 10000 == 0 )
					{
						System.out.println(UserCount);
					}
					UserCount++;
				}
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public static void main (String [] args) 
	{
		try	
		{
			Class.forName(driver);
			//conn = DriverManager.getConnection(creationURL, "root", "password");	
			//s = conn.createStatement();
			//s.executeUpdate("DROP DATABASE IF EXISTS " + dbName );
			
			//s = conn.createStatement();
			//s.executeUpdate("CREATE DATABASE " + dbName);
			//conn.close();

			conn = DriverManager.getConnection(connectionURL, "stackuser", "bacon");	

			// deletes all database tables, and creates them new. Right now this is 
			// aimed towards a one time data import.
			createDatabase();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}

		if( args.length != 2 )
		{
			System.out.println("Args: posts.xml location, users.xml location");
			System.exit(0);
		}
		
		ParsePosts(args[0]);
		ParseUsers(args[1]);
		// "C:/Users/cp125/Desktop/repo/blogs/tools/stackoverflow/StackOverflow Dump/Stack Overflow Data Dump - Jun 2011/Content/062011 Stack Overflow/users.xml"
		
		// Create Index
		try
		{
			s.execute("create index POSTS_ParentId_index ON POSTS(ParentId)");
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}

	}

	private static void ParseUsers(String path) 
	{
		try 
		{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = parser.getXMLReader();
			UsersHandler users = new UsersHandler();
			xr.setContentHandler(users);
			
			xr.parse(path);
			
			System.out.println("Users:" + users.UserCount);
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void ParsePosts(String path) 
	{
		try 
		{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = parser.getXMLReader();
			PostsHandler posts = new PostsHandler();
			xr.setContentHandler(posts);
			
			xr.parse(path);
			
			System.out.println("Posts:" + posts.PostCount);
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
