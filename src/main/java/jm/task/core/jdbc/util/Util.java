/**
 * Используется пул соединений, паттерн Connection pool

 * History version:
 * 2.0.0 added hibernate to use connection to DBMS MySQL
 * 1.0.0 - first release
 */

package jm.task.core.jdbc.util;

import jm.task.core.jdbc.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;


import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Properties;

public class Util {
    // реализуйте настройку соединения с БД
    private static final String URL = "db.url";
    private static final String USER = "db.username";
    private static final String PASSWORD = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool_size";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnections;

    private static final String DIALECT = "hibernate.dialect";
    private static final String DRIVER = "hibernate.driver";

    private static SessionFactory sessionFactory;

    private Util() {
    }


    // =================== Configure for JDBC ===================
    //                  deprecated since task 1.1.4
    //                  forRemoval( value = "false")


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
        try {
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


//    =================== Configure for Hibernate ===================
//       --------------------- for  task 1.1.4 -------------------

    private static void initHibernate() {

        try {
            Properties properties = new Properties();

            properties.put(Environment.DRIVER, PropertiesUtil.get(DRIVER));
            properties.put(Environment.URL,PropertiesUtil.get(URL));
            properties.put(Environment.USER,PropertiesUtil.get(USER));
            properties.put(Environment.PASS,PropertiesUtil.get(PASSWORD));
            properties.put(Environment.DIALECT,PropertiesUtil.get(DIALECT));

             //extra settings for hibernate
             //Дополнительное логирование запросов
            properties.put(Environment.SHOW_SQL, "true");

//            properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

            // Автоматическое создание схемы данных. Возможные значения: validate, update, create, create-drope, none
            // ВНИМАНИЕ!!! Выбросит исключение если  будет стоять значение validate, а таблица удалена
            properties.put(Environment.HBM2DDL_AUTO,"create");

            // connection pool settings for Hibernate
            properties.put(Environment.POOL_SIZE, PropertiesUtil.get(POOL_SIZE_KEY));

            Configuration configuration = new Configuration()
                    .setProperties(properties);

            // add Entity classes to know Hibernate about them
//            configuration.addAnnotatedClass(jm.task.core.jdbc.model.User.class);
            configuration.addAnnotatedClass(User.class);

            sessionFactory = configuration.buildSessionFactory();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Hibernate", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            initHibernate();
        }
        return sessionFactory;
    }

    public static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

}

