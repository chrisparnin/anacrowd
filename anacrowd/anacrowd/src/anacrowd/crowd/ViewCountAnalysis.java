package anacrowd.crowd;



import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.stackdb.Search;

public class ViewCountAnalysis 
{
	public void ViewCountAndCrowdSize(AbstractApi api )
	{
		Search info = new Search();
		info.Init();
		String tag = api.MainTag;
		try 
		{
			String qTable = tag + "questions_table";
			String aTable = tag + "answers_table";
			info.CreateTemporaryTable(tag, qTable);
			info.CreateTemporaryRelatedTable(tag, aTable, qTable);
			
			Connection conn = info.getConnection();
			Statement s = conn.createStatement();
			
			FileWriter fstream = new FileWriter("C:\\method_output.txt");
			PrintWriter out = new PrintWriter(fstream);
			out.println("class,method,accepted,viewCountSum,scoreSum,answerCountSum,commentCountSum,favoriteCountSum,questions");
//			WordDistributions wd = new WordDistributions();
			for( PackageElem pack : api.Packages )
			{
				System.out.println("Package: " + pack.Name);
				int counter = 0;
				for( ClassElem klass : pack.Classes )
				{
//					if (wd.WordLength(klass.Name) == 1) {
//						continue;
//					}
					System.out.println("Class: " + klass.Name + " (" + ++counter + "/" + pack.Classes.size() + ")");
					for (MethodElem methodElem : klass.Methods) 
					{
						List<Integer> methodQuestionIds = info.GetIds(qTable, methodElem.Name);
					
						int acceptedAnswers = 0;
						int viewCounts = 0;
						int scores = 0;
						int answerCounts = 0;
						int commentCounts = 0;
						int favoriteCounts = 0;
					
						for( int id : methodQuestionIds )
						{
							ResultSet question = s.executeQuery(
									"SELECT * FROM POSTS " +
											"WHERE Id = " + id
									);

							while (question.next()) {
								if (question.getInt("AcceptedAnswerId") > 0) {
									acceptedAnswers++;
								}
								viewCounts += question.getInt("ViewCount");
								scores += question.getInt("Score");
								answerCounts += question.getInt("AnswerCount");
								commentCounts += question.getInt("CommentCount");
								favoriteCounts += question.getInt("FavoriteCount");
							}
						}
						out.print(pack.Name + ":" + klass.Name + "," + methodElem.Name + ",");
						out.print(acceptedAnswers + ",");
						out.print(viewCounts + ",");
						out.print(scores + ",");
						out.print(answerCounts + ",");
						out.print(commentCounts + ",");
						out.print(favoriteCounts + ",");
						out.print(methodQuestionIds.size());
						out.println("");
						out.flush();
					}
				}
			}
			out.close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		catch (IOException ioe) 
		{
			ioe.printStackTrace();
		}
		
		info.Close();
	}
	
	public void rmDuplicatesAnalysis(AbstractApi api )
	{
		Search info = new Search();
		info.Init();
		String tag = api.MainTag;
		try 
		{
			String qTable = tag + "questions_table";
			String aTable = tag + "answers_table";
			info.CreateTemporaryTable(tag, qTable);
			info.CreateTemporaryRelatedTable(tag, aTable, qTable);
			
			Connection conn = info.getConnection();
			Statement s = conn.createStatement();
			
			FileWriter fstream = new FileWriter("C:\\noDuplicatesOutput.txt");
			PrintWriter out = new PrintWriter(fstream);
			out.println("accepted,viewCountSum,scoreSum,answerCountSum,commentCountSum,favoriteCountSum,questions");

			Set<Integer> allKlassQuestionIds = new HashSet<Integer>();
			for( PackageElem pack : api.Packages )
			{
				System.out.println("Package: " + pack.Name);
				int counter = 0;
				for( ClassElem klass : pack.Classes )
				{
					System.out.println("Class: " + klass.Name + " (" + ++counter + "/" + pack.Classes.size() + ")");
					List<Integer> klassQuestionIds = info.GetIds(qTable,  klass.Name);
					allKlassQuestionIds.addAll(klassQuestionIds);
				}
			}
			int acceptedAnswers = 0;
			int viewCounts = 0;
			int scores = 0;
			int answerCounts = 0;
			int commentCounts = 0;
			int favoriteCounts = 0;
			
			int counter2 = 0;
			for( int id : allKlassQuestionIds )
			{
				System.out.println(++counter2 + "/" + allKlassQuestionIds.size());
				ResultSet question = s.executeQuery(
						"SELECT * FROM POSTS " +
					    "WHERE Id = " + id
			    );

				while (question.next()) {
					if (question.getInt("AcceptedAnswerId") > 0) {
						acceptedAnswers++;
					}
					viewCounts += question.getInt("ViewCount");
					scores += question.getInt("Score");
					answerCounts += question.getInt("AnswerCount");
					commentCounts += question.getInt("CommentCount");
					favoriteCounts += question.getInt("FavoriteCount");
				}
			}
			out.print(acceptedAnswers + ",");
			out.print(viewCounts + ",");
			out.print(scores + ",");
			out.print(answerCounts + ",");
			out.print(commentCounts + ",");
			out.print(favoriteCounts + ",");
			out.print(allKlassQuestionIds.size());
			out.println("");
			out.flush();

			out.close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		catch (IOException ioe) 
		{
			ioe.printStackTrace();
		}
		
		info.Close();
	}
}
