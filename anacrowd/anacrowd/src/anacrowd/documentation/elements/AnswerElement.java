package anacrowd.documentation.elements;

import java.util.Date;

public class AnswerElement extends PostElement{

	public Date Date;
	public int Votes;
	public Integer BountyAmount;
	
	@Override
	public String GetStackOverflowUrl()
	{
		return "http://stackoverflow.com/a/" + Id;
	}
	
	private boolean isAcceptedAnswer;
	public boolean isAcceptedAnswer() 
	{
		return isAcceptedAnswer;
	}
	public void setIsAcceptedAnswer(boolean state)
	{
		isAcceptedAnswer = state;
	}
}
