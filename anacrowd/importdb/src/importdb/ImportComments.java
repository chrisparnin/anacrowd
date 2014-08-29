package importdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ImportComments 
{
	private static String driver = "com.mysql.jdbc.Driver";
	private static String dbName="stackdb";
	private static String connectionURL = "jdbc:mysql://localhost:3306/" + dbName;

	private static PreparedStatement createComments;
	private static Connection conn = null;
	private static Statement s;

	public static void CreateTable()
	{
		try
		{
			s = conn.createStatement();
			String createCommentsSQL = "CREATE TABLE COMMENTS (" +
					"Id INT NOT NULL," +
					"PostId INT NOT NULL," +
					"Score INT NOT NULL," +
					"Comment TEXT NULL," + 
					"CreationDate DATETIME NOT NULL," + 
					"UserId INT NULL," + 
					"PRIMARY KEY (ID)" +
					")";
			s.execute(createCommentsSQL);
		}
		catch( SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static class CommentsHandler extends DefaultHandler
	{
		public int CommentsCount = 0;
		java.text.DateFormat _df = new java.text.SimpleDateFormat("yyyy-MM-dd");

		public void startDocument()
		{
			try 
			{
				createComments = conn.prepareStatement("INSERT INTO COMMENTS " +
						"(Id,PostId,Score,Comment,CreationDate,UserId)" + 
						"VALUES (?, ?, ?, ?, ?, ?)");
				
				s.execute("set unique_checks = 0");
				//s.execute("set autocommit = 0");
				s.execute("set foreign_key_checks = 0");
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			} 
		}
		
		public void endDocument()
		{
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
		{
			try 
			{
				if( qName.equals("row"))
				{
				    //Id,PostId,VoteTypeId,CreationDate,UserId,BountyAmount		    
					int id = Integer.parseInt(attributes.getValue("Id"));  
				    int postId = Integer.parseInt(attributes.getValue("PostId"));
				    int score = Integer.parseInt(attributes.getValue("Score"));
				    String comment = attributes.getValue("Text");
				    String date = attributes.getValue("CreationDate");
				    String userId = attributes.getValue("UserId");
				    AddRow
					(
						id,
						postId,
						score,
						comment,
						date,
						userId
					);
					
					if( CommentsCount % 10000 == 0 )
					{
						System.out.println(CommentsCount);
					}
					CommentsCount++;

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

		
		private void AddRow(
				int id, int postId, int score,
				String text, String date, String userId
				) throws SQLException, ParseException 
		{
			createComments.setInt(1, id);
			createComments.setInt(2, postId);
			createComments.setInt(3, score);
			createComments.setString(4, text);
			createComments.setDate(5, new Date(_df.parse(date).getTime()));
			createComments.setObject(6, userId == null ? null : Integer.parseInt(userId));
			createComments.executeUpdate();
		}
	}

	public static void main (String [] args) 
	{
		try	
		{
			Class.forName(driver);
			conn = DriverManager.getConnection(connectionURL, "stackuser", "bacon");
			CreateTable();

		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}

		if( args.length != 1 )
		{
			System.out.println("Args: comments.xml location");
			System.exit(0);
		}
		System.out.println("Parsing: " + args[0]);
		
		
		ParseComments(args[0]);
		
		// Create Index
		try
		{
			s.execute("create index COMMENTS_PostId_index ON COMMENTS(PostId)");
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	private static void ParseComments(String path) 
	{
		try 
		{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = parser.getXMLReader();
			CommentsHandler comments = new CommentsHandler();
			xr.setContentHandler(comments);
			
			xr.parse(path);
			
			System.out.println("Comments:" + comments.CommentsCount);
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
