package anacrowd.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.UserInfo;

public class ExportUsers 
{
	public void DumpInfo( String outFile )
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
