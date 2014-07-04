package tv.px.android.robospice.robospice_oauth2.request;

import tv.px.android.robospice.robospice_oauth2.OAuth2RetryPolicy;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Abstract class for requests that try to access resources protected by OAuth2. Sets the HTTP-Authentication-header. If
 * you set a {@link OAuth2RetryPolicy} as retry-policy, this becomes even more useful.
 * 
 * @param <RESULT>
 *           type of the expected result
 */
public abstract class OAuth2SpringAndroidSpiceRequest<RESULT> extends SpringAndroidSpiceRequest<RESULT> {

   /**
    * Default constructor.
    * 
    * @param clazz
    *           the class-representation of the expected result
    */
   public OAuth2SpringAndroidSpiceRequest(Class<RESULT> clazz) {
      super(clazz);
      // TODO accept a grant
   }

   /**
    * Where the actual work is done. Adds the token to the HTTP-headers before sending the request.
    * 
    * @see com.octo.android.robospice.request.SpiceRequest#loadDataFromNetwork()
    * 
    * @return the result of the request
    */
   @Override
   public RESULT loadDataFromNetwork() {
      // TODO add OAuth2-authorization-header to request
      // TODO fire request
      // TODO handle generic exceptions
      return null;
   }

}
