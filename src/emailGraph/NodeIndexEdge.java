package emailGraph;

//��Ϊ��Ҫ�ڶ������������������Աߵ����˲����ַ�����������
public class NodeIndexEdge {
	public NodeIndexEdge(int theNode1,int theNode2,int theValue)
	{
		node1 = theNode1;
		node2 = theNode2;
		edgeValue = theValue;
	}
	
	public int getNode1()
	{	return node1; }
	
	public int getNode2()
	{	return node2; }
	
	public int getValue()
	{	return edgeValue; }
	
	public void setNode1(int theNode1)
	{   node1 = theNode1; }

	public void setNode2(int theNode2)
	{   node2 = theNode2; }
	
	public void setValue(int theValue)
	{   edgeValue = theValue; }
	
	private int node1;
	private int node2;
	private int edgeValue;
}
