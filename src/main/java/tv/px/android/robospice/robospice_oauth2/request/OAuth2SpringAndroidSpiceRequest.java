package tv.px.android.robospice.robospice_oauth2.request;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.social.oauth2.AccessGrant;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Abstract class for requests that try to access resources protected by OAuth2. Sets the HTTP-Authentication-header. If
 * you set a {@link OAuth2RetryPolicy} as retry-policy, this becomes even more useful.
 * 
 * @param <RESULT>
 *           type of the expected result
 */
public abstract class OAuth2SpringAndroidSpiceRequest<RESULT> extends SpringAndroidSpiceRequest<RESULT> {
   
   private AccessGrant accessGrant;
   private HttpMethod method;
   private URI url;

   /**
    * Default constructor.
    * 
    * @param clazz
    *           the class-representation of the expected result
    */
   public OAuth2SpringAndroidSpiceRequest(Class<RESULT> clazz, HttpMethod _method, URI _url) {
      super(clazz);
      this.method = _method;
      this.url = _url;
   }
   
   public void setAccessGrant(AccessGrant _accessgrant) {
      this.accessGrant = _accessgrant;
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
      return getRestTemplate().exchange(url, method, new HttpEntity<RESULT>(getAuthHeader()), getResultType()).getBody();
   }

   @SuppressWarnings("serial")
   private HttpHeaders getAuthHeader() {
      return new HttpHeaders() {
         {
            String auth = "Bearer " + accessGrant.getAccessToken();
            set("Authorization", auth);
         }
      };
   }

}
