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

import emailGraph.NodeIndexEdge;
import mysql.mysqlOperation;

public class DegreeCentrality {
	private final static String driver = "com.mysql.jdbc.Driver";
	private final static String emailGraphUrl = "jdbc:mysql://localhost:3306/mailgraph";
	private final static String emailListUrl = "jdbc:mysql://localhost:3306/maillist";
	private final static String user = "root";
	private final static String pwd = "zc529zc952";
	private static ArrayList<String> projectNameList = new ArrayList<String>(); //项目名称列表
	private static ArrayList<String> developerNameList = new ArrayList<String>(); //开发者名字列表
	private static ArrayList<String> InDeveloperNameList = new ArrayList<String>(); //被指向的开发者的名称列表
	private static int devCatagory[]; //记录开发者节点分类的信息
	private static ArrayList<NodeIndexEdge> edgeList = new ArrayList<NodeIndexEdge>(); //储存边的信息
	
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
			
			/***********************************用于获取开发者入度表的操作**********************************************/
			//获取当前项目的开发者名称以及边指向的开发者名称
			getDeveloperName(edgeResultSet);
			
			//得到当前项目的度中心性文本并写入文本中
			getDegreeCentralityTxt(projectNameList.get(i).toString(),i);
			
			//清空开发者名称列表、指向开发者名称列表以及边的信息
			developerNameList.clear();
			InDeveloperNameList.clear();
			edgeList.clear();
			/*********************************************************************************/	
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
		
		//添加进边集
		NodeIndexEdge e = new NodeIndexEdge(0,1,1);
		edgeList.add(e);
		
		//添加指向开发者名称
		InDeveloperNameList.add(authorName);
		
		//遍历结果集
		while(rs.next())
		{	
			authorName = rs.getString("authorName");
			anotherName = rs.getString("anotherName");
			
			//找到author和anotherAuthor是否已经存在list中
			int authorIndex = developerNameList.indexOf(authorName);
			int anotherIndex = developerNameList.indexOf(anotherName);
			
			//author是否在list中
			if(authorIndex == -1)
			{
				//添加指向开发者名称
				InDeveloperNameList.add(authorName);
				
				developerNameList.add(authorName);
				authorIndex = developerNameList.size()-1;
			}			
			
			//anotherName是否在list中
			if(anotherIndex == -1)
			{
				developerNameList.add(anotherName);
				anotherIndex = developerNameList.size()-1;
			}
			
			//添加进边集
			NodeIndexEdge E = new NodeIndexEdge(authorIndex,anotherIndex,1);
			edgeList.add(E);
		}
		
		//关闭result set
		rs.close();		
	}
	
	//对度中心性进行分类，总共分成5类：通过平均数来分类，对二分后的数据再进行二分，这样得到4类。如果度中心性为0，那么就是第5类
	public static void degreeCentralityCatagory(int developerInDegree[],int devSize)
	{
		devCatagory = new int[devSize];
		
		//先进行二分
		double avgDegreeCentrality =0 ;
		int noneZeroSize=0;
		for(int i=0;i<devSize;i++)
		{
			if(developerInDegree[i]!=0)
			{
				avgDegreeCentrality += developerInDegree[i];
				noneZeroSize++;
			}
			else
				devCatagory[i] = 5;//5表示度中心性为0的类
		}
		
		avgDegreeCentrality /= noneZeroSize;
		
		//确定度中心性的另两个平均数
		int onetwoCatagorySize=0;
		int threefourCatagorySize=0;
		double AvgOnetwoCatagory = 0;
		double AvgThreeFourCatagory = 0;
		for(int i=0;i<devSize;i++)
		{
			if(developerInDegree[i]>avgDegreeCentrality)
			{
				onetwoCatagorySize++;
				AvgOnetwoCatagory += developerInDegree[i];
			}
			else if(developerInDegree[i]<avgDegreeCentrality && developerInDegree[i]!=0)
			{
				threefourCatagorySize++;
				AvgThreeFourCatagory += developerInDegree[i];
			}
		}
		
		AvgOnetwoCatagory /= onetwoCatagorySize;
		AvgThreeFourCatagory /= threefourCatagorySize;
		
		//确定出1、2、3、4类
		for(int i=0;i<devSize;i++)
		{
			if(developerInDegree[i] >= AvgOnetwoCatagory)
				devCatagory[i] = 1;
			else if(developerInDegree[i] >= avgDegreeCentrality)
				devCatagory[i] = 2;
			else if(developerInDegree[i] >= AvgThreeFourCatagory)
				devCatagory[i] = 3;
			else if(developerInDegree[i] != 0)
				devCatagory[i] = 4;
		}
	}
	
	//得到当前项目的度中心性文本并写入文本中
	public static void getDegreeCentralityTxt(String projectName,int projectIndex) throws Exception
	{
		System.out.println(projectName);
		
		//记录开发者的入度
		int developerInDegree[] = new int[developerNameList.size()];
		
		for(int i=0;i<developerNameList.size();i++)
			developerInDegree[i] = 0;
		
		//在边集中逐个找出被指向的开发者名称，并计数
		for(int i=0;i<InDeveloperNameList.size();i++)
		{
			int InDeveloperIndex = developerNameList.indexOf(InDeveloperNameList.get(i));
			
			for(int j=0;j<edgeList.size();j++)
			{
				if(InDeveloperIndex == edgeList.get(j).getNode1())
					developerInDegree[InDeveloperIndex]++;
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
//		InDegreeBW.write(projectName+"\r\n");
		InDegreeBW.write("t # "+projectIndex+"\r\n");
		
		int developerNameSize = developerNameList.size();
		
		//给度中心性进行分类
		degreeCentralityCatagory(developerInDegree,developerNameSize);
		
		for(int i=0;i<developerNameSize;i++)
		{
			//对Degree Centrality进行标准化
//			double degreeCentrality = (double)developerInDegree[i]/(double)(developerNameSize-1);
//			System.out.println(developerNameList.get(i)+" InDegree "+ developerInDegree[i] + "; Catagory: "+devCatagory[i]);
//			InDegreeBW.write(developerNameList.get(i)+" "+devCatagory[i]+"\r\n");
			
			InDegreeBW.write("v "+i+" "+devCatagory[i]+"\r\n");
		}
		
		//输入边的信息
		for(int i=0;i<edgeList.size();i++)
			InDegreeBW.write("e "+edgeList.get(i).getNode1()+" "+edgeList.get(i).getNode2()+" 0"+"\r\n");
		
		//关闭写入边的文件
		InDegreeBW.close();
	}	
}
