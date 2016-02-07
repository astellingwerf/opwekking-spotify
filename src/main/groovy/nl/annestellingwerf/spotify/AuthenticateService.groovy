package nl.annestellingwerf.spotify

import com.wrapper.spotify.Api
import com.wrapper.spotify.models.AuthorizationCodeCredentials
import com.wrapper.spotify.models.User
import groovy.xml.MarkupBuilder
import nl.annestellingwerf.Service

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import java.security.SecureRandom

import static groovy.json.JsonOutput.toJson
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE
import static javax.ws.rs.core.Response.Status.*
import static javax.ws.rs.core.Response.ok
import static javax.ws.rs.core.Response.status
import static org.apache.commons.codec.binary.Hex.encodeHex

@Path('/spotify')
class AuthenticateService extends Service {

    public static final String SESSION_ATTR_SPOTIFY_API_INSTANCE = 'spotify.api';

    private static final String SESSION_ATTR_SPOTIFY_CSRF_TOKEN = 'spotify.csrf.token';
    private static final String QUERY_PARAM_STATE = 'state';
    private static final String QUERY_PARAM_CODE = 'code'

    static String createSecureRandomString() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return new String(encodeHex(randomBytes));
    }


    @Context
    HttpServletRequest currentRequest;

    @GET
    @Path('/authenticate')
    public Response initiation(@Context UriInfo uriInfo) {
        // Set return URL to self
        String returnUrl = uriInfo.absolutePathBuilder.path('/callback').build().toString()

        // Set the necessary scopes that the application will need from the user
        List<String> scopes = ['playlist-modify-private']

        // Set a state. This is used to prevent cross site request forgeries.
        String state = createSecureRandomString();
        session.setAttribute(SESSION_ATTR_SPOTIFY_CSRF_TOKEN, state);

        Api api = apiBuilder
                .redirectURI(returnUrl)
                .build()

        build status(FOUND)
                .location(URI.create(api.createAuthorizeURL(scopes, state)))
    }

    @GET
    @Path('/authenticate/callback')
    public Response callback(@Context UriInfo uriInfo) {
        // Check for CSRF first
        String stateInSession = (String) session.getAttribute(SESSION_ATTR_SPOTIFY_CSRF_TOKEN)
        String stateInRequest = currentRequest.getParameter(QUERY_PARAM_STATE)
        if (!stateInRequest.contentEquals(stateInSession)) {
            return build(status(FORBIDDEN).entity(toJson(error: 'CSRF attack detected.')).type(APPLICATION_JSON_TYPE))
        }

        // Walk through Authorization Code Grant and store the API instance
        String currentUrl = uriInfo.absolutePathBuilder.build().toString()
        Api api = apiBuilder.redirectURI(currentUrl).build()

        String code = currentRequest.getParameter(QUERY_PARAM_CODE)
        api.authorizationCodeGrant(code).build().get().with {
            api.accessToken = accessToken
            api.refreshToken = refreshToken
        }
        session.setAttribute(SESSION_ATTR_SPOTIFY_API_INSTANCE, api)

        // Build a self-closing HTML page
        def sw = new StringWriter()
        new MarkupBuilder(sw).html {
            head {
                title('Logged in successfully.')
                script(type: 'text/javascript',
                        '''
    function closeme() {
        window.open('', '_self', '');
        window.close();
    }''')
            }
            body(onload: 'closeme()') {
                h1('Logged in to Spotify successfully.')
                div('You were successfully logged in to Spotify. ' +
                        'This page should have closed automatically. ' +
                        'Please close it yourself to return to the app.')
            }
        }

        build ok(sw.toString()).type(TEXT_HTML_TYPE)
    }

    @GET
    @Path('/user')
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser() {
        Api api = session.getAttribute(SESSION_ATTR_SPOTIFY_API_INSTANCE)
        if (!api) {
            return build(status(UNAUTHORIZED).entity(toJson(error: 'Not authenticated yet.')))
        }

        User user = api.me.build().get()
        build ok(toJson([displayName: user.displayName, images: user.images]))
    }

    private Api.Builder getApiBuilder() {
        Api.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
    }

    private String getClientSecret() {
        getClientProperty('secret')
    }

    private String getClientId() {
        getClientProperty('id')
    }

    private String getClientProperty(String s) {
        System.getProperty("nl.annestellingwerf.spotify.client.$s")
    }
}
