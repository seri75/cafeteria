
package cafeteria.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cafeteria.OrderCanceled;

@RequestMapping("/points")
@FeignClient(name="point", url="${feign.client.point.url}")
public interface PointService {

    @RequestMapping(path = "/cancelPoint", method = RequestMethod.PATCH)
	public void cancelPoint(@RequestBody OrderCanceled orderCanceled);

}
