package tv.px.android.robospice.robospice_oauth2;

import java.util.Set;

import tv.px.android.robospice.robospice_oauth2.request.OAuth2SpringAndroidSpiceRequest;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * A {@link SpringAndroidSpiceService} dedicated to json web services via Jackson protected with OAuth2. Provides
 * caching.
 */
public class OAuth2JacksonSpringAndroidSpiceService extends JacksonSpringAndroidSpiceService {

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
         request.getSpiceRequest().setRetryPolicy(new OAuth2RetryPolicy(this));
         // TODO set the grant for the request and the retry-policy
      }
      
      // TODO if the request is for a grant, add a custom listener that will update the grant here
      
      super.addRequest(request, listRequestListener);
   }

}
