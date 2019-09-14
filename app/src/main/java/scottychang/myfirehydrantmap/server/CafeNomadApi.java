package scottychang.myfirehydrantmap.server;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import scottychang.cafe_walker.model.FireHydrant;

public interface CafeNomadApi {
    @GET("cafes/{city}")
    Call<List<FireHydrant>> getCoffeeShops(@Path("city") String city);
}
