package freSubGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import mysql.mysqlOperation;

public class frequentSubGraph {
	private final static String driver = "com.mysql.jdbc.Driver";
	private final static String emailGraphUrl = "jdbc:mysql://localhost:3306/mailgraph";
	private final static String emailListUrl = "jdbc:mysql://localhost:3306/maillist";
	private final static String user = "root";
	private final static String pwd = "zc529zc952";
	private final static String	projectName = "zookeeper-bookkeeper";
	
	public static void main(String args[]) throws Exception{
		mysqlOperation mysqlOp = new mysqlOperation(user,pwd,driver,emailGraphUrl);
		//创建与emailGraph的连接
		mysqlOp.buildSqlConnection();
		
		//获取查询结果集

		String selectSql = "select * from apacheemailgraph where projectName = '"+projectName+"'";
		ResultSet edgeResultSet = mysqlOp.selectEmaillist(selectSql); 
		
		//得到描述图的文本文档
		getGraphTxt(edgeResultSet,projectName);
		
		//关闭连接
		mysqlOp.closeSqlConnection();
	}
	
	//得到描述图的文本文档
	public static void getGraphTxt(ResultSet rs,String project) throws Exception
	{
		//要先用rs.next来获取		
		rs.next();
		
		//获取作者名称
		String authorName = rs.getString("authorName");
		
		//获取另一个作者名称
		String anotherName = rs.getString("anotherName");
		
		List<String> nodeList = new LinkedList<String>();
		nodeList.add(authorName);
		nodeList.add(anotherName);
		
		//将边写入文件
		FileWriter edgeOut = new FileWriter (new File("edge.txt")); 
		BufferedWriter edgeBw = new BufferedWriter(edgeOut);
		edgeBw.write("t # 0\r\n");
		
		String writebuffer = "e 0 1 0\r\n";
		/*Note:
		 *windows下的文本文件换行符:\r\n 
		 *linux/unix下的文本文件换行符:\r 
		 *Mac下的文本文件换行符:\n
		 */		
		edgeBw.write(writebuffer);
		
		//遍历结果集
		while(rs.next())
		{
			authorName = rs.getString("authorName");
			anotherName = rs.getString("anotherName");
			
			//找到author和anotherAuthor是否已经存在list中
			int authorIndex = nodeList.indexOf(authorName);
			int anotherIndex = nodeList.indexOf(anotherName);
			
			//author是否在list中
			if(authorIndex == -1)
			{
				nodeList.add(authorName);
				authorIndex = nodeList.size()-1;
			}
			
			//anotherName是否在list中
			if(anotherIndex == -1)
			{
				nodeList.add(anotherName);
				anotherIndex = nodeList.size()-1;
			}
			
			writebuffer = "e " + authorIndex + " " + anotherIndex+" 0\r\n";
			edgeBw.write(writebuffer);
		}
		
		//关闭写入边的文件
		edgeBw.close();

		//将边及其隐射关系写入文件
		FileWriter vertexOut = new FileWriter (new File("vertex.txt"));
		String labelName = "labelMap_"+project+".txt";
		FileWriter mapOut = new FileWriter(new File(labelName));
		
		BufferedWriter vertexBw = new BufferedWriter(vertexOut);
		BufferedWriter mapBw = new BufferedWriter(mapOut);
		
		//遍历list
		for(int i=0;i<nodeList.size();i++)
		{
			writebuffer = "v "+i+" "+i+"\r\n";
			vertexBw.write(writebuffer);
			
			writebuffer = i+" "+nodeList.get(i)+"\r\n";
			mapBw.write(writebuffer);
		}
		
		//关闭写入点的文件
		vertexBw.close();
		mapBw.close();
		
		//关闭result set
		rs.close();
	}
}
