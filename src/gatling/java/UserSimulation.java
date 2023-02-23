import io.gatling.javaapi.core.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.jdbcFeeder;

public class UserSimulation extends Simulation{

    HttpProtocolBuilder httpProtocol = http.baseUrl("http://192.168.220.128:8888");

    FeederBuilder.FileBased<Object> loginFeeder = jsonFile("login.json").circular();
    FeederBuilder.FileBased<Object> registerFeeder = jsonFile("register.json").circular();
//    FeederBuilder<Object> jdbcFeeder = jdbcFeeder("jdbc:mysql://192.168.220.128:3306/discuzz", "root", "123456", "SELECT * FROM user").circular();
    ScenarioBuilder scn = scenario("DemoSimulation").
        feed(registerFeeder).exec(http("registerRequest").post("/sign_up").header("content-type", "application/x-www-form-urlencoded").formParam("username", "#{username}").formParam("password", "#{password}").check(jmesPath("code").ofInt().is(0)));

    ScenarioBuilder scn2 = scenario("DemoSimulation1").
            feed(loginFeeder).exec(http("loginRequest").post("/sign_in").header("content-type", "application/x-www-form-urlencoded").formParam("username", "#{username}").formParam("password", "123456").check(jmesPath("code").ofInt().is(0)))
            .exec(http("logoutRequest").delete("/logout").header("content-type", "application/json").check(jmesPath("code").ofInt().is(0)));

    {
        setUp(scn.injectOpen(atOnceUsers(10)).protocols(httpProtocol).andThen(scn2.injectOpen(rampUsersPerSec(1).to(10).during(60)).protocols(httpProtocol)));
    }

}
