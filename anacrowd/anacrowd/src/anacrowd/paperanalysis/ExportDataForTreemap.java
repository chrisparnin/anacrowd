package anacrowd.paperanalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.PostInfo;
import anacrowd.stackdb.DBInfo.UserInfo;

public class ExportDataForTreemap extends BaseAnalysis
{
	@Override
	public void Run()
	{
		//ExportAndroid();
		//ExportJava();
		DumpAPIInfo(Android, "android_apiinfo.csv");
		DumpAPIInfo(Java, "java_apiinfo.csv");
	}

	private void ExportAndroid() 
	{
		AbstractApi api = Android;
		List<ClassDocumentation> docs = DeserializeModel( api);

		System.out.println("exporting coverage");
		DumpCoverage(docs, "android_coverage.csv");
		
		System.out.println("exporting posts2api");
		DumpAPILinks(docs, "android_posts2api.csv");
		
		System.out.println("exporting questions and answers details");
		ExportPosts("android_questions.csv", "android_answers.csv", docs);
		
		System.out.println("exporting users");
		ExportUsers("users.csv");
		
		System.out.println("exporting api info");
		DumpAPIInfo(api, "android_apiinfo.csv");
	}
	
	private void ExportJava() 
	{
		AbstractApi api = Java;
		List<ClassDocumentation> docs = DeserializeModel(api);

		System.out.println("exporting coverage");
		DumpCoverage(docs, "java_coverage.csv");
		
		System.out.println("exporting posts2api");
		DumpAPILinks(docs, "java_posts2api.csv");
		
		System.out.println("exporting questions and answers details");
		ExportPosts("java_questions.csv", "java_answers.csv", docs);
		
		System.out.println("exporting users");
		ExportUsers("users.csv");
		
		System.out.println("exporting api info");
		DumpAPIInfo(api, "java_apiinfo.csv");
	}

	
	public void DumpAPIInfo( AbstractApi api, String outFile )
	{
		try 
		{
			FileWriter fw = new FileWriter(outFile);
		
			for( PackageElem pack : api.Packages )
			{
				for( ClassElem klass : pack.Classes )
				{
					fw.write( join( new String[]{pack.Name +":" +klass.Name, klass.Methods.size()+""}, ","));
					fw.write("\n");
				}
			}
			
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void ExportUsers( String outFile )
	{
		try 
		{
			FileWriter fw = new FileWriter(outFile);
		
			DBInfo info = new DBInfo();
			info.Init();
			
			for( UserInfo ui : info.Users() )
			{
				fw.write( join( new String[]
				        {
							ui.Id+ "",
							ui.Name+ "",
							ui.Reputation+ "",
							ui.Age+ "",
							ui.Creation.toString(),
							ui.LastActivity.toString(),
							ui.Views+ "",
							ui.UpVotes+ "",
							ui.Downvotes + ""
				        }, 
						","));
				fw.write("\n");
			}
			
			info.Close();
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void ExportPosts(String questionsFile, String answersFile, List<ClassDocumentation> docs)
	{
		//List<PostInfo> list = ExportQuestions(questionsFile, tag);
		//ExportAnswers(answersFile, list);
		try
		{
			FileWriter fwAnswers = new FileWriter(answersFile);
			FileWriter fwQuestions = new FileWriter(questionsFile);
			DBInfo info = new DBInfo();
			info.Init();
			
			for( ClassDocumentation doc : docs )
			{
				String packName = doc.Klass.ParentPackage.Name;
				String klassName = doc.Klass.Name;
				for( ThreadElement thread : doc.getValidatedThreads() )
				{
					for( AnswerElement ans : thread.Answers)
					{
						if( ans.IsValidated(doc.Klass) )
						{
							int id = ans.Id;
							PostInfo a = info.GetPostDetail(id);
							fwAnswers.write( join( new String[]
							        {
										a.Id+ "",
										a.PostTypeId+ "",
										a.ParentId+ "",
										a.OwnerUserId+ "",
										a.CreationDate.toString(),
										a.LastActivityDate.toString(),
										//a.ViewCount+"",
										//a.Tags,
										//a.Title.replace(",", "[;]")
							        }, 
									","));
							fwAnswers.write("\n");
						}
					}
					if( thread.Question.IsLinked && thread.Question.IsValidated(doc.Klass) )
					{
						int id = thread.Question.Id;
						PostInfo q = info.GetPostDetail(id);

						fwQuestions.write( join( new String[]
						        {
									q.Id+ "",
									q.PostTypeId+ "",
									q.OwnerUserId+ "",
									q.CreationDate.toString(),
									q.LastActivityDate.toString(),
									q.ViewCount+"",
									q.Tags,
									q.Title.replace(",", "[;]")
						        }, 
								","));
						fwQuestions.write("\n");
					}
				}
			}
			info.Close();
			fwQuestions.close();
			fwAnswers.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public void DumpAPILinks( List<ClassDocumentation> docs, String outFile )
	{
		try 
		{
			FileWriter fw = new FileWriter(outFile);
			for( ClassDocumentation doc : docs )
			{
				String packName = doc.Klass.ParentPackage.Name;
				String klassName = doc.Klass.Name;
				for( ThreadElement thread : doc.getValidatedThreads() )
				{
					for( AnswerElement ans : thread.Answers)
					{
						if( ans.IsValidated(doc.Klass))
						{
							int id = ans.Id;
							fw.write( join( new String[]{"class", packName +":" +klassName, "answer", id+""}, ","));
							fw.write("\n");
						}
					}
					if( thread.Question.IsLinked && thread.Question.IsValidated(doc.Klass) )
					{
						int id = thread.Question.Id;
						fw.write( join( new String[]{"class", packName +":" +klassName, "question", id+""}, ","));
						fw.write("\n");
					}
				}
			}
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	
	public void DumpCoverage( List<ClassDocumentation> docs, String outputFile )
	{
		try 
		{
			FileWriter fw = new FileWriter(outputFile);

			for( ClassDocumentation doc : docs )
			{
				int num = doc.getValidatedThreads().size();
				String packName = doc.Klass.ParentPackage.Name;
				String klassName = doc.Klass.Name;
				fw.write( join( new String[]
				        {
							packName + ":" + klassName+ "", 
							num + ""
				        }, 
						","));
				fw.write("\n");
			}
			
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	
	
	
	public static void main(String[] args)
	{
		ExportDataForTreemap export = new ExportDataForTreemap();
		export.Run();
	}
}
