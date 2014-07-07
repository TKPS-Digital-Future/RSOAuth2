package tv.px.android.robospice.robospice_oauth2;

import java.util.Collections;
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

   private SpiceService spiceService;
   private OAuth2Template oauth2Template;
   private AccessGrant accessGrant;

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
      // TODO should probably be 2 or 3 for oauth-errors and a sensible
      // default for others
      return 0;
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

            // initialize and encapsulate spice-request
            OAuth2RefreshAccessRequest refreshRequest = new OAuth2RefreshAccessRequest(AccessGrant.class,
                     oauth2Template, accessGrant.getRefreshToken());
            CachedSpiceRequest<AccessGrant> cachedRequest = new CachedSpiceRequest<AccessGrant>(refreshRequest,
                     "tv.px.android.robospice.robospice_oauth.refresh_request", DurationInMillis.ALWAYS_EXPIRED);

            // initialize and encapsulate request-listener
            Set<RequestListener<?>> requestListenerSet = Collections.singleton(null);
            // TODO request-listener needs to check for permanent error and update retry-count

            // add request to chain
            spiceService.addRequest(cachedRequest, requestListenerSet);
         } else {
            Ln.d("Other HTTP exception");
            // TODO handle downstream, set retry-count to 0
         }
      } else if (arg0 instanceof RequestCancelledException) {
         Ln.d("Cancelled");
         // TODO handle downstream, set retry-count to 0
      } else {
         Ln.d("Other exception");
         // TODO handle downstream, set retry-count to 0
      }
   }
}
