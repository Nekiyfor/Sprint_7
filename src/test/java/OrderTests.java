import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.notNullValue;

public class OrderTests {
    private RequestSpecification request;

    @BeforeEach
    public void SetUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Проверка получения всех заказов")
    public void checkGetOrdersList() {
        given()
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders", notNullValue());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideColorTestCases")
    @DisplayName("Заказ создаётся успешно и содержит track")
    void shouldCreateOrderWithDifferentColors(String testName, List<String> color) {
        Order order = new Order();
        order.setColor(color);

       given()
                .body(order)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }


    static Stream<Arguments> provideColorTestCases() {
        return Stream.of(
                Arguments.of("Цвет один: BLACK", Arrays.asList("BLACK")),
                Arguments.of("Цвет один: GREY", Arrays.asList("GREY")),
                Arguments.of("Оба цвета", Arrays.asList("BLACK", "GREY")),
                Arguments.of("Цвет не указан", null)
        );
    }


}
