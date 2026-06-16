import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        Courier courier = new Courier("shogun", "risingSun");
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

    @Test
    @DisplayName("Проверка возникновения ошибки при попытке регистрации без обязательных полей")
    public void checkCourierCreationErrorRequiredFieldsTest() {
        Courier courierOnlyLogin = new Courier("shogun", "");
        Courier courierOnlyPassword = new Courier("", "risingSun");
        Courier courierIsEmpty = new Courier("", "");

        request.body(courierOnlyLogin)
                .when()
                .post("/api/v1/courier")
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
        request.body(courierOnlyPassword)
                .when()
                .post("/api/v1/courier")
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
        request.body(courierIsEmpty)
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

    @Test
    @DisplayName("Проверка возникновения ошибки при попытке залогина с неизвестными учетными данными")
    public void checkCourierErrorLoginTest(){
        String login = "shogun_" + System.currentTimeMillis();
        Courier courier = new Courier(login, "risingSun");
        Courier wrongLogin = new Courier("WrongLogin","risingSun");
        Courier wrongPassword = new Courier(login,"WrongPassword");
        request.body(courier)
                .post("/api/v1/courier");
        request.body(wrongLogin)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
        request.body(wrongPassword)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
        courierId =
                request.body(courier)
                        .post("/api/v1/courier/login")
                        .then().extract().body().path("id");

    }

    @Test
    @DisplayName("Проверка возникновения ошибки при попытке залогина без обязательных полей")
    public void checkLoginRequiredFieldsTest(){
        String login = "shogun_" + System.currentTimeMillis();
        Courier courier = new Courier(login, "risingSun");
        Courier courierOnlyLogin = new Courier(login, "");
        Courier courierOnlyPassword = new Courier("", "risingSun");
        Courier courierIsEmpty = new Courier("", "");
        request.body(courier)
                .post("/api/v1/courier");
        request.body(courierOnlyLogin)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
        request.body(courierOnlyPassword)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
        request.body(courierIsEmpty)
                .when()
                .post("/api/v1/courier/login")
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
        courierId =
                request.body(courier)
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


    @AfterEach
    void cleanup() {
        if (courierId > 0) {
            request.delete("/api/v1/courier/{id}", courierId);
        }

    }
}
