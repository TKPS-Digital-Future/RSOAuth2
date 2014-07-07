package tv.px.android.robospice.robospice_oauth2;

import java.util.Set;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;

import tv.px.android.robospice.robospice_oauth2.request.OAuth2SpringAndroidSpiceRequest;
import android.content.SharedPreferences;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * A {@link SpringAndroidSpiceService} dedicated to json web services via Jackson protected with OAuth2. Provides
 * caching.
 */
public class OAuth2JacksonSpringAndroidSpiceService extends JacksonSpringAndroidSpiceService {

   private OAuth2Template oauth2Template;
   private AccessGrant currentGrant;

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
      SharedPreferences sharedPreferences = getSharedPreferences("tv.px.android.robospice.robospice_oauth2.accessGrant", MODE_MULTI_PROCESS);
      String accessToken = sharedPreferences.getString("accessToken", "");
      String scope = sharedPreferences.getString("scope", "");
      String refreshToken = sharedPreferences.getString("refreshToken", "");
      Long expiresIn = sharedPreferences.getLong("expiresIn", 0);
      
      currentGrant = new AccessGrant(accessToken, scope, refreshToken, expiresIn);
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
         // TODO set the grant for the request
      }

      // TODO if the request is for a grant, add a custom listener that will update the grant here

      super.addRequest(request, listRequestListener);
   }

   // TODO verification of hostname/certificate using either request factory or connection factory

}
