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
	private static ArrayList<String> projectNameList = new ArrayList<String>(); //��Ŀ�����б�
	private static ArrayList<String> developerNameList = new ArrayList<String>(); //�����������б�
	private static ArrayList<String> InDeveloperNameList = new ArrayList<String>(); //��ָ��Ŀ����ߵ������б�
	private static int devCatagory[]; //��¼�����߽ڵ�������Ϣ
	private static ArrayList<NodeIndexEdge> edgeList = new ArrayList<NodeIndexEdge>(); //����ߵ���Ϣ
	
	public static void main(String args[]) throws Exception{
		mysqlOperation mysqlOp = new mysqlOperation(user,pwd,driver,emailGraphUrl);
		
		//���ı��ĵ��ж�ȡ��Ŀ����
		String projNameFile = "projectName.txt";
		getWholeProjectName(projNameFile);
		
		//������emailGraph������
		mysqlOp.buildSqlConnection();
		
		//��ÿ����Ŀ��ȡ���п����ߵ�Degree Centrality
		for(int i=0;i<projectNameList.size();i++)
		{
			//��ȡ��ѯ�����
			String selectSql = "select * from apacheemailgraph where projectName="+"'"+projectNameList.get(i)+"'";
			ResultSet edgeResultSet = mysqlOp.selectEmaillist(selectSql);
			
			/***********************************���ڻ�ȡ��������ȱ�Ĳ���**********************************************/
			//��ȡ��ǰ��Ŀ�Ŀ����������Լ���ָ��Ŀ���������
			getDeveloperName(edgeResultSet);
			
			//�õ���ǰ��Ŀ�Ķ��������ı���д���ı���
			getDegreeCentralityTxt(projectNameList.get(i).toString(),i);
			
			//��տ����������б�ָ�򿪷��������б��Լ��ߵ���Ϣ
			developerNameList.clear();
			InDeveloperNameList.clear();
			edgeList.clear();
			/*********************************************************************************/	
		}
		
		//�ر�����
		mysqlOp.closeSqlConnection();
	}
	
	//����Ŀ���Ƶ��ı�������ַ�������projectNameList
	public static void getWholeProjectName(String txtName) throws Exception
	{
		File projFile = new File(txtName);
		BufferedReader reader = new BufferedReader(new FileReader(projFile));
		
		String project = null;
		
		while((project = reader.readLine()) !=null)
			projectNameList.add(project);
		
		reader.close();
	}
	
	//��ȡ������Ա������
	public static void getDeveloperName(ResultSet rs) throws Exception
	{
		//Ҫ����rs.next����ȡ		
		rs.next();

		//��ȡ��������
		String authorName = rs.getString("authorName");
		
		//��ȡ��һ����������
		String anotherName = rs.getString("anotherName");
		
		developerNameList.add(authorName);
		developerNameList.add(anotherName);
		
		//��ӽ��߼�
		NodeIndexEdge e = new NodeIndexEdge(0,1,1);
		edgeList.add(e);
		
		//���ָ�򿪷�������
		InDeveloperNameList.add(authorName);
		
		//���������
		while(rs.next())
		{	
			authorName = rs.getString("authorName");
			anotherName = rs.getString("anotherName");
			
			//�ҵ�author��anotherAuthor�Ƿ��Ѿ�����list��
			int authorIndex = developerNameList.indexOf(authorName);
			int anotherIndex = developerNameList.indexOf(anotherName);
			
			//author�Ƿ���list��
			if(authorIndex == -1)
			{
				//���ָ�򿪷�������
				InDeveloperNameList.add(authorName);
				
				developerNameList.add(authorName);
				authorIndex = developerNameList.size()-1;
			}			
			
			//anotherName�Ƿ���list��
			if(anotherIndex == -1)
			{
				developerNameList.add(anotherName);
				anotherIndex = developerNameList.size()-1;
			}
			
			//��ӽ��߼�
			NodeIndexEdge E = new NodeIndexEdge(authorIndex,anotherIndex,1);
			edgeList.add(E);
		}
		
		//�ر�result set
		rs.close();		
	}
	
	//�Զ������Խ��з��࣬�ܹ��ֳ�5�ࣺͨ��ƽ���������࣬�Զ��ֺ�������ٽ��ж��֣������õ�4�ࡣ�����������Ϊ0����ô���ǵ�5��
	public static void degreeCentralityCatagory(int developerInDegree[],int devSize)
	{
		devCatagory = new int[devSize];
		
		//�Ƚ��ж���
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
				devCatagory[i] = 5;//5��ʾ��������Ϊ0����
		}
		
		avgDegreeCentrality /= noneZeroSize;
		
		//ȷ���������Ե�������ƽ����
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
		
		//ȷ����1��2��3��4��
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
	
	//�õ���ǰ��Ŀ�Ķ��������ı���д���ı���
	public static void getDegreeCentralityTxt(String projectName,int projectIndex) throws Exception
	{
		System.out.println(projectName);
		
		//��¼�����ߵ����
		int developerInDegree[] = new int[developerNameList.size()];
		
		for(int i=0;i<developerNameList.size();i++)
			developerInDegree[i] = 0;
		
		//�ڱ߼�������ҳ���ָ��Ŀ��������ƣ�������
		for(int i=0;i<InDeveloperNameList.size();i++)
		{
			int InDeveloperIndex = developerNameList.indexOf(InDeveloperNameList.get(i));
			
			for(int j=0;j<edgeList.size();j++)
			{
				if(InDeveloperIndex == edgeList.get(j).getNode1())
					developerInDegree[InDeveloperIndex]++;
			}
		}	
			
		//д����ļ�
		String fileName = "InDegree.txt";
		FileWriter InDegreeOut = new FileWriter (new File(fileName),true); 
		BufferedWriter InDegreeBW = new BufferedWriter(InDegreeOut);
		
		/*Note:
		 *windows�µ��ı��ļ����з�:\r\n 
		 *linux/unix�µ��ı��ļ����з�:\r 
		 *Mac�µ��ı��ļ����з�:\n
		 */		
//		InDegreeBW.write(projectName+"\r\n");
		InDegreeBW.write("t # "+projectIndex+"\r\n");
		
		int developerNameSize = developerNameList.size();
		
		//���������Խ��з���
		degreeCentralityCatagory(developerInDegree,developerNameSize);
		
		for(int i=0;i<developerNameSize;i++)
		{
			//��Degree Centrality���б�׼��
//			double degreeCentrality = (double)developerInDegree[i]/(double)(developerNameSize-1);
//			System.out.println(developerNameList.get(i)+" InDegree "+ developerInDegree[i] + "; Catagory: "+devCatagory[i]);
//			InDegreeBW.write(developerNameList.get(i)+" "+devCatagory[i]+"\r\n");
			
			InDegreeBW.write("v "+i+" "+devCatagory[i]+"\r\n");
		}
		
		//����ߵ���Ϣ
		for(int i=0;i<edgeList.size();i++)
			InDegreeBW.write("e "+edgeList.get(i).getNode1()+" "+edgeList.get(i).getNode2()+" 0"+"\r\n");
		
		//�ر�д��ߵ��ļ�
		InDegreeBW.close();
	}	
}
