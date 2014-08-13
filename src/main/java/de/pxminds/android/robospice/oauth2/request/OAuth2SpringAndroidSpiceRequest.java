package de.pxminds.android.robospice.oauth2.request;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.social.oauth2.AccessGrant;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Abstract class for requests that try to access resources protected by OAuth2. Sets the HTTP-Authentication-header. If
 * you set a {@link de.pxminds.android.robospice.oauth2.OAuth2RetryPolicy} as retry-policy, this becomes even more
 * useful.
 * 
 * @param <RESULT>
 *           type of the expected result
 */
public abstract class OAuth2SpringAndroidSpiceRequest<RESULT> extends SpringAndroidSpiceRequest<RESULT> {

   private AccessGrant accessGrant;
   private final HttpMethod method;
   private final URI url;
   private final HttpEntity<RESULT> httpEntity;

   /**
    * Default constructor.
    * 
    * @param clazz
    *           the class-representation of the expected result
    * @param _method
    *           the HTTP-method of the request (POST/GET/DELETE/etc.)
    * @param _url
    *           the target-URI for the request
    */
   public OAuth2SpringAndroidSpiceRequest(Class<RESULT> clazz, HttpMethod _method, URI _url) {
      super(clazz);
      this.method = _method;
      this.url = _url;
      this.httpEntity = new HttpEntity<RESULT>(getAuthHeader());
   }

   /**
    * Constructor to allow passing a custom HTTP-Entity. Be careful to add the getAuthHeader() to this entity!
    * 
    * @param clazz
    *           the class-representation of the expected result
    * @param _method
    *           the HTTP-method of the request (POST/GET/DELETE/etc.)
    * @param _url
    *           the target-URI for the request
    * @param _httpEntity
    *           the custom HTTP-Entity. Could contain a JSON-body for POST- or PUT-requests. Should contain the
    *           getAuthHeader()
    */
   public OAuth2SpringAndroidSpiceRequest(Class<RESULT> clazz, HttpMethod _method, URI _url,
            HttpEntity<RESULT> _httpEntity) {
      super(clazz);
      this.method = _method;
      this.url = _url;
      this.httpEntity = _httpEntity;
   }

   /**
    * Set the access-grant for this request.
    * 
    * @param _accessgrant
    *           a (hopefully) valid access-grant
    */
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
   public final RESULT loadDataFromNetwork() {
      return getRestTemplate().exchange(url, method, httpEntity, getResultType()).getBody();
   }

   protected HttpHeaders getAuthHeader() {
      return new HttpHeaders() {
         {
            String auth = "Bearer " + accessGrant.getAccessToken();
            set("Authorization", auth);
         }
      };
   }

}
