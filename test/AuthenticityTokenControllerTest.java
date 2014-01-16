import authtoken.AuthTokenConstants;
import authtoken.AuthenticityTokenGenerator;
import controllers.routes;
import org.junit.Test;
import play.libs.Crypto;
import play.mvc.Content;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static play.test.Helpers.contentType;


/**
 * Testing AuthenticityToken controller
 *
 */
public class AuthenticityTokenControllerTest {
    @Test
    public void formContainsAuthenticityToken() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result res = route(fakeRequest("GET", "/form").withSession("", ""));
                Logger logger = Logger.getLogger(AuthenticityTokenControllerTest.class.getName());
                String sContent = contentAsString(res);
                logger.log(Level.FINE, sContent);
                assertThat(sContent.contains(AuthTokenConstants.AUTH_TOKEN_FORM_FIELD));
            }
        });
    }

    @Test
    public void badFormDoesntContainsAuthenticityToken() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result res = route(fakeRequest("GET", "/badform").withSession("", ""));
                String sContent = contentAsString(res);
                assertThat(!sContent.contains(AuthTokenConstants.AUTH_TOKEN_FORM_FIELD));
            }
        });
    }

    @Test
    public void formPassesAuthenticity() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                String token = UUID.randomUUID().toString();

                final Map<String, String> data = new HashMap<String, String>();
                data.put(AuthTokenConstants.AUTH_TOKEN_FORM_FIELD, token);

                Result result = route(fakeRequest("POST", "/form/process")
                        .withFormUrlEncodedBody(data)
                        .withSession(AuthTokenConstants.AUTH_TOKEN, Crypto.sign(token))
                );

                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(charset(result)).isEqualTo("utf-8");
            }
        });
    }

    @Test
    public void badFormAccessDenied() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                String token = UUID.randomUUID().toString();

                final Map<String, String> data = new HashMap<String, String>();
                data.put(AuthTokenConstants.AUTH_TOKEN_FORM_FIELD, "SOME INVALID TOKEN");

                Result result = route(fakeRequest("POST", "/form/process")
                        .withFormUrlEncodedBody(data)
                        .withSession(AuthTokenConstants.AUTH_TOKEN, Crypto.sign(token))
                );

                assertThat(status(result)).isEqualTo(BAD_REQUEST);
            }
        });
    }
}
