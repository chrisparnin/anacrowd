package anacrowd.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.PostInfo;
import anacrowd.stackdb.DBInfo.UserInfo;

public class ExportCoverage 
{
	public void DumpCoverage( AbstractApi api, String outputFile )
	{
		try 
		{
			FileWriter fw = new FileWriter(outputFile);
		
			for( PackageElem pack : api.Packages)
			{
				for( ClassElem klass : pack.Classes )
				{
					int num = klass.AnswerIds.size() + klass.QuestionIds.size();

					fw.write( join( new String[]
					        {
								pack.Name + ":" + klass.Name+ "",
								num + ""
					        }, 
							","));
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
