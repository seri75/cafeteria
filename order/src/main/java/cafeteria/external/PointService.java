
package cafeteria.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import cafeteria.OrderCanceled;

@RequestMapping("/points")
@FeignClient(name="point", url="${feign.client.point.url}")
public interface PointService {

    @PatchMapping("/cancelPoint")
	public void cancelPoint(@RequestBody OrderCanceled orderCanceled);

}