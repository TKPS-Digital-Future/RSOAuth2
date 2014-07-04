package tv.px.android.robospice.robospice_oauth2.request;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;

/**
 * Get a new {@link AccessGrant} by performing a username-password-authentication.
 * 
 */
public class OAuth2CredentialsAccessRequest extends OAuth2AccessRequest {

   private String username;
   private String password;

   /**
    * Initialize the request.
    * 
    * @param clazz
    *           class of the expected result
    * @param oauth2Template
    *           initialized template to perform the communication
    * @param _username
    *           username to use for authentication
    * @param _password
    *           password to use for authentication
    */
   public OAuth2CredentialsAccessRequest(Class<AccessGrant> clazz, OAuth2Template oauth2Template, String _username,
            String _password) {
      super(clazz, oauth2Template);
      this.username = _username;
      this.password = _password;
   }

   @Override
   public AccessGrant loadDataFromNetwork() {
      return this.oauth2Template.exchangeCredentialsForAccess(username, password, null);
   }

}
