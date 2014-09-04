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
		private String Body;
		//private byte[] compressedBody;
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
	
	public List<PostInfo> Query(String[] searchTags)
	{
		List<PostInfo> list = new ArrayList<PostInfo>();
		try
		{
			String query = "SELECT * FROM POSTS WHERE " + BuildLikeClauses(searchTags);
			System.out.println(query);
			ResultSet set = _commonStatement.executeQuery(query);
			while( set.next() )
			{
				PostInfo post = PopulatePostInfo(set);
				list.add(post);
			}
			set.close();
			
			int count= 0;
			for( PostInfo post : list )
			{
				PopulateComments(post);

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
	
	private void PopulateComments(PostInfo post)
	{
		try
		{
			ResultSet set = _commonStatement.executeQuery("SELECT * FROM Comments WHERE PostId="+post.Id);
			while( set.next() )
			{
				CommentInfo comment = new CommentInfo();
				comment.Comment = set.getString("Comment");
				comment.CreationDate = set.getDate("CreationDate");
				comment.Score = set.getInt("Score");
				comment.UserId = set.getInt("UserId");
				comment.Id = set.getInt("Id");
				
				post.Comments.add(comment);
			}
			set.close();
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
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

	private PostInfo PopulatePostInfo(ResultSet set)
	{
		PostInfo info = new PostInfo();
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
	
	public static void main (String [] args) 
	{
		CheckoutTaggedPostsAndComments checkout = new CheckoutTaggedPostsAndComments();
		
		checkout.Init("stackuser", "bacon");
		List<PostInfo> posts = checkout.Query(new String[]
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
