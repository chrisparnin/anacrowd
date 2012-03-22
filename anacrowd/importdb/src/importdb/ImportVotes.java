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

public class ImportVotes 
{
	private static String driver = "com.mysql.jdbc.Driver";
	private static String dbName="stackdb";
	private static String connectionURL = "jdbc:mysql://localhost:3306/" + dbName;

	private static PreparedStatement createVotes;
	private static Connection conn = null;
	private static Statement s;

	public static void CreateTable()
	{
		try
		{
			s = conn.createStatement();
			String createVotesSQL = "CREATE TABLE VOTES (" +
					"Id INT NOT NULL," +
					"PostId INT NOT NULL," +
					"VoteTypeId INT NOT NULL," +
					"CreationDate DATETIME NOT NULL," + 
					"UserId INT NULL," + 
					"BountyAmount INT NULL," + 
					"PRIMARY KEY (ID)" +
					")";
			s.execute(createVotesSQL);
		}
		catch( SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static class VotesHandler extends DefaultHandler
	{
		public int VoteCount = 0;
		java.text.DateFormat _df = new java.text.SimpleDateFormat("yyyy-MM-dd");

		public void startDocument()
		{
			try 
			{
				createVotes = conn.prepareStatement("INSERT INTO VOTES " +
						"(Id,PostId,VoteTypeId,CreationDate,UserId,BountyAmount)" + 
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
				    int voteTypeId = Integer.parseInt(attributes.getValue("VoteTypeId"));
				    String date = attributes.getValue("CreationDate");
				    String userId = attributes.getValue("UserId");
				    String bountyAmount = attributes.getValue("BountyAmount");
				    AddRow
					(
						id,
						postId,
						voteTypeId,
						date,
						userId,
						bountyAmount
					);
					
					if( VoteCount % 10000 == 0 )
					{
						System.out.println(VoteCount);
					}
					VoteCount++;

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
				int id, int postId, int voteTypeId,
				String date, String userId, String bountyAmount
				) throws SQLException, ParseException 
		{
			createVotes.setInt(1, id);
			createVotes.setInt(2, postId);
			createVotes.setInt(3, voteTypeId);
			createVotes.setDate(4, new Date(_df.parse(date).getTime()));
			createVotes.setObject(5, userId == null ? null : Integer.parseInt(userId));
			createVotes.setObject(6, bountyAmount == null ? null : Integer.parseInt(bountyAmount));
			createVotes.executeUpdate();
		}
	}

	public static void main (String [] args) 
	{
		try	
		{
			Class.forName(driver);
			conn = DriverManager.getConnection(connectionURL, "root", "password");
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
			System.out.println("Args: votes.xml location");
			System.exit(0);
		}
		System.out.println("Parsing: " + args[0]);
		
		
		ParseVotes(args[0]);
		
		// Create Index
		try
		{
			s.execute("create index VOTES_PostId_index ON VOTES(PostId)");
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	private static void ParseVotes(String path) 
	{
		try 
		{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = parser.getXMLReader();
			VotesHandler users = new VotesHandler();
			xr.setContentHandler(users);
			
			xr.parse(path);
			
			System.out.println("Votes:" + users.VoteCount);
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
