package tv.px.android.robospice.robospice_oauth2;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.retry.RetryPolicy;

/**
 * Retry-policy for requests that try to access resources protected by OAuth2. If the request fails due to an expired
 * token and a valid refresh-token is present, this policy will attempt to refresh the token.
 */
public class OAuth2RetryPolicy implements RetryPolicy {

   private SpiceService spiceService;

   /**
    * The default constructor. A spice-service is required to fire the request to refresh the token.
    * 
    * @param _spiceService
    *           instance of the running spice-service
    */
   public OAuth2RetryPolicy(SpiceService _spiceService) {
      super();
      this.spiceService = _spiceService;
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
    * @param arg0 the exception that occured during last request invocation
    */
   public void retry(SpiceException arg0) {
      // TODO check for oauth-error and try refreshing the token
      // TODO propagate the refreshed token to the others
   }
}
