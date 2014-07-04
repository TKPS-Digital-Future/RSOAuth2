package tv.px.android.robospice.robospice_oauth2;

import java.util.Set;

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
      super.addRequest(request, listRequestListener);
   }

}
