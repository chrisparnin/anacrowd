package anacrowd.api.elements;

import java.util.ArrayList;
import java.util.List;

public class MethodElem 
{
	public String Name;
	public String FullName;
	public boolean IsPublic;
	
	// Analytics...
	public int NumQuestions;
	
	public List<Integer> QuestionIds = new ArrayList<Integer>();
	public List<Integer> AnswerIds = new ArrayList<Integer>();

}
