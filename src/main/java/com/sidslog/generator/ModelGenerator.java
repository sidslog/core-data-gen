package com.sidslog.generator;

import java.io.*;
import java.sql.Statement;
import java.sql.*;
import java.util.*;

import org.sqlite.JDBC;

/**
 * Created by sidslog on 14.04.14.
 */
public class ModelGenerator {

    public static List<EntityClass> testClasses() {
        List<EntityClass> temp = new ArrayList<EntityClass>();

        EntityClass base = new EntityClass("Base", null);
        base.getAttributes().add(new EntityAttribute("active", EntityAttributeType.EA_BOOLEAN, null));
        base.getAttributes().add(new EntityAttribute("createdAt", EntityAttributeType.EA_DATE, null));
        temp.add(base);


        EntityClass aaa = new EntityClass("AAA", base);
        aaa.getAttributes().add(new EntityAttribute("name", EntityAttributeType.EA_STRING, null));
        temp.add(aaa);

        EntityClass bbb = new EntityClass("BBB", base);
        bbb.getAttributes().add(new EntityAttribute("name", EntityAttributeType.EA_STRING, null));
        temp.add(bbb);

        EntityClass user = new EntityClass("User", null);
        user.getAttributes().add(new EntityAttribute("name", EntityAttributeType.EA_STRING, null));
        temp.add(user);

        EntityClass card = new EntityClass("Card", base);
        card.getAttributes().add(new EntityAttribute("name", EntityAttributeType.EA_STRING, null));
        card.getAttributes().add(new EntityAttribute("user", EntityAttributeType.EA_REF, user));
        temp.add(card);

        EntityClass spec = new EntityClass("Spec", null);
        spec.getAttributes().add(new EntityAttribute("name", EntityAttributeType.EA_STRING, null));
        temp.add(spec);

        return temp;
    }


    public static void generate(List<EntityClass> classes) throws Throwable {
        List<EntityClass> baseClasses = new ArrayList<EntityClass>();
        for (EntityClass entityClass : classes) {
            if (entityClass.getParent() == null) {
                baseClasses.add(entityClass);
            }
        }

        Collections.sort(baseClasses, new Comparator<EntityClass>() {
            @Override
            public int compare(EntityClass entityClass, EntityClass entityClass2) {
                int result = - entityClass.getChildren().size() + entityClass2.getChildren().size();
                if (result != 0) {
                    return result;
                } else {
                    return entityClass.getName().compareTo(entityClass2.getName());
                }
            }
        });

        List<String> ddls = new ArrayList<String>();

        for (EntityClass entityClass : baseClasses) {
//            String drop = dropForClass(entityClass);
            String ddl = ddlForClass(entityClass);
//            ddls.add(drop);
            ddls.add(ddl);
        }

        for (EntityClass entityClass : baseClasses) {
            if (entityClass.getChildren().size() > 0) {
                ddls.add(indexForBaseClass(entityClass));
            }
        }

        for (EntityClass entityClass : baseClasses) {
            for (EntityAttribute attribute : entityClass.getAttributes()) {
                if (attribute.getType() == EntityAttributeType.EA_REF) {
                    ddls.add(indexForRef(attribute, entityClass));
                }
            }
        }

        int i = 1;
        for (EntityClass entityClass : baseClasses) {
            entityClass.setIndex(i ++);
            ddls.add(primaryKey(entityClass));
            for (EntityClass child : entityClass.getChildren()) {
                child.setIndex(i ++);
                ddls.add(primaryKey(child));
            }
        }


        for (String ddl : ddls) {
            System.out.println("ddl = " + ddl);
        }

        String tempFile = "sample.db";
        File fileTemp = new File(tempFile);
        if (fileTemp.exists()){
            fileTemp.delete();
        }


        Class.forName("org.sqlite.JDBC");

        Connection connection = null;
        final Properties p = new Properties();
        p.setProperty("journal_mode", "DELETE");


        connection = DriverManager.getConnection("jdbc:sqlite:sample.db", p);
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.

        executeCommon(statement);
        prepareMetadata(connection);

        executeUpdates(statement, ddls);
    }

    public static void executeUpdates(Statement statement, List<String> ddls) throws SQLException {

        for (String ddl : ddls) {
            statement.executeUpdate(ddl);
        }

    }

    public static void executeCommon(Statement statement) throws SQLException {
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
    }

    public static void prepareMetadata(Connection connection) throws Throwable {
        RandomAccessFile f = new RandomAccessFile("data.plist", "r");
        byte[] b = new byte[(int)f.length()];
        f.read(b);

        PreparedStatement stmt  = connection.prepareStatement("INSERT INTO Z_METADATA (Z_VERSION, Z_UUID, Z_PLIST) VALUES (1,\"D98AFD84-4648-4378-8841-AADBFAFD7B56\",?1);");
        stmt.setBytes(1, b);
        stmt.executeUpdate();
    }

    public static String dropForClass(EntityClass entityClass) {
        return "drop table if exists Z_" + entityClass.getName().toUpperCase();
    }

    public static String primaryKey(EntityClass entityClass) {
//        statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (1,\"Base\",0, 0);");
        StringBuffer buffer = new StringBuffer("");
        buffer.append("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (")
                .append(entityClass.getIndex())
                .append(", \"")
                .append(entityClass.getName())
                .append("\",")
                .append(entityClass.getParent() != null ? entityClass.getParent().getIndex() : 0)
                .append(", ")
                .append(0)
                .append(");");
        return buffer.toString();

    }

