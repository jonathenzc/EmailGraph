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
	
	//创建mysql连接
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
	
	//关闭mysql连接
	public static void closeSqlConnection(Connection conn) throws SQLException
	{
		conn.close();
		
		System.out.println("connection closed");
	}
	
	//从mysql读取emaillist的内容
	public static ResultSet selectEmaillist(Connection conn,String project) throws Exception
	{
		Statement statement = conn.createStatement();
		
		String sql = "select * from apacheEmailList where projectName = '" + project+"'";
		
		ResultSet emailList = statement.executeQuery(sql);
		
		return emailList;
	}
	
	//向mysql插入emailgraph中的一条边
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
	
	//将构建好的图插入mysql
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
	
	//判断这封邮件是否是一封别人的回复
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
	
	//构建emailGraph
	public static void emailGraphConstruction(ResultSet rs) throws Exception
	{
		//要先用rs.next来获取		
		rs.next();
		String subject = rs.getString("subject");
		
		//获取项目名称
		String projectName = rs.getString("projectName");
		
		//获取邮件作者
		String fromName = rs.getString("from");
		
		List<String> nodeList = new LinkedList<String>();
		nodeList.add(fromName);
		
		while(rs.next())
		{
			String anotherSubject = rs.getString("subject");
			
			//判断这封邮件是否是别人的回复邮件
			if(isAReEmail(anotherSubject))
			{
				String anotherFrom = rs.getString("from");
				nodeList.add(anotherFrom);
			}
			//一个主题的邮件已经结束，将之前的作者和出现次数赋值到map中
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
		
		//关闭result set
		rs.close();
	}
	
	//将一个subject的邮件edge赋值到list中
	public static void addNewEdge(List<String> nodeList)
	{
		//计算边权值
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
		
		//将刚才得到的边赋值到list中
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

		
		// 创建mysql连接emailList
		Connection emailListConn = buildSqlConnection(emailListUrl);
		
		//获取jena的mailList
		ResultSet emailList = selectEmaillist(emailListConn,projectName);
		
		//构建email graph
		emailGraphConstruction(emailList);
		
//		for(int i=0;i<list.size();i++)
//		{
//			System.out.println(list.get(i).getNode1()+" "+list.get(i).getNode2()+" "+list.get(i).getValue());
//		}		
		
		//关闭mysql连接emailList
		closeSqlConnection(emailListConn);
		
		// 创建mysql连接emailGraph
		Connection emailgraphConn = buildSqlConnection(emailGraphUrl);

		//将构建好的email graph存入数据库中
		insertGraph(emailgraphConn,projectName);
		
		//关闭mysql连接emailGraph
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
