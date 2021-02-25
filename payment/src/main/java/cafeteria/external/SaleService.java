package cafeteria.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name="sale", url="${feign.client.sale.url}")
public interface SaleService {

	@PutMapping("/sumtAmt")
	 public void sumAmt(Sale sale);
}
