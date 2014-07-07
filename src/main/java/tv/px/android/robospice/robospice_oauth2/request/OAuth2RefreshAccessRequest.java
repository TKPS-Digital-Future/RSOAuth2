package tv.px.android.robospice.robospice_oauth2.request;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;

/**
 * Get a new {@link AccessGrant} by refreshing the token.
 * 
 */
public class OAuth2RefreshAccessRequest extends OAuth2AccessRequest {

   private String refreshToken;

   /**
    * Initialize the request.
    * 
    * @param clazz
    *           class of the expected result
    * @param _oauth2Template
    *           initialized template to perform the communication
    * @param _refreshToken
    *           refresh token used to authenticate
    */
   public OAuth2RefreshAccessRequest(Class<AccessGrant> clazz, OAuth2Template _oauth2Template, String _refreshToken) {
      super(clazz, _oauth2Template);
      this.refreshToken = _refreshToken;
   }

   @Override
   public AccessGrant loadDataFromNetwork() throws Exception {
      return this.getOauth2Template().refreshAccess(refreshToken, null);
   }

}
