package importdb.checkout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class CheckoutTaggedPostsAndComments 
{
	private static String driver = "com.mysql.jdbc.Driver";
	private static String dbName="stackdb";
	private static String connectionURL = "jdbc:mysql://localhost:3306/" + dbName;
	private static Connection conn = null;

	Statement _commonStatement;

	public void Init(String user, String pass)
	{
		try 
		{
			Class.forName(driver);
			conn = DriverManager.getConnection(connectionURL, user, pass);
			_commonStatement = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
			_commonStatement.setFetchSize(Integer.MIN_VALUE);
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
		public String Body;
		//private byte[] compressedBody;
		public int Score;
		public int FavoriteCount;
		public int AnswerCount;
		public Integer AcceptedAnswerId;
		
		public List<CommentInfo> Comments = new ArrayList<CommentInfo>();
	}
	
	// Question/Answer
	public static class Question
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
		public String Body;
		public int Score;
		public int FavoriteCount;
		public int AnswerCount;
		public Integer AcceptedAnswerId;

		public List<Answer> Answers = new ArrayList<Answer>();
		public List<CommentInfo> Comments = new ArrayList<CommentInfo>();		
	}
	
	public static class Answer
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
		public String Body;
		public int Score;
		public int FavoriteCount;
		public int AnswerCount;
		public Integer AcceptedAnswerId;
		
		public List<CommentInfo> Comments = new ArrayList<CommentInfo>();		
	}
	
	public static class CommentInfo
	{
		public int Id;
		public Date CreationDate;
		public int UserId;
		public String Comment;
		public int Score;
	}
	
	public List<Question> Query(String[] searchTags)
	{
		List<Question> list = new ArrayList<Question>();
		try
		{
			String query = "SELECT * FROM POSTS WHERE " + BuildLikeClauses(searchTags);
			System.out.println(query);
			ResultSet set = _commonStatement.executeQuery(query);
			while( set.next() )
			{
				Question post = PopulateQuestions(set);
				list.add(post);
			}
			set.close();
			
			int count= 0;
			for( Question post : list )
			{
				post.Comments = PopulateComments(post.Id);

				PopulateAnswers(post.Id);

				
				for( Answer answer : post.Answers )
				{
					answer.Comments = PopulateComments(answer.Id);
				}
								
				if( count % 1000 == 0 )
				{
					System.out.println(post.Title);
					if( post.Comments.size() > 0 )
						System.out.println(post.Comments.get(0).Comment);
				}
				count++;
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		return list;
	}
	
	private List<CommentInfo> PopulateComments(int id)
	{
		List<CommentInfo> list = new ArrayList<CommentInfo>();
		try
		{
			ResultSet set = _commonStatement.executeQuery("SELECT * FROM Comments WHERE PostId="+id);
			while( set.next() )
			{
				CommentInfo comment = new CommentInfo();
				comment.Comment = set.getString("Comment");
				comment.CreationDate = set.getDate("CreationDate");
				comment.Score = set.getInt("Score");
				comment.UserId = set.getInt("UserId");
				comment.Id = set.getInt("Id");
				
				list.add(comment);
			}
			set.close();
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
		return list;
	}

	// Case Insensitive
	private String BuildLikeClauses(String[] searchTags)
	{
		List<String> queryParts = new ArrayList<String>();
		for( String searchTag : searchTags )
		{
			queryParts.add("LOWER(TAGS) LIKE '%" + searchTag + "%'");
		}
		
		return join(queryParts, " OR ");
	}

	private Question PopulateQuestions(ResultSet set)
	{
		Question info = new Question();
		try
		{
			info.Id = set.getInt("Id");
			info.PostTypeId = set.getInt("PostTypeId");
			info.OwnerUserId = set.getInt("OwnerUserId");
			info.CreationDate = set.getDate("CreationDate");
			info.LastActivityDate = set.getDate("LastActivityDate");
			info.ViewCount = set.getInt("ViewCount");
			info.Tags = set.getString("Tags");
			info.Title = set.getString("Title");
			info.Body = set.getString("Body");
			info.Score = set.getInt("Score");
			info.FavoriteCount = set.getInt("FavoriteCount");
			info.AnswerCount = set.getInt("AnswerCount");
			info.AcceptedAnswerId = (Integer)set.getObject("AcceptedAnswerId");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return info;
	}
	
	public List<Answer> PopulateAnswers(int parentId)
	{
		List<Answer> answers = new ArrayList<Answer>();
		try
		{
			ResultSet set = _commonStatement.executeQuery("SELECT Id,PostTypeId,ParentId,OwnerUserId,CreationDate,LastActivityDate,Body,ViewCount,Title FROM POSTS WHERE ParentId = " + parentId);
			while( set.next() )
			{
				Answer info = new Answer();
				info.Id = set.getInt("Id");
				info.PostTypeId = set.getInt("PostTypeId");
				info.OwnerUserId = set.getInt("OwnerUserId");
				info.CreationDate = set.getDate("CreationDate");
				info.LastActivityDate = set.getDate("LastActivityDate");
				info.ViewCount = set.getInt("ViewCount");
				info.Title = set.getString("Title");
				info.Body = set.getString("Body");
			}
			set.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		return answers;	
	}

	
	public static void main (String [] args) 
	{
		CheckoutTaggedPostsAndComments checkout = new CheckoutTaggedPostsAndComments();
		
		checkout.Init("stackuser", "bacon");
		List<Question> posts = checkout.Query(new String[]
		{
			"<internet-explorer>", "<ie>","<ie6>","<ie7>","<ie8>","<ie9>","<ie10>","<ie11>"
		});
		
		System.out.println("Posts with tags:" + posts.size());
	}
	
	 static String join(Collection<?> s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Iterator<?> iter = s.iterator();
	     while (iter.hasNext()) {
	         builder.append(iter.next());
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         builder.append(delimiter);
	     }
	     return builder.toString();
	 }


}
