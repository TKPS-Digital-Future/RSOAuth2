package de.pxminds.android.robospice.oauth2.request;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;

import com.octo.android.robospice.request.SpiceRequest;

/**
 * Superclass for requests to gain an OAuth2 {@link AccessGrant}.
 * 
 */
public abstract class OAuth2AccessRequest extends SpiceRequest<AccessGrant> {

   private OAuth2Template oauth2Template;

   /**
    * Default constructor sets the {@link OAuth2Template}.
    * 
    * @param clazz
    *           the class of the expected result
    * @param _oauth2Template
    *           an initialized OAuth2Template
    */
   public OAuth2AccessRequest(Class<AccessGrant> clazz, OAuth2Template _oauth2Template) {
      super(clazz);
      this.oauth2Template = _oauth2Template;
   }

   /**
    * @return the oauth2Template
    */
   protected OAuth2Template getOauth2Template() {
      return oauth2Template;
   }
}
