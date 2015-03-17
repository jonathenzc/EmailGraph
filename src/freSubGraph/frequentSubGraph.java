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
		//������emailGraph������
		mysqlOp.buildSqlConnection();
		
		//��ȡ��ѯ�����

		String selectSql = "select * from apacheemailgraph where projectName = '"+projectName+"'";
		ResultSet edgeResultSet = mysqlOp.selectEmaillist(selectSql); 
		
		//�õ�����ͼ���ı��ĵ�
		getGraphTxt(edgeResultSet,projectName);
		
		//�ر�����
		mysqlOp.closeSqlConnection();
	}
	
	//�õ�����ͼ���ı��ĵ�
	public static void getGraphTxt(ResultSet rs,String project) throws Exception
	{
		//Ҫ����rs.next����ȡ		
		rs.next();
		
		//��ȡ��������
		String authorName = rs.getString("authorName");
		
		//��ȡ��һ����������
		String anotherName = rs.getString("anotherName");
		
		List<String> nodeList = new LinkedList<String>();
		nodeList.add(authorName);
		nodeList.add(anotherName);
		
		//����д���ļ�
		FileWriter edgeOut = new FileWriter (new File("edge.txt")); 
		BufferedWriter edgeBw = new BufferedWriter(edgeOut);
		edgeBw.write("t # 0\r\n");
		
		String writebuffer = "e 0 1 0\r\n";
		/*Note:
		 *windows�µ��ı��ļ����з�:\r\n 
		 *linux/unix�µ��ı��ļ����з�:\r 
		 *Mac�µ��ı��ļ����з�:\n
		 */		
		edgeBw.write(writebuffer);
		
		//���������
		while(rs.next())
		{
			authorName = rs.getString("authorName");
			anotherName = rs.getString("anotherName");
			
			//�ҵ�author��anotherAuthor�Ƿ��Ѿ�����list��
			int authorIndex = nodeList.indexOf(authorName);
			int anotherIndex = nodeList.indexOf(anotherName);
			
			//author�Ƿ���list��
			if(authorIndex == -1)
			{
				nodeList.add(authorName);
				authorIndex = nodeList.size()-1;
			}
			
			//anotherName�Ƿ���list��
			if(anotherIndex == -1)
			{
				nodeList.add(anotherName);
				anotherIndex = nodeList.size()-1;
			}
			
			writebuffer = "e " + authorIndex + " " + anotherIndex+" 0\r\n";
			edgeBw.write(writebuffer);
		}
		
		//�ر�д��ߵ��ļ�
		edgeBw.close();

		//���߼��������ϵд���ļ�
		FileWriter vertexOut = new FileWriter (new File("vertex.txt"));
		String labelName = "labelMap_"+project+".txt";
		FileWriter mapOut = new FileWriter(new File(labelName));
		
		BufferedWriter vertexBw = new BufferedWriter(vertexOut);
		BufferedWriter mapBw = new BufferedWriter(mapOut);
		
		//����list
		for(int i=0;i<nodeList.size();i++)
		{
			writebuffer = "v "+i+" "+i+"\r\n";
			vertexBw.write(writebuffer);
			
			writebuffer = i+" "+nodeList.get(i)+"\r\n";
			mapBw.write(writebuffer);
		}
		
		//�ر�д�����ļ�
		vertexBw.close();
		mapBw.close();
		
		//�ر�result set
		rs.close();
	}
}
