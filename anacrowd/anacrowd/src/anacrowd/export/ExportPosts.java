package anacrowd.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.PostInfo;
import anacrowd.stackdb.DBInfo.UserInfo;

public class ExportPosts 
{
	public void DumpInfo( String questionsFile, String answersFile, String tag )
	{
		List<PostInfo> list = ExportQuestions(questionsFile, tag);
		ExportAnswers(answersFile, list);
	}

	private void ExportAnswers(String answersFile, List<PostInfo> list) 
	{
		try 
		{
			FileWriter fw = new FileWriter(answersFile);
		
			DBInfo info = new DBInfo();
			info.Init();
			
			
			for( PostInfo q : list )
			{
				for( PostInfo a : info.Answers(q.Id))
				{
					fw.write( join( new String[]
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
					fw.write("\n");
				}
			}
			
			info.Close();
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	
	private List<PostInfo> ExportQuestions(String questionsFile, String tag) {
		try 
		{
			FileWriter fw = new FileWriter(questionsFile);
		
			DBInfo info = new DBInfo();
			info.Init();
			
			List<PostInfo> list = info.Questions(tag);
			for( PostInfo ui : list )
			{
				fw.write( join( new String[]
				        {
							ui.Id+ "",
							ui.PostTypeId+ "",
							ui.OwnerUserId+ "",
							ui.CreationDate.toString(),
							ui.LastActivityDate.toString(),
							ui.ViewCount+"",
							ui.Tags,
							ui.Title.replace(",", "[;]")
				        }, 
						","));
				fw.write("\n");
			}
			
			info.Close();
			fw.close();
			
			return list;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	 static String join(String[] stringsA, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Collection<String> strings = Arrays.asList(stringsA);
	     Iterator iter = strings.iterator();
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
