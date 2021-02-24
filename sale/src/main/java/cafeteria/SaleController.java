package cafeteria;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

 @RestController
 public class SaleController {
	 
	 @Autowired
	 private SaleRepository saleRepository;
	 
	 @PatchMapping
	 public void sumAmt(Sale sale) {
		 
		 List<Sale> sales = saleRepository.findByPhoneNumberAndYyyymm(sale.getPhoneNumber(), sale.getYyyymm());
		 
		 if(sales.size() == 0) {
			 
			 saleRepository.save(sale);
		 } else {
			 sales.forEach(s -> {
				 s.setSumAmt(s.getSumAmt() + sale.getSumAmt());
				 saleRepository.save(s);
			 });
		 } 
	 }

 }
