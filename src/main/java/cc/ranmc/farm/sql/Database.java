package cc.ranmc.farm.sql;

import cc.ranmc.farm.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cc.ranmc.utils.BasicUtil.print;

public class Database {

    private Connection connection;

    public Database() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:./" +
                            Main.getInstance().getDataFolder().getPath() + "/data",
                    "sa", "");
        } catch (Exception e) {
            print("数据库错误" + e.getMessage());
        }
        createTable();
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (Exception e) {
            print("数据库错误" + e.getMessage());
        }

    }

    /**
     * 新增数据库表
     */
    public void createTable() {
        runCommand("CREATE TABLE PLAYER " +
                "(ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " PLAYER TEXT NOT NULL," +
                " OPEN TEXT," +
                " CARROT TEXT," +
                " WHEAT TEXT," +
                " WHEAT_SEEDS TEXT," +
                " BEETROOT TEXT," +
                " BEETROOT_SEEDS TEXT," +
                " NETHER_WART TEXT," +
                " PUMPKIN TEXT," +
                " MELON TEXT," +
                " CACTUS TEXT," +
                " BAMBOO TEXT," +
                " SUGAR_CANE TEXT," +
                " POTATO TEXT)");
    }

    /**
     * 新增数据库
     * @param table 表
     * @param map 内容
     */
    public int insert(String table, Map<String, String> map) {
        StringBuilder name = new StringBuilder();
        StringBuilder value = new StringBuilder();
        for (String key : map.keySet()) {
            name.append(key);
            name.append(",");
            value.append(map.get(key));
            value.append("','");
        }
        if (!name.isEmpty()) name.deleteCharAt(name.length() - 1);
        if (value.length() >= 3) value.delete(value.length() - 3, value.length());
        return runCommandGetId("INSERT INTO " + table.toUpperCase() + " ("+name+") VALUES ('" + value + "');");
    }

    /**
     * 查询表数据
     * @param table 表
     * @param filter 数据
     * @return 数据
     */
    public Map<String, String> selectMap(String table, SQLFilter filter) {
        return queryMap("SELECT * FROM " + table.toUpperCase() + filter.getResult());
    }

    public Map<String, String> selectMap(String table) {
        return queryMap("SELECT * FROM " + table.toUpperCase());
    }

    public List<Map<String, String>> selectList(String table, SQLFilter filter) {
        return queryList("SELECT * FROM " + table.toUpperCase() + filter.getResult());
    }

    public List<Map<String, String>> selectList(String table) {
        return queryList("SELECT * FROM " + table.toUpperCase());
    }

    public int selectCount(String table) {
        return Integer.parseInt(queryMap("SELECT COUNT(*) FROM " + table)
                .getOrDefault("COUNT(*)", "0"));
    }

    public int selectCount(String table, SQLFilter filter) {
        return Integer.parseInt(queryMap("SELECT COUNT(*) FROM " + table.toUpperCase() + filter.getResult())
                .getOrDefault("COUNT(*)", "0"));
    }

    /**
     * 分析数据
     * @param command 命令
     * @return 数据
     */
    protected Map<String, String> queryMap(String command) {
        Map<String, String> map = new HashMap<>();
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(command);
            if (rs.next()) {
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        map.put(md.getColumnName(i), rs.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            print("数据库错误" + e.getMessage() + "\n" + command);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException e) {
                print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
        return map;
    }

    protected List<Map<String, String>> queryList(String command) {
        List<Map<String, String>> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(command);
            while (rs.next()) {
                if (!rs.isClosed()) {
                    Map<String, String> map = new HashMap<>();
                    ResultSetMetaData md = rs.getMetaData();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        if (rs.getString(i) != null) {
                            map.put(md.getColumnName(i), rs.getString(i));
                        }
                    }
                    list.add(map);
                }
            }
        } catch (Exception e) {
            print("数据库错误" + e.getMessage() + "\n" + command);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException e) {
                print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
        return list;
    }

    /**
     * 更新表数据
     * @param table 表
     * @param filter 数据
     */
    public void update(String table, SQLFilter filter) {
        runCommand("UPDATE " + table.toUpperCase() + filter.getResult());
    }

    /**
     * 删除表数据
     * @param table 表
     * @param id 编号
     */
    public void delete(String table, String id) {
        runCommand("DELETE FROM " + table.toUpperCase() + " WHERE ID = " + id);
    }

    public void delete(String table, SQLFilter filter) {
        runCommand("DELETE FROM " + table.toUpperCase() + filter.getResult());
    }

    /**
     * 执行数据库指令
     * @param command 命令
     */
    public void runCommand(String command) {
        try {
            connection.createStatement().executeUpdate(command);
        } catch (SQLException e) {
            if (!command.contains("CREATE TABLE")) {
                print("数据库错误" + e.getMessage() + "\n" + command);
            }
        }
    }

    private int runCommandGetId(String command) {
        try {
            PreparedStatement statement = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            print("数据库错误" + e.getMessage() + "\n" + command);
        }
        return -1;
    }
}
