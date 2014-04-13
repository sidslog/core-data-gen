package com.sidslog;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;

import com.sidslog.generator.ModelGenerator;
import org.sqlite.JDBC;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Throwable
    {
        ModelGenerator.generate();
    }
}
