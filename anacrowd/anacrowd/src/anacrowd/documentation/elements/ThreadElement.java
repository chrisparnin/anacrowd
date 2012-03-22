package anacrowd.documentation.elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import anacrowd.documentation.ClassDocumentation;

public class ThreadElement 
{
	public List<AnswerElement> Answers = new ArrayList<AnswerElement>();
	//public List<AnswerElement> LinkedAnswers = new ArrayList<AnswerElement>();
	public QuestionElement Question;
	
	public int Votes;
	public int FavoriteCount;
	public Date CreationDate;
	public int ViewCount;
	
	// Cannot quite share different linked answers.
	//have to move links to "ClassDoc" part of serialized object.
	//have to do better with Site....Put body on threadelement....
}