    public static String indexForRef(EntityAttribute attribute, EntityClass base) {
//        statement.executeUpdate("CREATE INDEX ZBASE_ZUSER_INDEX ON ZBASE (ZUSER);");

        StringBuffer buffer = new StringBuffer("");
        buffer.append("CREATE INDEX Z")
                .append(base.getName().toUpperCase())
                .append("_Z")
                .append(attribute.getRef().getName().toUpperCase())
                .append("_INDEX ON Z")
                .append(base.getName().toUpperCase())
                .append(" (Z")
                .append(attribute.getRef().getName().toUpperCase())
                .append(");");
        return buffer.toString();
    }

    public static String indexForBaseClass(EntityClass entityClass) {

//        statement.executeUpdate("CREATE INDEX ZBASE_Z_ENT_INDEX ON ZBASE (Z_ENT);");

        if (entityClass.getChildren().size() > 0) {
            StringBuffer buffer = new StringBuffer("");
            buffer.append("CREATE INDEX Z")
                    .append(entityClass.getName().toUpperCase())
                    .append("_Z_ENT_INDEX ON Z")
                    .append(entityClass.getName().toUpperCase())
                    .append(" (Z_ENT);");
            return buffer.toString();
        }
        return null;
    }

    public static String ddlForClass(EntityClass entityClass) {

        Collections.sort(entityClass.getChildren(), new Comparator<EntityClass>() {
            @Override
            public int compare(EntityClass entityClass, EntityClass entityClass2) {
                return entityClass.getName().compareTo(entityClass2.getName());
            }
        });

        for (EntityClass child : entityClass.getChildren()) {
            for (EntityAttribute attribute : child.getAttributes()) {
                int countInParent = 0;
                boolean found = false;

                do {
                    found = false;
                    for (EntityAttribute parentAttribute : entityClass.getAttributes()) {
                        String parentName = countInParent > 0
                                ? attribute.getName() + countInParent : attribute.getName();
                        if (parentName.equals(parentAttribute.getName())) {
                            countInParent ++;
                            found = true;
                            break;
                        }
                    }
                } while (found);

                if (countInParent > 0) {
                    attribute.setName(attribute.getName() + countInParent);
                }

                entityClass.getAttributes().add(attribute);
            }
        }


        StringBuffer buffer = new StringBuffer("");
        buffer.append("CREATE TABLE Z")
                .append(entityClass.getName().toUpperCase())
                .append("(\n")
                .append("  Z_PK integer PRIMARY KEY,\n")
                .append("  Z_ENT integer,\n")
                .append("  Z_OPT integer");

        for (EntityAttribute attribute : entityClass.getAttributes()) {
            appendAttribute(buffer, attribute);
        }

        buffer.append("\n);");
        return buffer.toString();
    }

    public static void appendAttribute(StringBuffer buffer, EntityAttribute attribute) {
        buffer.append(",\n  ")
                .append("Z")
                .append(attribute.getName().toUpperCase())
                .append(" ");

        String type = "varchar";
        switch (attribute.getType()) {
            case EA_BOOLEAN:
            case EA_REF:
                type = "integer";
                break;
            case EA_DATE:
                type = "timestamp";
                break;
            case EA_STRING:
                type = "varchar";
                break;
        }
        buffer.append(type);
    }

    public static void dumpPlist() throws Throwable {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:data.db");

        ResultSet rs = connection.createStatement().executeQuery("SELECT Z_PLIST FROM Z_METADATA");
        while (rs.next()) {
            byte[] bytes = rs.getBytes("Z_PLIST");

            FileOutputStream fos = new FileOutputStream("data4.plist");
            fos.write(bytes);
            fos.flush();
        }

    }

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
            statement.executeUpdate("drop table if exists ZBASE");
            statement.executeUpdate("drop table if exists ZSPEC");

            statement.executeUpdate("drop index if exists ZBASE_ZUSER_INDEX");
            statement.executeUpdate("drop index if exists ZBASE_Z_ENT_INDEX");

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

            statement.executeUpdate("CREATE TABLE ZBASE (\n" +
                    "  Z_PK integer PRIMARY KEY,\n" +
                    "  Z_ENT integer,\n" +
                    "  Z_OPT integer,\n" +
                    "  ZACTIVE integer,\n" +
                    "  ZUSER integer,\n" +
                    "  ZCREATEDAT timestamp,\n" +
                    "  ZNAME varchar\n" +
                    ");");

            statement.executeUpdate("CREATE INDEX ZBASE_Z_ENT_INDEX ON ZBASE (Z_ENT);");
            statement.executeUpdate("CREATE INDEX ZBASE_ZUSER_INDEX ON ZBASE (ZUSER);");


            statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (1,\"Base\",0, 0);");
            statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (2,\"Card\",1, 0);");
            statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (3,\"Spec\",0, 0);");
            statement.executeUpdate("INSERT INTO Z_PRIMARYKEY (Z_ENT, Z_NAME, Z_SUPER, Z_MAX) VALUES (4,\"User\",0, 0);");
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


            connection = DriverManager.getConnection("jdbc:sqlite:data.db");

            ResultSet rs = connection.createStatement().executeQuery("SELECT Z_PLIST FROM Z_METADATA");
            while (rs.next()) {
                byte[] bytes = rs.getBytes("Z_PLIST");

                FileOutputStream fos = new FileOutputStream("data3.plist");
                fos.write(bytes);
                fos.flush();
            }


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
