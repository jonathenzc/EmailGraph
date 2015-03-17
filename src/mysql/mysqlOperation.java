package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class mysqlOperation {
	public mysqlOperation(String _user, String _pwd, String _driver, String _dbUrl)
	{
		user = _user;
		pwd = _pwd;
		driver = _driver;
		dbUrl = _dbUrl;
	}
	
	//创建mysql连接
	public void buildSqlConnection() throws SQLException
	{
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			conn = DriverManager.getConnection(dbUrl,user,pwd);
			
			if(!conn.isClosed())
				System.out.println(dbUrl+" connection built");
	}
	
	//关闭mysql连接
	public void closeSqlConnection() throws SQLException
	{
		conn.close();
		
		System.out.println("connection closed");
	}
	
	//从mysql读取sql的结果集
	public ResultSet selectEmaillist(String selectSql) throws Exception
	{
		Statement statement = conn.createStatement();
		
		ResultSet rs = statement.executeQuery(selectSql);
		
		return rs;
	}
	
	//向mysql插入sql
	public void insertAnEdge(String insertSql) throws SQLException
	{
		Statement statement = conn.createStatement();
		
		int result = statement.executeUpdate(insertSql);
	}
	
	//variable:
	private String user;
	private String pwd;
	private String driver;
	private String dbUrl;
	private Connection conn;
}
