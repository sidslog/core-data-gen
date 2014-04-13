package com.sidslog.generator;

import java.io.*;
import java.sql.Statement;
import java.sql.*;
import java.util.Properties;

import com.sun.tools.javac.util.Paths;
import org.sqlite.JDBC;

/**
 * Created by sidslog on 14.04.14.
 */
public class ModelGenerator {
    public static void generate() throws Throwable {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        Connection connection = null;
        Connection connection2 = null;
        try
        {
            // create a database connection
            final Properties p = new Properties();
            p.setProperty("journal_mode", "DELETE");


            connection = DriverManager.getConnection("jdbc:sqlite:sample.db", p);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists Z_METADATA");
            statement.executeUpdate("drop table if exists Z_PRIMARYKEY");
            statement.executeUpdate("drop table if exists ZUSER");
            statement.executeUpdate("drop table if exists ZCARD");
            statement.executeUpdate("drop index if exists ZCARD_ZUSER_INDEX");

            statement.executeUpdate("CREATE TABLE Z_METADATA (\n" +
                    "  Z_VERSION integer PRIMARY KEY,\n" +
                    "  Z_UUID varchar(255),\n" +
                    "  Z_PLIST blob\n" +
                    ");");

            statement.executeUpdate("CREATE TABLE Z_PRIMARYKEY (\n" +
                    "  Z_ENT integer PRIMARY KEY,\n" +
                    "  Z_NAME varchar,\n" +
                    "  Z_SUPER integer,\n" +
                    "  Z_MAX integer\n" +
                    ");");

            statement.executeUpdate("CREATE TABLE ZUSER (\n" +
                    "  Z_PK integer PRIMARY KEY,\n" +
                    "  Z_ENT integer,\n" +
                    "  Z_OPT integer,\n" +
                    "  ZNAME varchar\n" +
                    ");");

            statement.executeUpdate("CREATE TABLE ZSPEC(\n" +
                    "  Z_PK integer PRIMARY KEY,\n" +
                    "  Z_ENT integer,\n" +
                    "  Z_OPT integer,\n" +
                    "  ZNAME varchar\n" +
                    ");");

            statement.executeUpdate("CREATE TABLE ZCARD (\n" +
                    "  Z_PK integer PRIMARY KEY,\n" +
                    "  Z_ENT integer,\n" +
                    "  Z_OPT integer,\n" +
                    "  ZUSER integer,\n" +
                    "  ZNAME varchar\n" +
                    ");");

            statement.executeUpdate("CREATE INDEX ZCARD_ZUSER_INDEX ON ZCARD (ZUSER);");

            statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (1,\"Card\",0, 0);");
            statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (2,\"User\",0, 0);");
            statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (3,\"Spec\",0, 0);");

            RandomAccessFile f = new RandomAccessFile("data.plist", "r");
            byte[] b = new byte[(int)f.length()];
            f.read(b);

            PreparedStatement stmt  = connection.prepareStatement("INSERT INTO Z_METADATA (Z_VERSION, Z_UUID, Z_PLIST) VALUES (1,\"D98AFD84-4648-4378-8841-AADBFAFD7B56\",?1);");
            stmt.setBytes(1, b);
            stmt.executeUpdate();
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                System.err.println(e);
            }
        }

    }
}
