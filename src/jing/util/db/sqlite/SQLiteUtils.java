package jing.util.db.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jing.util.fields.DBFields;

public class SQLiteUtils {
	private static SQLiteUtils instance = null;
	private String dbPath = null;
	private Connection conn = null;
	private boolean isConnected = false;

	private SQLiteUtils(){
		instance = this;
	}

	public static SQLiteUtils getInstance(){
		if (instance == null) {
			new SQLiteUtils();
		}
		return instance;
	}
	
	public void setDBPath(String path){
		dbPath = path;
	}
	

	public void connect() throws Exception {
		if (dbPath == null) {
			System.out.println("DB path not set!");
			return;
		}
		if(this.isConnected){
			return;
		}
		System.out.println("Connecting sqlite db: " + dbPath);
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite://" + dbPath);
		this.isConnected = true;
	}

	public synchronized void createDatabase(String databaseName)
			throws Exception {
		if (databaseName != null) {
			Statement stat = conn.createStatement();
			stat.executeUpdate("create table " + databaseName
					+ "(name varchar(20), salary int);");
			stat.close();
			stat = null;
		} else {
			throw new Exception("Create database error: databaseName not set.");
		}
	}

	public synchronized void insert(Map<String, String> record)
			throws Exception {
		Statement stat = conn.createStatement();
		String tableName = record.get(DBFields.TABLE);
		if (tableName == null) {
			throw new Exception("Table name not set.");
		}
		record.remove(DBFields.TABLE);
		if (record.size() < 1) {
			throw new Exception("No record need to insert");
		}
		Set<String> recordName = record.keySet();
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(tableName);
		sb.append("(");
		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (String name : recordName) {
			columns.append("'");
			columns.append(name);
			columns.append("',");

			values.append("'");
			values.append(record.get(name));
			values.append("',");
		}
		columns.deleteCharAt(columns.length() - 1);
		values.deleteCharAt(values.length() - 1);
		sb.append(columns);
		sb.append(") values (");
		sb.append(values);
		sb.append(");");

		stat.executeUpdate(sb.toString());
		stat.close();
		stat = null;
	}

	public synchronized void update(Map<String, Object> record)
			throws Exception {
		if(!record.containsKey(DBFields.TABLE)){
			throw new Exception("Table not set.");
		}
		String tableName = record.get(DBFields.TABLE).toString();
		
		if(!record.containsKey(DBFields.UPDATE_FIELDS)){
			throw new Exception("Update fields not set");
		}
		
		Map<String, String> updateFieldMap = (Map<String, String>)record.get(DBFields.UPDATE_FIELDS);
		if(updateFieldMap.size() < 1){
			throw new Exception("Update fields not set");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(tableName);
		sb.append(" set ");
		StringBuilder updateFields = new StringBuilder();
		Set<String> updateFieldSet = updateFieldMap.keySet();
		for(String field : updateFieldSet) {
			updateFields.append(field);
			updateFields.append("='");
			updateFields.append(updateFieldMap.get(field));
			updateFields.append("',");
		}
		updateFields.delete(updateFields.length() - 2, updateFields.length() - 1);
		sb.append(updateFields.toString());
		
		this.appendWhereCondition(record, sb);
		sb.append(";");
		
		Statement stat = conn.createStatement();
		stat.executeUpdate(sb.toString());
		stat.close();
		stat = null;
		
	}
	
	public synchronized List<Map<String, String>> query(Map<String, Object> queryData) throws Exception{
		if(!queryData.containsKey(DBFields.TABLE)){
			throw new Exception("Table not set.");
		}
		String tableName = queryData.get(DBFields.TABLE).toString();
		
		StringBuilder returnFields = new StringBuilder();
		if(!queryData.containsKey(DBFields.RETURN_FIELDS)){
			returnFields.append("*");
		} else {
		ArrayList<String> returnFieldList = (ArrayList<String>)queryData.get(DBFields.RETURN_FIELDS);
			for(String fields : returnFieldList){
				returnFields.append(fields);
				returnFields.append(",");
			}
			returnFields.deleteCharAt(returnFields.length() - 1);
		}
		
		Statement stat = conn.createStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append(returnFields);
		sb.append(" from ");
		sb.append(tableName);
		
		this.appendWhereCondition(queryData, sb);
		sb.append(";");
		
		List<Map<String, String>> returnList = new ArrayList<Map<String,String>>();
		Map<String, String> returnValues = null;
		ResultSet rs = stat.executeQuery(sb.toString());
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		int i = 0;
        while (rs.next()) {
        	returnValues = new HashMap<String, String>();
        	for(i=1;i<=columnCount;i++){
        		returnValues.put(metaData.getColumnName(i), rs.getString(i));
        	} 
        	returnList.add(returnValues);
        }  
        rs.close();
        stat.close();
        rs = null;
        stat = null;
        return returnList;
	}
	
	public boolean isConnected(){
		return this.isConnected;
	}
	
	public void close(){
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				instance = null;
			}
		}
		conn = null;
		this.isConnected = false;
	}
	
	private void appendWhereCondition(Map<String, Object> queryData, StringBuilder sb){
		if(queryData.containsKey(DBFields.QUERY_CONDITION)){
			StringBuilder conditions = new StringBuilder();
			Map<String, String> conditionMap = (Map<String, String>)queryData.get(DBFields.QUERY_CONDITION);
			Set<String> conditionName = conditionMap.keySet();
			for(String name : conditionName){
				conditions.append(name);
				conditions.append("='");
				conditions.append(conditionMap.get(name));
				conditions.append("'");
			}
			sb.append(" where ");
			sb.append(conditions);
		}
	}
}
