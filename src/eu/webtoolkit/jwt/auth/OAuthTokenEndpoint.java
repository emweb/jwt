/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint to retrieve an access token.
 *
 * <p>The token endpoint is used by the client to obtain an {@link OAuthAccessToken} by presenting
 * its authorization grant. This implementation only supports the &quot;authorization_code&quot;
 * grant type. The client ID and secret can be passed with Basic auth or by POST request parameters.
 * When something goes wrong, the reply will include a JSON object with an &quot;error&quot;
 * attribute.
 *
 * <p>This endpoint is implemented as a {@link WResource}, so it&apos;s usually deployed using
 * {@link WServer#addResource(WResource resource, String path) WServer#addResource()}.
 *
 * <p>For more information refer to the specification: <a
 * href="https://tools.ietf.org/rfc/rfc6749.txt">https://tools.ietf.org/rfc/rfc6749.txt</a>
 *
 * <p>When the scope includes &quot;openid&quot; an ID {@link Token} will be included as specified
 * by the OpenID Connect standard.
 *
 * <p>This class relies on a correct implementation of several function in the {@link
 * AbstractUserDatabase}. Namely {@link AbstractUserDatabase#idpClientFindWithId(String clientId)
 * AbstractUserDatabase#idpClientFindWithId()}, {@link
 * AbstractUserDatabase#idpClientAuthMethod(OAuthClient client)
 * AbstractUserDatabase#idpClientAuthMethod()}, {@link
 * AbstractUserDatabase#idpVerifySecret(OAuthClient client, String secret)
 * AbstractUserDatabase#idpVerifySecret()}, {@link AbstractUserDatabase#idpClientId(OAuthClient
 * client) AbstractUserDatabase#idpClientId()}, {@link
 * AbstractUserDatabase#idpTokenFindWithValue(String purpose, String scope)
 * AbstractUserDatabase#idpTokenFindWithValue()}, {@link AbstractUserDatabase#idpTokenAdd(String
 * value, WDate expirationTime, String purpose, String scope, String redirectUri, User user,
 * OAuthClient authClient) AbstractUserDatabase#idpTokenAdd()}, {@link
 * AbstractUserDatabase#idpTokenRemove(IssuedToken token) AbstractUserDatabase#idpTokenRemove()},
 * {@link AbstractUserDatabase#idpTokenRedirectUri(IssuedToken token)
 * AbstractUserDatabase#idpTokenRedirectUri()}, AbstractUserDatabase::idpTokenAuthClient, {@link
 * AbstractUserDatabase#idpTokenUser(IssuedToken token) AbstractUserDatabase#idpTokenUser()}, and
 * {@link AbstractUserDatabase#idpTokenScope(IssuedToken token)
 * AbstractUserDatabase#idpTokenScope()}.
 *
 * <p>Must be deployed using TLS.
 */
public class OAuthTokenEndpoint extends WResource {
  private static Logger logger = LoggerFactory.getLogger(OAuthTokenEndpoint.class);

  /**
   * Constructor.
   *
   * <p>The issuer argument is used for the &quot;iss&quot; attribute in the ID {@link Token} when
   * the scope includes &quot;openid&quot;.
   */
  public OAuthTokenEndpoint(final AbstractUserDatabase db, String issuer) {
    super();
    this.db_ = db;
    this.accessExpSecs_ = 3600;
    this.idExpSecs_ = 3600;
    this.iss_ = issuer;
  }

  public void handleRequest(final WebRequest request, final WebResponse response) {
    try {
      response.setContentType("application/json");
      response.addHeader("Cache-Control", "no-store");
      response.addHeader("Pragma", "no-cache");
      String grantType = request.getParameter("grant_type");
      String redirectUri = request.getParameter("redirect_uri");
      String code = request.getParameter("code");
      String clientId = "";
      String clientSecret = "";
      ClientSecretMethod authMethod = ClientSecretMethod.HttpAuthorizationBasic;
      String headerSecret = "";
      String authHeader = request.getHeaderValue("Authorization");
      if (authHeader.length() > AUTH_TYPE.length() + 1) {
        headerSecret = Utils.base64DecodeS(authHeader.substring(AUTH_TYPE.length() + 1));
        List<String> tokens = new ArrayList<String>();
        StringUtils.split(tokens, headerSecret, ":", false);
        if (tokens.size() == 2) {
          clientId = java.net.URLDecoder.decode(tokens.get(0), "UTF-8");
          ;
          clientSecret = java.net.URLDecoder.decode(tokens.get(1), "UTF-8");
          ;
          authMethod = ClientSecretMethod.HttpAuthorizationBasic;
        }
      }
      if (clientId.length() == 0 && clientSecret.length() == 0) {
        String clientIdParam = request.getParameter("client_id");
        String clientSecretParam = request.getParameter("client_secret");
        if (clientIdParam != null && clientSecretParam != null) {
          clientId = clientIdParam;
          clientSecret = clientSecretParam;
          authMethod = ClientSecretMethod.RequestBodyParameter;
        }
      }
      if (!(code != null)
          || clientId.length() == 0
          || clientSecret.length() == 0
          || !(grantType != null)
          || !(redirectUri != null)) {
        response.setStatus(400);
        response.out().append("{\"error\": \"invalid_request\"}").append('\n');
        logger.info(
            new StringWriter()
                .append("{\"error\": \"invalid_request\"}:")
                .append(" code:")
                .append(code != null ? code : "NULL")
                .append(" clientId: ")
                .append(clientId)
                .append(" clientSecret: ")
                .append(clientSecret.length() == 0 ? "MISSING" : "NOT MISSING")
                .append(" grantType: ")
                .append(grantType != null ? grantType : "NULL")
                .append(" redirectUri: ")
                .append(redirectUri != null ? redirectUri : "NULL")
                .toString());
        return;
      }
      OAuthClient client = this.db_.idpClientFindWithId(clientId);
      if (!client.isCheckValid()
          || !client.verifySecret(clientSecret)
          || client.getAuthMethod() != authMethod) {
        response.setStatus(401);
        if (authHeader.length() != 0) {
          if (client.getAuthMethod() == ClientSecretMethod.HttpAuthorizationBasic) {
            response.addHeader("WWW-Authenticate", AUTH_TYPE);
          } else {
            response.addHeader("WWW-Authenticate", methodToString(client.getAuthMethod()));
          }
        }
        response.out().append("{\n\"error\": \"invalid_client\"\n}").append('\n');
        logger.info(
            new StringWriter()
                .append("{\"error\": \"invalid_client\"}: ")
                .append(" id: ")
                .append(clientId)
                .append(" client: ")
                .append(client.isCheckValid() ? "valid" : "not valid")
                .append(" secret: ")
                .append(client.verifySecret(clientSecret) ? "correct" : "incorrect")
                .append(" method: ")
                .append(client.getAuthMethod() != authMethod ? "no match" : "match")
                .toString());
        return;
      }
      if (!grantType.equals(GRANT_TYPE)) {
        response.setStatus(400);
        response.out().append("{\n\"error\": \"unsupported_grant_type\"\n}").append('\n');
        logger.info(
            new StringWriter()
                .append("{\"error\": \"unsupported_grant_type\"}: ")
                .append(" id: ")
                .append(clientId)
                .append(" grantType: ")
                .append(String.valueOf(grantType != null))
                .toString());
        return;
      }
      IssuedToken authCode = this.db_.idpTokenFindWithValue(GRANT_TYPE, code);
      if (!authCode.isCheckValid()
          || !authCode.getRedirectUri().equals(redirectUri)
          || WDate.getCurrentServerDate().after(authCode.getExpirationTime())) {
        response.setStatus(400);
        response.out().append("{\n\"error\": \"invalid_grant\"\n}").append('\n');
        logger.info(
            new StringWriter()
                .append("{\"error\": \"invalid_grant\"}:")
                .append(" id: ")
                .append(clientId)
                .append(" code: ")
                .append(code)
                .append(" authCode: ")
                .append(authCode.isCheckValid() ? "valid" : "not valid")
                .append(" redirectUri: ")
                .append(redirectUri)
                .append(!authCode.getRedirectUri().equals(redirectUri) ? " - invalid" : " - valid")
                .append(" timestamp: ")
                .append(authCode.getExpirationTime().toString())
                .append(
                    WDate.getCurrentServerDate().after(authCode.getExpirationTime())
                        ? ", expired"
                        : ", not expired")
                .toString());
        return;
      }
      String accessTokenValue = MathUtils.randomId();
      WDate expirationTime = WDate.getCurrentServerDate().addSeconds(this.accessExpSecs_);
      final User user = authCode.getUser();
      final OAuthClient authClient = authCode.getAuthClient();
      final String scope = authCode.getScope();
      this.db_.idpTokenAdd(
          accessTokenValue,
          expirationTime,
          "access_token",
          scope,
          authCode.getRedirectUri(),
          user,
          authClient);
      this.db_.idpTokenRemove(authCode);
      response.setStatus(200);
      com.google.gson.JsonObject root = new com.google.gson.JsonObject();
      root.add("access_token", (new com.google.gson.JsonPrimitive(accessTokenValue)));
      root.add("token_type", (new com.google.gson.JsonPrimitive("Bearer")));
      root.add("expires_in", (new com.google.gson.JsonPrimitive(this.accessExpSecs_)));
      if (authCode.getScope().indexOf("openid") != -1) {
        String header = "";
        String signature = "";
        String payload =
            Utils.base64Encode(this.idTokenPayload(authClient.getClientId(), scope, user), false);
        header = Utils.base64Encode("{\n\"typ\": \"JWT\",\n\"alg\": \"none\"\n}", false);
        signature = Utils.base64Encode("", false);
        root.add(
            "id_token",
            (new com.google.gson.JsonPrimitive(header + "." + payload + "." + signature)));
      }
      response.out().append(root.toString());
      logger.info(
          new StringWriter()
              .append("success: ")
              .append(clientId)
              .append(", ")
              .append(user.getId())
              .append(", ")
              .append(this.db_.getEmail(user))
              .toString());
    } catch (IOException ioe) {
      logger.error(new StringWriter().append(ioe.getMessage()).toString());
    }
  }
  /**
   * Sets the amount of seconds after which generated access tokens expire.
   *
   * <p>Defaults to 3600 seconds.
   */
  public void setAccessExpSecs(int seconds) {
    this.accessExpSecs_ = seconds;
  }
  /**
   * Sets the amount of seconds after which generated id tokens expire.
   *
   * <p>Defaults to 3600 seconds.
   */
  public void setIdExpSecs(int seconds) {
    this.idExpSecs_ = seconds;
  }
  /** Is only called when scope contains openid. Generates a JSON Web {@link Token}. */
  private String idTokenPayload(final String clientId, final String scope, final User user) {
    com.google.gson.JsonObject root = new com.google.gson.JsonObject();
    root.add("iss", (new com.google.gson.JsonPrimitive(this.iss_)));
    root.add("sub", (new com.google.gson.JsonPrimitive(user.getId())));
    root.add("aud", (new com.google.gson.JsonPrimitive(clientId)));
    WDate curTime = WDate.getCurrentServerDate();
    root.add(
        "exp",
        (new com.google.gson.JsonPrimitive(
            (long) curTime.addSeconds(this.idExpSecs_).getDate().getTime() / 1000)));
    root.add("iat", (new com.google.gson.JsonPrimitive((long) curTime.getDate().getTime() / 1000)));
    root.add(
        "auth_time",
        (new com.google.gson.JsonPrimitive(
            String.valueOf(user.getLastLoginAttempt().getDate().getTime() / 1000))));
    return root.toString();
  }

  private AbstractUserDatabase db_;
  private int accessExpSecs_;
  private int idExpSecs_;
  private String iss_;

  private static String methodToString(ClientSecretMethod method) {
    switch (method) {
      case HttpAuthorizationBasic:
        return "client_secret_basic";
      case RequestBodyParameter:
        return "client_secret_post";
      default:
        return "";
    }
  }

  private static final String GRANT_TYPE = "authorization_code";
  private static final String AUTH_TYPE = "Basic";
}
