package anacrowd.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;

public class Post2Api
{
	public void DumpAPIUsageData( AbstractApi api, String outFile )
	{
		try 
		{
			FileWriter fw = new FileWriter(outFile);
		
			for( PackageElem pack : api.Packages )
			{
				for( ClassElem klass : pack.Classes )
				{
					for( Integer id : klass.AnswerIds )
					{
						fw.write( join( new String[]{"class", pack.Name +":" +klass.Name, "answer", id+""}, ","));
						fw.write("\n");
					}
					for( Integer id : klass.QuestionIds )
					{
						fw.write( join( new String[]{"class", pack.Name +":" +klass.Name, "question", id+""}, ","));
						fw.write("\n");
					}
					
					for( MethodElem meth : klass.Methods )
					{
						for( Integer id : klass.AnswerIds )
						{
							fw.write( join( new String[]{"method", pack.Name +":" + klass.Name + ":" + meth.Name, "answer", id+""}, ","));
							fw.write("\n");
						}
						for( Integer id : klass.QuestionIds )
						{
							fw.write( join( new String[]{"method", pack.Name +":" + klass.Name + ":" + meth.Name, "question", id+""}, ","));
							fw.write("\n");
						}
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
