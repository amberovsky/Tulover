package com.revolut.transfer;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.revolut.router.Response.ResponseCode;

import static com.revolut.router.Response.ResponseCode.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration e2e tests
 *
 * There is a concurrent test at the end
 */
public class TransferServiceTest {
    @BeforeAll
    public static void setup() {
        port = 4567;
        baseURI = "http://localhost";
        Main.main(new String[] {});
    }

    @Test
    public void testReturns404OnUnknownURL() {
        given()
            .get("/whatever")
        .then()
            .statusCode(404);
    }

    private static Stream<Arguments> createInputForTestResponseCodeOn200WithJSONContentType() {
        return Stream.of(
            Arguments.of("/transfer?to=326608e5-5fbf-4505-871d-d0ec830e1994&amount=123", MISSING_PARAMETER),
            Arguments.of("/transfer?from=326608e5-5fbf-4505-871d-d0ec830e1994&amount=123", MISSING_PARAMETER),
            Arguments.of("/transfer?from=5ab59fdf-997f-4a20-ab33-67272b840a19&to=326608e5-5fbf-4505-871d-d0ec830e1994", MISSING_PARAMETER),
            Arguments.of("/transfer?from=123&to=326608e5-5fbf-4505-871d-d0ec830e1994&amount=123", WRONG_PARAMETER),
            Arguments.of("/transfer?from=326608e5-5fbf-4505-871d-d0ec830e1994&to=111&amount=123", WRONG_PARAMETER),

            Arguments.of("/transfer?from=326608e5-5fbf-4505-871d-d0ec830e1994&to=5ab59fdf-997f-4a20-ab33-67272b840a19&amount=abc", WRONG_PARAMETER),
            Arguments.of("/transfer?from=326608e5-5fbf-4505-871d-d0ec830e1994&to=5ab59fdf-997f-4a20-ab33-67272b840a19&amount=0", INVALID_VALUE),

            Arguments.of("/transfer?from=00000000-0000-0000-0000-000000000001&to=5ab59fdf-997f-4a20-ab33-67272b840a19&amount=1", INVALID_VALUE),
            Arguments.of("/transfer?to=00000000-0000-0000-0000-000000000001&from=5ab59fdf-997f-4a20-ab33-67272b840a19&amount=1", INVALID_VALUE)
        );
    }

    @ParameterizedTest
    @MethodSource("createInputForTestResponseCodeOn200WithJSONContentType")
    void testResponseCodeOn200WithJSONContentType(String URI, ResponseCode responseCode) {
        given()
            .post(URI)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("responseCode", equalTo(responseCode.toString()));
    }

    @Test
    public void testReturns200WithINVALID_VALUEAndDataOnFromFieldEqualsToToField() {
        given()
            .post("/transfer?to=5ab59fdf-997f-4a20-ab33-67272b840a19&from=5ab59fdf-997f-4a20-ab33-67272b840a19&amount=1")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("responseCode", equalTo(INVALID_VALUE.toString()))
            .body("msg", equalTo("can not transfer to the same account"));
    }

    @Test
    public void testReturns200WithNO_ERRORWhenThereAreEnoughMoney() {
        given()
            .post("/transfer?to=5ab59fdf-997f-4a20-ab33-67272b840a19&from=326608e5-5fbf-4505-871d-d0ec830e1994&amount=1")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("responseCode", equalTo(NO_ERROR.toString()))
            .body("msg", equalTo(""));
    }

    @Test
    public void testReturns200WithINTERNAL_ERRORWhenThereAreEnoughMoney() {
        given()
            .post("/transfer?to=5ab59fdf-997f-4a20-ab33-67272b840a19&from=326608e5-5fbf-4505-871d-d0ec830e1994&amount=9999")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("responseCode", equalTo(INTERNAL_ERROR.toString()))
            .body("msg", equalTo("Account 326608e5-5fbf-4505-871d-d0ec830e1994 does not have enough balance"));
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int threads = 100;
        List<Callable<Void>> tasks = new ArrayList<>(threads);
        AtomicInteger noErrors = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            // Account d2febbaf-0edb-4f19-824e-588b712c8c29 has 5000 funds

            // Half of the requests will be with 1 amount, the other half will be with a too big amount
            long amountToSend = (i % 2 == 0) ? 1 : 10000;
            tasks.add(() -> {
                String code =
                given()
                    .post("/transfer?to=5ab59fdf-997f-4a20-ab33-67272b840a19&from=d2febbaf-0edb-4f19-824e-588b712c8c29&amount=" + amountToSend)
                .then()
                .extract()
                    .path("responseCode");

                if (code.equals("NO_ERROR")) noErrors.incrementAndGet();
                else errors.incrementAndGet();

                return null;
            });
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        executorService.invokeAll(tasks);

        assertEquals(threads / 2, noErrors.get());
        assertEquals(threads / 2, errors.get());
        executorService.shutdown();
    }
}
