import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CourierAccountTests {
    private RequestSpecification request;
    private int courierId;

    @BeforeEach
    public void SetUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        request = given().header("Content-type", "application/json");
    }

    @Test
    @DisplayName("Проверка успешной регистрации аккаунта курьера")
    public void checkFirstCourierSuccessfulCreationTest() {
        String login = "shogun_" + System.currentTimeMillis();
        Courier courier = new Courier(login, "risingSun");
        request.body(courier)
                .when()
                .post("/api/v1/courier")
                .then().statusCode(201)
                .body("ok", equalTo(true));
        courierId =
                request.body(courier)
                        .post("/api/v1/courier/login")
                        .then().extract().body().path("id");

    }

    @Test
    @DisplayName("Проверка возникновения ошибки при попытке регистрации курьера с существующим логином")
    public void checkErrorRepeatCourierCreationTest() {
        String login = "shogun_" + System.currentTimeMillis();
        Courier courier = new Courier(login, "risingSun");
        request.body(courier)
                .post("/api/v1/courier");
        request.body(courier)
                .when()
                .post("/api/v1/courier")
                .then().statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
        courierId =
                request.body(courier)
                        .post("/api/v1/courier/login")
                        .then().extract().body().path("id");
    }

    @ParameterizedTest
    @MethodSource("provideEmptyFieldTestCases")
    @DisplayName("Проверка возникновения ошибки при попытке регистрации без обязательных полей")
    public void checkCourierCreationErrorRequiredFieldsTest(String caseName, String login, String password) {
        Courier courier = new Courier();
        courier.setLogin(login);
        courier.setPassword(password);
        request.body(courier)
                .when()
                .post("/api/v1/courier")
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Проверка успешного залогина")
    public void checkCourierSuccessfulLoginTest() {
        String login = "shogun_" + System.currentTimeMillis();
        Courier courier = new Courier(login, "risingSun");
        request.body(courier)
                .post("/api/v1/courier");
        request.body(courier)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(200);
        courierId =
                request.body(courier)
                        .post("/api/v1/courier/login")
                        .then().extract().body().path("id");
        MatcherAssert.assertThat("id", notNullValue());

    }

    @ParameterizedTest
    @MethodSource("provideUnknownCredentials")
    @DisplayName("Проверка возникновения ошибки при попытке залогина с неизвестными учетными данными")
    public void checkCourierErrorLoginTest(String caseName, String login, String password) {
        Courier createdCourier = new Courier("shogun", "risingSun");
        Courier courier = new Courier();
        courier.setLogin(login);
        courier.setPassword(password);
        request.body(createdCourier)
                .post("/api/v1/courier");
        request.body(courier)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
        courierId =
                request.body(createdCourier)
                        .post("/api/v1/courier/login")
                        .then().extract().body().path("id");

    }

    @ParameterizedTest
    @MethodSource("provideEmptyFieldTestCases")
    @DisplayName("Проверка возникновения ошибки при попытке залогина без обязательных полей")
    public void checkLoginRequiredFieldsTest(String caseName, String login, String password) {
        Courier createdCourier = new Courier("greatShogun", "risingSun");
        Courier courier = new Courier();
        courier.setLogin(login);
        courier.setPassword(password);
        request.body(createdCourier)
                .post("/api/v1/courier");
        request.body(courier)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
        courierId =
                request.body(createdCourier)
                        .post("/api/v1/courier/login")
                        .then().extract().body().path("id");

    }


    @Test
    @DisplayName("Проверка удаления аккаунта курьера")
    public void checkCourierDeleteTest() {
        String login = "shogun_" + System.currentTimeMillis();
        Courier courier = new Courier(login, "risingSun");
        request.body(courier)
                .post("/api/v1/courier");
        int courierId =
                request.body(courier)
                        .post("/api/v1/courier/login")
                        .then().extract().body().path("id");
        request.delete("/api/v1/courier/{courierId}", courierId)
                .then().statusCode(200)
                .body("ok", equalTo(true));
    }

    static Stream<Arguments> provideEmptyFieldTestCases() {
        return Stream.of(
                Arguments.of("Кейс без пароля", "greatShogun", ""),
                Arguments.of("Кейс без логина", "", "risingSun"),
                Arguments.of("Кейс с пустыми полями", "", "")
        );
    }

    static Stream<Arguments> provideUnknownCredentials() {
        return Stream.of(
                Arguments.of("Кейс с неверным логином", "wrongLogin", "risingSun"),
                Arguments.of("Кейс с неверным паролем", "shogun", "wrongPassword")
        );
    }


    @AfterEach
    void cleanup() {
        if (courierId > 0) {
            request.delete("/api/v1/courier/{id}", courierId);
        }

    }
}
