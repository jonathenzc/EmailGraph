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
	private static ArrayList projectNameList = new ArrayList(); //��Ŀ�����б�
	private static ArrayList developerNameList = new ArrayList(); //�����������б�
	private static ArrayList InDeveloperNameList = new ArrayList(); //��ָ��Ŀ����ߵ������б�
	
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
			
			//��ȡ��ǰ��Ŀ�Ŀ����������Լ���ָ��Ŀ���������
			getDeveloperName(edgeResultSet);
			
			//�õ���ǰ��Ŀ�Ķ��������ı���д���ı���
			getDegreeCentralityTxt(projectNameList.get(i).toString());
			
			//��տ����������б��Լ�ָ�򿪷��������б�
			developerNameList.clear();
			InDeveloperNameList.clear();
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
		
		//���ָ�򿪷�������
		InDeveloperNameList.add(anotherName);
		
		//���������
		while(rs.next())
		{	
			authorName = rs.getString("authorName");
			anotherName = rs.getString("anotherName");
			
			//���ָ�򿪷�������
			InDeveloperNameList.add(anotherName);
			
			//�ҵ�author��anotherAuthor�Ƿ��Ѿ�����list��
			int authorIndex = developerNameList.indexOf(authorName);
			int anotherIndex = developerNameList.indexOf(anotherName);
			
			//author�Ƿ���list��
			if(authorIndex == -1)
				developerNameList.add(authorName);
			
			//anotherName�Ƿ���list��
			if(anotherIndex == -1)
				developerNameList.add(anotherName);		
		}
		
		//�ر�result set
		rs.close();		
	}
	
	
	//�õ���ǰ��Ŀ�Ķ��������ı���д���ı���
	public static void getDegreeCentralityTxt(String projectName) throws Exception
	{
		System.out.println(projectName);
		
		//��¼�����ߵ����
		int developerInDegree[] = new int[developerNameList.size()];
		
		for(int i=0;i<developerNameList.size();i++)
			developerInDegree[i] = 0;
		
		//��ָ�򿪷��������б�������ҳ����������ƣ�������
		for(int i=0;i<developerNameList.size();i++)
		{
			for(int j=0;j<InDeveloperNameList.size();j++)
			{
				if(developerNameList.get(i).equals(InDeveloperNameList.get(j)))
					developerInDegree[i]++;
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
		InDegreeBW.write(projectName+"\r\n");
		
		int developerNameSize = developerNameList.size();
		
		for(int i=0;i<developerNameList.size();i++)
		{
			//��Degree Centrality���б�׼��
			double degreeCentrality = (double)developerInDegree[i]/(double)(developerNameSize-1);
			
//			System.out.println(developerNameList.get(i)+" "+degreeCentrality);
			InDegreeBW.write(developerNameList.get(i)+" "+degreeCentrality+"\r\n");
		}
		
		//�ر�д��ߵ��ļ�
		InDegreeBW.close();
	}	
}
