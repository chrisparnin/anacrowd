package anacrowd.paperanalysis.datacharacterization.threads;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.api.AndroidApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.paperanalysis.BaseAnalysis;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.PostInfo;

public class ThreadStats extends BaseAnalysis 
{
	DBInfo info;
	@Override
	public void Run()
	{
//		OutputTaggedThreads("android.tag.threads.txt", "<android>");
//		System.out.println("wrote tagged threads");
//		OutputLinkedThreads("android.linked.threads.txt", Android, "classdoc/android");
//		System.out.println("wrote linked threads");
		
		//OutputTaggedThreads("gwt.tag.threads.txt", "<gwt>");
		OutputLinkedThreads("gwt.linked.threads.txt", GWT);

		//OutputTaggedThreads("android.tag.threads.txt", "<android>");
		OutputLinkedThreads("android.linked.threads.txt", Android);

		//OutputTaggedThreads("java.tag.threads.txt", "<java>");
		OutputLinkedThreads("java.linked.threads.txt", Java);

	}
	
	private void OutputLinkedThreads(String outputPath, AbstractApi api) 
	{
		List<ClassDocumentation> docs = DeserializeModel(api);
		Hashtable<Integer,ThreadElement> threadMap = new Hashtable<Integer,ThreadElement>();
		
		for( ClassDocumentation doc : docs )
		{
			for( ThreadElement t : doc.getValidatedThreads() )
			{
				threadMap.put(t.Question.Id, t);
			}
		}

		info = new DBInfo();
		info.Init();

		List<PostInfo> linkedPosts = new ArrayList<PostInfo>();
		for( ThreadElement t : threadMap.values() )
		{
			linkedPosts.add(info.GetPostDetail(t.Question.Id));
		}
		OutputThreads(outputPath,linkedPosts);
		info.Close();
	}

	public void OutputTaggedThreads(String outputPath, String tag)
	{
		info = new DBInfo();
		info.Init();
		
		List<PostInfo> taggedPosts = info.Tags(tag);
		OutputThreads(outputPath,taggedPosts);

		info.Close();
	}
	
	public void OutputThreads(String outputPath, List<PostInfo> posts)
	{
		try
		{
			FileWriter fw = new FileWriter(outputPath);
			for( PostInfo info : posts )
			{
				fw.write(join(new String[]
						{
							info.AnswerCount + "",
							info.FavoriteCount +"",
							info.Score +"",
							info.ViewCount +"",
							info.AcceptedAnswerId != null ? "1": "0"
						},","));
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
		new ThreadStats().Run();
	}

}
