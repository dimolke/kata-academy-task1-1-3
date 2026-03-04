package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;
import org.hibernate.HibernateException;

import java.util.List;

public class UserDaoHibernateImpl implements UserDao {

    @FunctionalInterface
    private interface SQLQuery {
        String getQuery();
    }

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

        private final UserDaoHibernateImpl.SQLQuery sqlQuery;

        Queries(UserDaoHibernateImpl.SQLQuery query) {
            this.sqlQuery = query;
        }

        public String getQuery() {
            return sqlQuery.getQuery();
        }
    }

    public UserDaoHibernateImpl() {
    }

    @Override
    public void createUsersTable() {
        try( var session = Util.getSessionFactory().openSession();){
            session.beginTransaction();

            session.createNativeQuery(Queries.CREATE_TABLE.getQuery())
                    .addEntity(User.class)
                    .executeUpdate();

            session.getTransaction().commit();
        } catch (HibernateException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void dropUsersTable() {
        try (var session = Util.getSessionFactory()
                    .openSession()) {
            session.beginTransaction();
            session.createNativeQuery(Queries.DROP_SQL.getQuery())
                    .executeUpdate();

            session.getTransaction().commit();
        } catch (HibernateException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        try (var session = Util.getSessionFactory()
                    .openSession()) {
            session.beginTransaction();

            User user = new User(name,lastName,age);
            session.persist(user);

            session.getTransaction().commit();

        } catch (HibernateException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void removeUserById(long id) {
        try(var session = Util.getSessionFactory()
                .openSession()) {
            session.beginTransaction();

            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
            }

            session.getTransaction().commit();

        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (var session = Util.getSessionFactory()
                .openSession()) {
            session.beginTransaction();

            List<User> users = session.
                    createNativeQuery(Queries.FIND_ALL_SQL.getQuery(), User.class)
                    .list();

            session.getTransaction().commit();
            return users;

        } catch (HibernateException e) {
            throw new DaoException(e);
        }

    }

    @Override
    public void cleanUsersTable() {
        try(var session = Util.getSessionFactory()
                .openSession()) {
         session.beginTransaction();

         session.createNativeQuery(Queries.DELETE_SQL.getQuery())
                 .executeUpdate();

         session.getTransaction().commit();

        } catch (HibernateException e) {
            throw new DaoException(e);
        }
    }
}
