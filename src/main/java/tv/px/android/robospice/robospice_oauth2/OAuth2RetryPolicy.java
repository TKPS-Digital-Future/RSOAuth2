package tv.px.android.robospice.robospice_oauth2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.web.client.HttpClientErrorException;

import roboguice.util.temp.Ln;
import tv.px.android.robospice.robospice_oauth2.request.OAuth2RefreshAccessRequest;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.retry.RetryPolicy;

/**
 * Retry-policy for requests that try to access resources protected by OAuth2. If the request fails due to an expired
 * token and a valid refresh-token is present, this policy will attempt to refresh the token.
 */
public class OAuth2RetryPolicy implements RetryPolicy {

   // SpiceService to fire the refresh-request
   private SpiceService spiceService;

   // OAuth2Template and AccessGrant to initialize the refresh-request
   private OAuth2Template oauth2Template;
   private AccessGrant accessGrant;

   // internal state
   private int retryCount = 0;

   /**
    * The default constructor. A spice-service is required to fire the request to refresh the token.
    * 
    * @param _spiceService
    *           instance of the running spice-service
    * @param _oauth2Template
    *           OAuth2Template for the communication with the grant-endpoint
    * @param _accessGrant
    *           existing access-grant to extract the refresh-token from
    */
   public OAuth2RetryPolicy(SpiceService _spiceService, OAuth2Template _oauth2Template, AccessGrant _accessGrant) {
      super();
      this.spiceService = _spiceService;
      this.oauth2Template = _oauth2Template;
      this.accessGrant = _accessGrant;
   }

   /**
    * @see com.octo.android.robospice.retry.RetryPolicy#getDelayBeforeRetry()
    * 
    * @return the delay to sleep between each retry attempt (in ms)
    */
   public long getDelayBeforeRetry() {
      // TODO find and return a sensible value
      // TODO return 0 if the refresh-request has finished successfully
      return 0;
   }

   /**
    * @see com.octo.android.robospice.retry.RetryPolicy#getRetryCount()
    * 
    * @return the remaining number of retry attempts. When this method returns 0, request is not retried anymore
    */
   public int getRetryCount() {
      return retryCount;
   }

   /**
    * Actual retry-policy.
    * 
    * @see com.octo.android.robospice.retry.RetryPolicy#retry(SpiceException)
    * 
    * @param arg0
    *           the exception that occurred during last request invocation
    */
   public void retry(SpiceException arg0) {
      if (arg0.getCause() instanceof HttpClientErrorException) {
         HttpClientErrorException exception = (HttpClientErrorException) arg0.getCause();
         if (exception.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
            Ln.d("401 ERROR");
            // TODO check for oauth-error and try refreshing the token
            // TODO propagate the refreshed token to the others

            // set retry-count to 3 in order to get a refresh-request through
            retryCount = 3;

            // initialize and encapsulate spice-request
            OAuth2RefreshAccessRequest refreshRequest = new OAuth2RefreshAccessRequest(AccessGrant.class,
                     oauth2Template, accessGrant.getRefreshToken());
            CachedSpiceRequest<AccessGrant> cachedRequest = new CachedSpiceRequest<AccessGrant>(refreshRequest,
                     "tv.px.android.robospice.robospice_oauth.refresh_request", DurationInMillis.ALWAYS_EXPIRED);

            // initialize and encapsulate request-listener
            Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
            requestListenerSet.add(new GrantRequestListener());

            // add request to chain
            spiceService.addRequest(cachedRequest, requestListenerSet);
         } else {
            Ln.d("Other HTTP exception");
            retryCount = 0;
         }
      } else if (arg0 instanceof RequestCancelledException) {
         Ln.d("Cancelled");
         retryCount = 0;
      } else {
         Ln.d("Other exception");
         retryCount = 0;
      }
   }

   private class GrantRequestListener implements RequestListener<AccessGrant> {

      /**
       * Called when the refresh-request failed. Sets the retry-count to 0.
       * 
       * @see com.octo.android.robospice.request.listener.RequestListener#onRequestFailure(com.octo.android.robospice.persistence.exception.SpiceException)
       */
      public void onRequestFailure(SpiceException arg0) {
         retryCount = 0;
      }

      /**
       * Called when the refresh-request was successful. Sets the retry-count to 1 and updates the internal
       * access-grant.
       * 
       * @see com.octo.android.robospice.request.listener.RequestListener#onRequestSuccess(java.lang.Object)
       */
      public void onRequestSuccess(AccessGrant arg0) {
         retryCount = 1;
         accessGrant = arg0;
      }

   }
}
