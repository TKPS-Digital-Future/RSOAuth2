package tv.px.android.robospice.robospice_oauth2;

import java.util.HashSet;
import java.util.Set;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;

import tv.px.android.robospice.robospice_oauth2.request.OAuth2AccessRequest;
import tv.px.android.robospice.robospice_oauth2.request.OAuth2SpringAndroidSpiceRequest;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * A {@link SpringAndroidSpiceService} dedicated to json web services via Jackson protected with OAuth2. Provides
 * caching.
 */
public class OAuth2JacksonSpringAndroidSpiceService extends JacksonSpringAndroidSpiceService {

   private final static String preferencesName = "tv.px.android.robospice.robospice_oauth2.accessGrant";

   private final static String accessTokenKey = "accessToken";
   private final static String scopeKey = "scope";
   private final static String refreshTokenKey = "refreshToken";
   private final static String expiresInKey = "expiresIn";

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
      oauth2Template = new OAuth2Template(null, null, null, null, null);
      // TODO properly initialize template with dummy-data now
      // TODO properly initialize template with correct data

      // load initial access grant from shared preferences
      // TODO set default values to dummy-data now
      // TODO add error-handling
      SharedPreferences sharedPreferences = getSharedPreferences(preferencesName, MODE_MULTI_PROCESS);
      String accessToken = sharedPreferences.getString(accessTokenKey, "");
      String scope = sharedPreferences.getString(scopeKey, "");
      String refreshToken = sharedPreferences.getString(refreshTokenKey, "");
      Long expiresIn = sharedPreferences.getLong(expiresInKey, 0);

      currentGrant = new AccessGrant(accessToken, scope, refreshToken, expiresIn);

      authenticatedRequests = new HashSet<OAuth2SpringAndroidSpiceRequest<?>>();
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
         OAuth2RetryPolicy retryPolicy = new OAuth2RetryPolicy(this, oauth2Template, currentGrant);
         request.getSpiceRequest().setRetryPolicy(retryPolicy);
         
         ((OAuth2SpringAndroidSpiceRequest<?>) request.getSpiceRequest()).setAccessGrant(currentGrant);

         authenticatedRequests.add((OAuth2SpringAndroidSpiceRequest<?>) request.getSpiceRequest());

         listRequestListener.add(new AuthenticatedRequestListener<Object>(
                  (OAuth2SpringAndroidSpiceRequest<Object>) request.getSpiceRequest()));
      } else if (request.getSpiceRequest() instanceof OAuth2AccessRequest) {
         listRequestListener.add(new GrantRequestListener());
      }

      super.addRequest(request, listRequestListener);
   }

   // TODO verification of hostname/certificate using either request factory or connection factory

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
         currentGrant = arg0;

         Editor editor = getSharedPreferences(preferencesName, MODE_MULTI_PROCESS).edit();
         editor.putString(accessTokenKey, arg0.getAccessToken());
         editor.putString(scopeKey, arg0.getScope());
         editor.putString(refreshTokenKey, arg0.getRefreshToken());
         editor.putLong(expiresInKey, arg0.getExpireTime());
         editor.commit();

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
