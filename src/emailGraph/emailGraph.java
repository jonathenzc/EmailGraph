package emailGraph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class emailGraph {
	private final static String driver = "com.mysql.jdbc.Driver";
	private final static String emailGraphUrl = "jdbc:mysql://localhost:3306/mailgraph";
	private final static String emailListUrl = "jdbc:mysql://localhost:3306/maillist";
	private final static String user = "root";
	private final static String pwd = "zc529zc952";
	private final static int edgeID = 41997;
	private final static String projectName = "zookeeper-bookkeeper";
	
	private static List<Edge> list = new LinkedList<Edge>();
	
	//����mysql����
	public static Connection buildSqlConnection(String url) throws SQLException
	{
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Connection conn = DriverManager.getConnection(url,user,pwd);
			
			if(!conn.isClosed())
				System.out.println(url+" connection built");
			
			return conn;
	}
	
	//�ر�mysql����
	public static void closeSqlConnection(Connection conn) throws SQLException
	{
		conn.close();
		
		System.out.println("connection closed");
	}
	
	//��mysql��ȡemaillist������
	public static ResultSet selectEmaillist(Connection conn,String project) throws Exception
	{
		Statement statement = conn.createStatement();
		
		String sql = "select * from apacheEmailList where projectName = '" + project+"'";
		
		ResultSet emailList = statement.executeQuery(sql);
		
		return emailList;
	}
	
	//��mysql����emailgraph�е�һ����
	public static void insertAnEdge(Connection conn,int id, String author, String project, int linkValue, String anotherAuthor) throws SQLException
	{
		Statement statement = conn.createStatement();
		
		String sql = "insert into apacheEmailGraph values(";
		sql += id + ","; //add id
		sql += "\"" + author + "\","; //add authorName
		sql += "\"" + project + "\","; //add projectName
		sql += linkValue + ","; // add linkValue
		sql += "\"" + anotherAuthor + "\")"; //add anotherName
		
//		System.out.println(sql);
//		System.out.println(subject);
		
		int result = statement.executeUpdate(sql);
	}
	
	//�������õ�ͼ����mysql
	public static void insertGraph(Connection conn,String project) throws Exception
	{
		for(int i=0;i<list.size();i++)
		{
			String node1 = list.get(i).getNode1();
			String node2 = list.get(i).getNode2();
			int edgeValue = list.get(i).getValue();
			
			insertAnEdge(conn,i+edgeID,node1,project,edgeValue,node2);
		}
	}
	
	//�ж�����ʼ��Ƿ���һ����˵Ļظ�
	private static boolean isAReEmail(String s)
	{
		if(s.startsWith("Re:") || s.contains("[Commented]")|| s.contains("[updated]") || 
		   s.contains("[Assigned]") || s.contains("[Closed]") || s.contains("[Comment Edited]"))
		{
			return true;
		}
		else
			return false;
	}
	
	//����emailGraph
	public static void emailGraphConstruction(ResultSet rs) throws Exception
	{
		//Ҫ����rs.next����ȡ		
		rs.next();
		String subject = rs.getString("subject");
		
		//��ȡ��Ŀ����
		String projectName = rs.getString("projectName");
		
		//��ȡ�ʼ�����
		String fromName = rs.getString("from");
		
		List<String> nodeList = new LinkedList<String>();
		nodeList.add(fromName);
		
		while(rs.next())
		{
			String anotherSubject = rs.getString("subject");
			
			//�ж�����ʼ��Ƿ��Ǳ��˵Ļظ��ʼ�
			if(isAReEmail(anotherSubject))
			{
				String anotherFrom = rs.getString("from");
				nodeList.add(anotherFrom);
			}
			//һ��������ʼ��Ѿ���������֮ǰ�����ߺͳ��ִ�����ֵ��map��
			else
			{
				if(nodeList.size()>1)
				{
					addNewEdge(nodeList);
				
					fromName = rs.getString("from");
				
					nodeList.clear();
					nodeList.add(fromName);
				}
			}
		}
		
		//�ر�result set
		rs.close();
	}
	
	//��һ��subject���ʼ�edge��ֵ��list��
	public static void addNewEdge(List<String> nodeList)
	{
		//�����Ȩֵ
//		Iterator<String> nodeListIt = nodeList.iterator();
		
		List<Edge> edgeMap = new LinkedList<Edge>();
		Edge e = new Edge(nodeList.get(0),nodeList.get(1),1);
		edgeMap.add(e);
		
		for(int i=2;i<nodeList.size();i++)
		{
			int index = i-1;
			while(index >= 0 && nodeList.get(i).equals(nodeList.get(index)))
				index--;
			
			if(index>=0)
			{
				for(int j=0;j<edgeMap.size();j++)
				{
					String node1 = nodeList.get(i);
					String node2 = nodeList.get(index);
					if((edgeMap.get(j).getNode1().equals(node1) && edgeMap.get(j).getNode2().equals(node2)) ||
					   (edgeMap.get(j).getNode1().equals(node2) && edgeMap.get(j).getNode2().equals(node1)))
					{
						int edgeValue = edgeMap.get(j).getValue();
						edgeMap.get(j).setValue(edgeValue+1);
						break;
					}
					
					if(j==edgeMap.size()-1)
					{
						edgeMap.add(new Edge(node1,node2,1));
						break;
					}
				}
			}
		}
		
//		for(int i=0;i<edgeMap.size();i++)
//		{
//			System.out.println(edgeMap.get(i).getNode1()+" "+edgeMap.get(i).getNode2()+" "+edgeMap.get(i).getValue());
//		}
		
		//���ղŵõ��ı߸�ֵ��list��
		for(int i=0;i<edgeMap.size();i++)
		{
			String node1 = edgeMap.get(i).getNode1();
			String node2 = edgeMap.get(i).getNode2();
			int value = edgeMap.get(i).getValue();
			
			if(list.size()==0)
				list.add(new Edge(node1,node2,value));
			else
			{
				for(int j=0;j<list.size();j++)
				{
					if((list.get(j).getNode1().equals(node1) && list.get(j).getNode2().equals(node2)) ||
					   (list.get(j).getNode1().equals(node2) && list.get(j).getNode2().equals(node1)))
					{
						int edgeValue = list.get(j).getValue();
						list.get(j).setValue(edgeValue+value);
						break;
					}
					
					if(j==list.size()-1)
					{
						list.add(new Edge(node1,node2,value));
						break;	
					}
				}
			}
		}		
	}
	
	public static void main(String[] args) throws Exception 
	{

		
		// ����mysql����emailList
		Connection emailListConn = buildSqlConnection(emailListUrl);
		
		//��ȡjena��mailList
		ResultSet emailList = selectEmaillist(emailListConn,projectName);
		
		//����email graph
		emailGraphConstruction(emailList);
		
//		for(int i=0;i<list.size();i++)
//		{
//			System.out.println(list.get(i).getNode1()+" "+list.get(i).getNode2()+" "+list.get(i).getValue());
//		}		
		
		//�ر�mysql����emailList
		closeSqlConnection(emailListConn);
		
		// ����mysql����emailGraph
		Connection emailgraphConn = buildSqlConnection(emailGraphUrl);

		//�������õ�email graph�������ݿ���
		insertGraph(emailgraphConn,projectName);
		
		//�ر�mysql����emailGraph
		closeSqlConnection(emailListConn);


		
//		List<String> l = new LinkedList<String>();
//		l.add("A");
//		l.add("B");
//		l.add("B");
//		l.add("C");
//		l.add("B");
//		l.add("A");
//		l.add("D");
//		l.add("A");
//		
//		addNewEdge(l);
	}
}
