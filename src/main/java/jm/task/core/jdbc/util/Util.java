/**
 * Используется пул соединений, паттерн Connection pool
 */

package jm.task.core.jdbc.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Util {
    // реализуйте настройку соединения с БД
    private static final String URL = "db.url";
    private static final String USER = "db.username";
    private static final String PASSWORD = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool_size";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnections;

    private Util() {
    }

    static {
        loadDriver();
        initConnectionPool();
    }


    public static Connection open() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL),
                    PropertiesUtil.get(USER),
                    PropertiesUtil.get(PASSWORD)
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static Connection getConnection() {
        try{
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static void initConnectionPool() {
        String poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
        var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);

        // инициализируем pool с правильным размером
        pool = new ArrayBlockingQueue<>(size);

        // инициализируем sourceConnections
        sourceConnections = new ArrayList<>(size);

        // добавляем все соединения в pool
        for (int i = 0; i < size; i++) {
            var connection = open();
            var proxyConnection = (Connection)
                    Proxy.newProxyInstance(Util.class.getClassLoader(),
                            new Class[]{Connection.class},
                            (proxy, method, args) -> method.getName().equals("close")
                            ? pool.add((Connection) proxy)
                            : method.invoke(connection, args));
            pool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }

    public static void closePool() {
        try {
            for (Connection sourceConnection : sourceConnections) {
                sourceConnection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

