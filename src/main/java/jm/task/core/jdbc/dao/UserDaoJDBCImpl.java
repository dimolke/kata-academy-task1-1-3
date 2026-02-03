package jm.task.core.jdbc.dao;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;

import java.awt.desktop.QuitEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDaoJDBCImpl implements UserDao {

    //declare singleton UserDao
    private static UserDao INSTANCE = null;

    @FunctionalInterface
    private interface SQLQuery {
        String getQuery();
    }

    // according to issue#2 of mentor:
    // to change overlap sql statements in methods
    // introduced class enum and used lambda to implement this issue
    private enum Queries {
        // DDL
        CREATE_TABLE(() -> """
                CREATE TABLE IF NOT EXISTS users(
                id BIGINT UNSIGNED
                AUTO_INCREMENT PRIMARY KEY NOT NULL,
                name VARCHAR(100) NULL,
                lastname VARCHAR(100) NULL,
                age TINYINT NULL)
                CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
                """),
        // DDL
        DROP_SQL(() -> """
                DROP TABLE IF EXISTS users
        """),
        //DML
        SAVE_TABLE(() -> """
                    INSERT INTO users(name, 
                                      lastname, 
                                      age) 
                    VALUES (?, ?, ?);
                """),
        //DML
        DELETE_SQL(() -> """
                DELETE FROM users
        """),
        //DML
        FIND_ALL_SQL(() -> """
                           SELECT id,
                           name,
                           lastname,
                           age
                    FROM users
                """),
        //DML
        TRUNCATE_SQL(() -> """
                            TRUNCATE users
        """);

        private final SQLQuery sqlQuery;

        Queries(SQLQuery query) {
            this.sqlQuery = query;
        }

        public String getQuery() {
            return sqlQuery.getQuery();
        }
    }

    private UserDaoJDBCImpl() {
    }

    public void createUsersTable() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = Util.getConnection();

            // disable autocommit mode
            // according to issue#3 from mentor
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.execute(Queries.CREATE_TABLE.getQuery());
            connection.commit();
        } catch (SQLException throwable) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            System.out.println("SQLException: " + throwable.getMessage());
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
    }

    public void dropUsersTable() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = Util.getConnection();
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.execute(Queries.DROP_SQL.getQuery());
            connection.commit();
        } catch (SQLException throwable) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            System.out.println("SQLException: " + throwable.getMessage());
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
    }

    public void saveUser(String name, String lastName, byte age) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = Util.getConnection();
            connection.setAutoCommit(false);

             preparedStatement = connection.
                     prepareStatement(Queries.SAVE_TABLE.getQuery(),
                             Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, lastName);
            preparedStatement.setByte(3, age);

            preparedStatement.executeUpdate();
            connection.commit();

        } catch (SQLException throwable) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            System.out.println("SQLException: " + throwable.getMessage());
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
    }

    public void removeUserById(long id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try { connection = Util.getConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.
                prepareStatement(Queries.DELETE_SQL.getQuery());
            preparedStatement.setLong(1, id);

        } catch (SQLException throwable) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            System.out.println("SQLException: " + throwable.getMessage());
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
    }

    public List<User> getAllUsers() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = Util.getConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.
                    prepareStatement(Queries.FIND_ALL_SQL.getQuery());
            var resultSet = preparedStatement.executeQuery();

            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        } catch (SQLException throwable) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            System.out.println("SQLException: " + throwable.getMessage());
            return Collections.emptyList();
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
    }

    // DDL
    public void cleanUsersTable() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = Util.getConnection();
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.execute(Queries.TRUNCATE_SQL.getQuery());

        } catch (SQLException throwable) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            System.out.println("SQLException: " + throwable.getMessage());
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
    }

    private static User buildUser(ResultSet resultSet) throws SQLException {
        User user = new User(
                resultSet.getString("name"),
                resultSet.getString("lastname"),
                resultSet.getByte("age")
        );
        user.setId(resultSet.getLong("id"));
        return user;
    }


    // create singleton UserDao
    public static UserDao getInstance() {
        if (INSTANCE == null) {
            synchronized (UserDaoJDBCImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserDaoJDBCImpl();
                }
            }
        }
        return INSTANCE;
    }
}
