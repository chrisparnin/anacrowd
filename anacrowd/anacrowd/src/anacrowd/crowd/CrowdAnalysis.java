package anacrowd.crowd;



import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import java.util.Collections;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.Search;

public class CrowdAnalysis 
{
	public static class UserQA
	{
		public int UserId;
		public int Answers;
		public int Questions;
		public int Upvotes;
		public int Downvotes;
	}
	
	public static class CoverageEvent
	{
		public Date Time;
		public int PostId;
		public int PostTypeId;
		public String URI;
		public int UserId;
	}
	
	public void SpeedOfCrowd(AbstractApi api)
	{
		Search info = new Search();
		info.Init();
		
		String tag = api.MainTag;
		
		System.out.println("Created Answers Table");
		try 
		{
			String qTable = tag + "questions_table";
			String aTable = tag + "answers_table";
			info.CreateTemporaryTable(tag, qTable);
			info.CreateTemporaryRelatedTable(tag, aTable, qTable);
			
			Connection conn = info.getConnection();
			Statement s = conn.createStatement();

			List<CoverageEvent> coverageEvents = new ArrayList<CoverageEvent>();
			
			for( PackageElem pack : api.Packages )
			{
				System.out.println("Package: " + pack.Name);
				for( ClassElem klass : pack.Classes )
				{
					List<Integer> methQuestions = info.GetIds(qTable,  klass.Name);
					
					for( int id : methQuestions )
					{
						CoverageEvent evt = new CoverageEvent();
						
						ResultSet rs = s.executeQuery(
								"SELECT CreationDate,OwnerUserId FROM " + qTable + " " + 
								"WHERE Id = " + id
						);
						rs.next();
						evt.Time = rs.getDate("CreationDate");
						evt.PostId = id;
						evt.PostTypeId = 1;
						evt.URI = pack.Name + ":" + klass.Name;
						evt.UserId = rs.getInt("OwnerUserId");
						coverageEvents.add( evt );
					}
				}
			}

			HashSet<Date> etimes = new HashSet<Date>();
			for( CoverageEvent evt : coverageEvents )
			{
				etimes.add( evt.Time );
			}
			List<Date> times =  new ArrayList<Date>(etimes);
			Collections.sort(times);

			//System.out.println(relevantEvents.size());
			Hashtable<String,Integer> coverage = new Hashtable<String,Integer>();
			HashSet<Integer> users = new HashSet<Integer>();
			HashSet<Integer> crowd = new HashSet<Integer>();

			for( PackageElem pack : api.Packages )
			{
				for( ClassElem klass : pack.Classes )
				{
					coverage.put(pack.Name +":"+ klass.Name, 0);
				}
			}

			for( Date time : times )
			{
				List<CoverageEvent> relevantEvents = new ArrayList<CoverageEvent>();
				for( CoverageEvent evt : coverageEvents )
				{
					if( evt.Time.equals(time) )
					{
						relevantEvents.add(evt);
						crowd.add(evt.UserId);
					}
				}					
				
				for( PackageElem pack : api.Packages )
				{
					for( ClassElem klass : pack.Classes )
					{
						String key = pack.Name +":"+ klass.Name;
						for( CoverageEvent evt : relevantEvents )
						{
							if( evt.URI.equals(key))
							{
								coverage.put(key, coverage.get(key)+1);
								users.add(evt.UserId);
							}
						}
					}
				}
				
				int count = 0;
				for( String key: coverage.keySet() )
				{
					if( coverage.get(key) > 0 )
					{
						count++;
					}
				}
				
				System.out.println(
						time + "," + 
						count + "," + 
						coverage.size() + "," + 
						users.size() + "," + 
						crowd.size() + ","+
						relevantEvents.size()
				);
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		info.Close();
	}
	
	public void SizeOfCrowd(String tag )
	{
		Search info = new Search();
		info.Init();
		
		System.out.println("Created Answers Table");
		try 
		{
			String qTable = tag + "questions_table";
			String aTable = tag + "answers_table";
			info.CreateTemporaryTable(tag, qTable);
			info.CreateTemporaryRelatedTable(tag, aTable, qTable);
			
			Connection conn = info.getConnection();
			Statement s = conn.createStatement();
			
			Hashtable<Integer,UserQA> dict = new Hashtable<Integer,UserQA>();

			// Questions a User has asked
			ResultSet questionSet = s.executeQuery(
					"SELECT OwnerUserId,Count(*) as num FROM " + qTable + " " + 
					"GROUP BY OwnerUserId"
			);
			while( questionSet.next())
			{
				int userId = questionSet.getInt("OwnerUserId");
				int numQs = questionSet.getInt("num");
				if( !dict.containsKey(userId) )
				{
					dict.put(userId, new UserQA());
				}
				dict.get(userId).Questions = numQs;
			}

			// Answers a User has given
			ResultSet answerSet = s.executeQuery(
					"SELECT OwnerUserId,Count(*) as num FROM " + aTable + " " + 
					"GROUP BY OwnerUserId"
			);
			while( answerSet .next())
			{
				int userId = answerSet .getInt("OwnerUserId");
				int numAs = answerSet .getInt("num");
				if( !dict.containsKey(userId) )
				{
					dict.put(userId, new UserQA());
				}
				dict.get(userId).Answers = numAs;
			}
			
			for( int key : dict.keySet() )
			{
				// User details...
				ResultSet userSet = s.executeQuery(
						"SELECT UpVotes,DownVotes,Age,CreationDate,LastAccessDate,Reputation FROM USERS " + 
						"WHERE Id = " + key
				);
				userSet.next();
				
				System.out.print(key + ",");
				System.out.print(dict.get(key).Questions + ",") ;
				System.out.print(dict.get(key).Answers + "," );
				System.out.print(userSet.getInt("UpVotes" )+ "," );
				System.out.print(userSet.getInt("DownVotes" )+ "," );
				System.out.print(userSet.getInt("Age")+ "," );
				System.out.print(userSet.getDate("LastAccessDate")+ ",");
				System.out.print(userSet.getDate("CreationDate")+ ",");
				System.out.print(userSet.getInt("Reputation")+ "," );
				System.out.println();
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		info.Close();
	}
}
