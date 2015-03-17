package emailGraph;

public class Edge {
	public Edge(String theNode1,String theNode2,int theValue)
	{
		node1 = theNode1;
		node2 = theNode2;
		edgeValue = theValue;
	}
	
	public String getNode1()
	{	return node1; }
	
	public String getNode2()
	{	return node2; }
	
	public int getValue()
	{	return edgeValue; }
	
	public void setNode1(String theNode1)
	{   node1 = theNode1; }

	public void setNode2(String theNode2)
	{   node2 = theNode2; }
	
	public void setValue(int theValue)
	{   edgeValue = theValue; }
	
	private String node1;
	private String node2;
	private int edgeValue;
}
