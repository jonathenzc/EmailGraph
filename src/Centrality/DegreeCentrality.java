package Centrality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mysql.mysqlOperation;

public class DegreeCentrality {
	private final static String driver = "com.mysql.jdbc.Driver";
	private final static String emailGraphUrl = "jdbc:mysql://localhost:3306/mailgraph";
	private final static String emailListUrl = "jdbc:mysql://localhost:3306/maillist";
	private final static String user = "root";
	private final static String pwd = "zc529zc952";
	private static ArrayList projectNameList = new ArrayList(); //项目名称列表
	private static ArrayList developerNameList = new ArrayList(); //开发者名字列表
	private static ArrayList InDeveloperNameList = new ArrayList(); //被指向的开发者的名称列表
	
	public static void main(String args[]) throws Exception{
		mysqlOperation mysqlOp = new mysqlOperation(user,pwd,driver,emailGraphUrl);
		
		//从文本文档中读取项目名称
		String projNameFile = "projectName.txt";
		getWholeProjectName(projNameFile);
		
		//创建与emailGraph的连接
		mysqlOp.buildSqlConnection();
		
		//对每个项目获取其中开发者的Degree Centrality
		for(int i=0;i<projectNameList.size();i++)
		{
			//获取查询结果集
			String selectSql = "select * from apacheemailgraph where projectName="+"'"+projectNameList.get(i)+"'";
			ResultSet edgeResultSet = mysqlOp.selectEmaillist(selectSql);
			
			//获取当前项目的开发者名称以及边指向的开发者名称
			getDeveloperName(edgeResultSet);
			
			//得到当前项目的度中心性文本并写入文本中
			getDegreeCentralityTxt(projectNameList.get(i).toString());
			
			//清空开发者名称列表以及指向开发者名称列表
			developerNameList.clear();
			InDeveloperNameList.clear();
		}
		
		//关闭连接
		mysqlOp.closeSqlConnection();
	}
	
	//将项目名称的文本读入进字符串数组projectNameList
	public static void getWholeProjectName(String txtName) throws Exception
	{
		File projFile = new File(txtName);
		BufferedReader reader = new BufferedReader(new FileReader(projFile));
		
		String project = null;
		
		while((project = reader.readLine()) !=null)
			projectNameList.add(project);
		
		reader.close();
	}
	
	//获取开发人员的名字
	public static void getDeveloperName(ResultSet rs) throws Exception
	{
		//要先用rs.next来获取		
		rs.next();

		//获取作者名称
		String authorName = rs.getString("authorName");
		
		//获取另一个作者名称
		String anotherName = rs.getString("anotherName");
		
		developerNameList.add(authorName);
		developerNameList.add(anotherName);
		
		//添加指向开发者名称
		InDeveloperNameList.add(anotherName);
		
		//遍历结果集
		while(rs.next())
		{	
			authorName = rs.getString("authorName");
			anotherName = rs.getString("anotherName");
			
			//添加指向开发者名称
			InDeveloperNameList.add(anotherName);
			
			//找到author和anotherAuthor是否已经存在list中
			int authorIndex = developerNameList.indexOf(authorName);
			int anotherIndex = developerNameList.indexOf(anotherName);
			
			//author是否在list中
			if(authorIndex == -1)
				developerNameList.add(authorName);
			
			//anotherName是否在list中
			if(anotherIndex == -1)
				developerNameList.add(anotherName);		
		}
		
		//关闭result set
		rs.close();		
	}
	
	
	//得到当前项目的度中心性文本并写入文本中
	public static void getDegreeCentralityTxt(String projectName) throws Exception
	{
		System.out.println(projectName);
		
		//记录开发者的入度
		int developerInDegree[] = new int[developerNameList.size()];
		
		for(int i=0;i<developerNameList.size();i++)
			developerInDegree[i] = 0;
		
		//在指向开发者名称列表中逐个找出开发者名称，并计数
		for(int i=0;i<developerNameList.size();i++)
		{
			for(int j=0;j<InDeveloperNameList.size();j++)
			{
				if(developerNameList.get(i).equals(InDeveloperNameList.get(j)))
					developerInDegree[i]++;
			}
		}	
			
		//写入度文件
		String fileName = "InDegree.txt";
		FileWriter InDegreeOut = new FileWriter (new File(fileName),true); 
		BufferedWriter InDegreeBW = new BufferedWriter(InDegreeOut);
		
		/*Note:
		 *windows下的文本文件换行符:\r\n 
		 *linux/unix下的文本文件换行符:\r 
		 *Mac下的文本文件换行符:\n
		 */		
		InDegreeBW.write(projectName+"\r\n");
		
		int developerNameSize = developerNameList.size();
		
		for(int i=0;i<developerNameList.size();i++)
		{
			//对Degree Centrality进行标准化
			double degreeCentrality = (double)developerInDegree[i]/(double)(developerNameSize-1);
			
//			System.out.println(developerNameList.get(i)+" "+degreeCentrality);
			InDegreeBW.write(developerNameList.get(i)+" "+degreeCentrality+"\r\n");
		}
		
		//关闭写入边的文件
		InDegreeBW.close();
	}	
}
