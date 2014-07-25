package de.pxminds.android.robospice.oauth2;

import java.util.HashSet;
import java.util.Set;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;

import com.octo.android.robospice.Jackson2SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import de.pxminds.android.robospice.oauth2.request.OAuth2AccessRequest;
import de.pxminds.android.robospice.oauth2.request.OAuth2SpringAndroidSpiceRequest;

/**
 * A {@link SpringAndroidSpiceService} dedicated to json web services via Jackson protected with OAuth2. Provides
 * caching.
 */
public abstract class OAuth2Jackson2SpringAndroidSpiceService extends Jackson2SpringAndroidSpiceService {

   private OAuth2Template oauth2Template;
   private AccessGrant currentGrant;

   private Set<OAuth2SpringAndroidSpiceRequest<?>> authenticatedRequests;

   /**
    * Initializes the service and the {@link OAuth2Template}.
    * 
    * @see com.octo.android.robospice.SpringAndroidSpiceService#onCreate()
    */
   @Override
   public void onCreate() {
      super.onCreate();
      oauth2Template = createOAuth2Template();

      currentGrant = createAccessGrant();

      authenticatedRequests = new HashSet<OAuth2SpringAndroidSpiceRequest<?>>();
   }

   /**
    * Factory-method to create an OAuth2Template used for the retry-policy. Override in subclasses to set up
    * client-credentials, etc.
    * 
    * @return a completely initialized OAuth2Template
    */
   public abstract OAuth2Template createOAuth2Template();

   /**
    * Factory-method to create an initial AccessGrant used for the requests and retry-policy. Override in subclasses to
    * do fancy things like loading it from preferences, etc.
    * 
    * @return an AccessGrant containing at least a valid refresh-token
    */
   public abstract AccessGrant createAccessGrant();

   /**
    * Get the current AccessGrant. Useful if you want to save it to preferences in a subclass.
    * 
    * @return the current AccessGrant
    */
   public AccessGrant getCurrentGrant() {
      return currentGrant;
   }

   /**
    * Set the current AccessGrant. Called when an OAuth2AccessRequest returns a new grant. Override in subclasses (don't
    * forget to call super()) to save it to preferences, etc.
    * 
    * @param newGrant the new AccessGrant
    */
   public void setCurrentGrant(AccessGrant newGrant) {
      this.currentGrant = newGrant;
   }

   /**
    * Add a request to the queue. If it is an {@link OAuth2SpringAndroidSpiceRequest}, an {@link OAuth2RetryPolicy} will
    * be created and set.
    * 
    * @see com.octo.android.robospice.SpringAndroidSpiceService#addRequest(CachedSpiceRequest, Set)
    * 
    * @param request
    *           the request to add to the queue
    * @param listRequestListener
    *           a set of request-listeners to notify when the request finishes or fails
    */
   @Override
   public void addRequest(CachedSpiceRequest<?> request, Set<RequestListener<?>> listRequestListener) {
      if (request.getSpiceRequest() instanceof OAuth2SpringAndroidSpiceRequest) {
         OAuth2RetryPolicy retryPolicy = new OAuth2RetryPolicy(this, oauth2Template, getCurrentGrant());
         request.getSpiceRequest().setRetryPolicy(retryPolicy);

         ((OAuth2SpringAndroidSpiceRequest<?>) request.getSpiceRequest()).setAccessGrant(getCurrentGrant());

         authenticatedRequests.add((OAuth2SpringAndroidSpiceRequest<?>) request.getSpiceRequest());

         listRequestListener.add(new AuthenticatedRequestListener<Object>(
                  (OAuth2SpringAndroidSpiceRequest<Object>) request.getSpiceRequest()));
      } else if (request.getSpiceRequest() instanceof OAuth2AccessRequest) {
         listRequestListener.add(new GrantRequestListener());
      }

      super.addRequest(request, listRequestListener);
   }

   private class GrantRequestListener implements RequestListener<AccessGrant> {

      /**
       * Grant-request failed. Handle downstream.
       * 
       * @see com.octo.android.robospice.request.listener.RequestListener#onRequestFailure(SpiceException)
       */
      public void onRequestFailure(SpiceException arg0) {
         // NO-OP, handle downstream
      }

      /**
       * Grant-request succeeded, update global grant. Also write it to shared preferences.
       * 
       * @see com.octo.android.robospice.request.listener.RequestListener#onRequestSuccess(java.lang.Object)
       */
      public void onRequestSuccess(AccessGrant arg0) {
         setCurrentGrant(arg0);

         for (OAuth2SpringAndroidSpiceRequest<?> currentRequest : authenticatedRequests) {
            currentRequest.setAccessGrant(arg0);
         }
      }
   }

   private class AuthenticatedRequestListener<RESULT> implements RequestListener<RESULT> {
      private OAuth2SpringAndroidSpiceRequest<RESULT> associatedRequest;

      /**
       * Initialize the request-listener.
       * 
       * @param _associatedRequest
       *           the request associated with this listener
       */
      public AuthenticatedRequestListener(OAuth2SpringAndroidSpiceRequest<RESULT> _associatedRequest) {
         this.associatedRequest = _associatedRequest;
      }

      /**
       * Remove request from list of authenticated requests.
       * 
       * @see com.octo.android.robospice.request.listener.RequestListener#onRequestFailure(SpiceException)
       * 
       * @param arg0
       *           the exception that cause the request to fail. Needs to be handled by separate listener
       */
      public void onRequestFailure(SpiceException arg0) {
         authenticatedRequests.remove(associatedRequest);
      }

      /**
       * Remove request from list of authenticated requests.
       * 
       * @see com.octo.android.robospice.request.listener.RequestListener#onRequestSuccess(java.lang.Object)
       * @param arg0
       *           the result of the request. Ignored here
       */
      public void onRequestSuccess(RESULT arg0) {
         authenticatedRequests.remove(associatedRequest);
      }

   }
}
