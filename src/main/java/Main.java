import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.sql.DriverManager.getConnection;

public class Main {
    private static final String URL_DB = "jdbc:mysql://sql7.freemysqlhosting.net:3306/sql7281012?useUnicode=yes&characterEncoding=UTF-8";
    private static final String USERNAME_DB = "sql7281012";
    private static final String PASSWORD_DB = "VrWWk6FjKA";
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;

    public static void main(String[] args) throws IOException, SQLException {
        List<String> users = readFromFile("src/main/resources/user.txt");
        List<String> genres = readFromFile("src/main/resources/genre.txt");
        List<String> movies = readFromFile("src/main/resources/movie.txt");
        List<String> reviews = readFromFile("src/main/resources/review.txt");
        List<String> posters = readFromFile("src/main/resources/poster.txt");

        connection = getConnection(URL_DB, USERNAME_DB, PASSWORD_DB);
        statement = connection.createStatement();

        insertUser(users);
        insertGenre(genres);
        insertMovies(movies);
        insertReviews(reviews);
        updateMovieWithPoster(posters);

        statement.close();
        connection.close();
    }

    private static List<String> readFromFile(String path) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8" ));
        ArrayList<String> lines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.length() != 0) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                lines.add(line);
            }
        }
        return lines;
    }

    private static void insertUser(List<String> users) throws SQLException {
        String name, email, password;
        for (int i = 0; i < users.size() / 3; i++) {
            name = users.get(i * 3);
            email = users.get(i * 3 + 1);
            password = users.get(i * 3 + 2);
            String insertQuery = "INSERT INTO user(email, nickname, password)" +
                    " VALUES ('" + email + "', '" + name + "', '" + password + "');";
            //System.out.println(insertQuery);
            statement.executeUpdate(insertQuery);
        }
    }

    private static void insertGenre(List<String> genres) throws SQLException {
        String name;
        for (int i = 0; i < genres.size(); i++) {
            name = genres.get(i);
            String insertQuery = "INSERT INTO genre(name) VALUES ('" + name + "');";
            //System.out.println(insertQuery);
            statement.executeUpdate(insertQuery);
        }
    }

    private static void insertMovies(List<String> movies) throws SQLException {
        String nameRussian, nameNative, yearOfRelease, countries, genres, description;
        double rating, price;
        int movieId = 0;

        HashSet<String> countrySet = new HashSet<>();
        for (int i = 0; i < movies.size() / 7; i++) {
            countries = movies.get(i * 7 + 2);
            String[] countryArray = countries.split(", ");
            for (String countryName : countryArray) {
                countrySet.add(countryName);
            }
        }
        insertCountry(countrySet);

        for (int i = 0; i < movies.size() / 7; i++) {
            String[] names = movies.get(i * 7).split("/");
            nameRussian = names[0];
            nameNative = names[1];
            yearOfRelease = movies.get(i * 7 + 1);
            countries = movies.get(i * 7 + 2);
            genres = movies.get(i * 7 + 3);
            description = movies.get(i * 7 + 4);
            rating = new Double(movies.get(i * 7 + 5).substring(7));
            price = new Double(movies.get(i * 7 + 6).substring(6));

            String insertQuery = "INSERT INTO movie(nameRussian, nameNative, yearOfRelease, description, rating, price)" +
                    " VALUES (?, ?, ?, ?, ?, ?);";
            //System.out.println(insertQuery);
            //System.out.println(countries);
            //System.out.println(genres);

            //statement.executeUpdate(insertQuery, Statement.RETURN_GENERATED_KEYS);
            //ResultSet resultSet = statement.getGeneratedKeys();

            preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, nameRussian);
            preparedStatement.setString(2, nameNative);
            preparedStatement.setString(3, yearOfRelease);
            preparedStatement.setString(4, description);
            preparedStatement.setDouble(5, rating);
            preparedStatement.setDouble(6, price);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                movieId = resultSet.getInt(1);
                insertMovieCountry(movieId, countries);
                insertMovieGenre(movieId, genres);
            }
        }


    }

    private static void insertCountry(HashSet<String> countrySet) throws SQLException {
        for (String countryName : countrySet) {
            String insertQuery = "INSERT INTO country(name) VALUES ('" + countryName + "');";
            //System.out.println(insertQuery);
            statement.executeUpdate(insertQuery);
        }
    }

    private static void insertMovieCountry(int movieId, String countries) throws SQLException {
        String[] countryArray = countries.split(", ");
        for (String countryName : countryArray) {
            ResultSet resultSet = statement.executeQuery("SELECT id FROM country WHERE name = '" + countryName + "';");
            if (resultSet.next()) {
                int countryId = resultSet.getInt(1);
                String insertQuery = "INSERT INTO movieCountry(movieId, countryId)" +
                        " VALUES ('" + movieId + "', '" + countryId + "');";
                //System.out.println(insertQuery);
                statement.executeUpdate(insertQuery);
            }
        }
    }

    private static void insertMovieGenre(int movieId, String genres) throws SQLException {
        String[] genreArray = genres.split(", ");
        for (String genreName : genreArray) {
            ResultSet resultSet = statement.executeQuery("SELECT id FROM genre WHERE name = '" + genreName + "';");
            if (resultSet.next()) {
                int genreId = resultSet.getInt(1);
                String insertQuery = "INSERT INTO movieGenre(movieId, genreId)" +
                        " VALUES ('" + movieId + "', '" + genreId + "');";
                statement.executeUpdate(insertQuery);
            }
        }
    }

    private static void updateMovieWithPoster(List<String> posters) throws SQLException {
        for (String line : posters) {
            int index = line.indexOf("https");
            String movieName = line.substring(0, index - 1);
            String url = line.substring(index);
            String updateQuery = "UPDATE movie SET picturePath = '" + url + "' WHERE nameRussian = '" + movieName + "';";
            statement.executeUpdate(updateQuery);
        }
    }

    private static void insertReviews(List<String> reviews) throws SQLException {
        for (int i = 0; i < reviews.size() / 3; i++) {
            String movieName = reviews.get(i * 3);
            String nickName = reviews.get(i * 3 + 1);
            String description = reviews.get(i * 3 + 2);

            int movieId = 0;
            int userId = 0;

            String sql = "SELECT id FROM movie WHERE nameRussian = ?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, movieName);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                movieId = resultSet.getInt(1);
            }
            resultSet = statement.executeQuery("SELECT id FROM user WHERE nickname = '" + nickName + "';");
            if (resultSet.next()) {
                userId = resultSet.getInt(1);
            }

            String insertQuery = "INSERT INTO review(movieId, userId, text)" +
                    " VALUES ('" + movieId + "', '" + userId + "', '" + description + "');";
            System.out.println(insertQuery);
            statement.executeUpdate(insertQuery);
        }
    }
}
