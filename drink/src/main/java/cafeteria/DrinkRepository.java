package cafeteria;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface DrinkRepository extends PagingAndSortingRepository<Drink, Long>{

	List<Drink> findByOrderId(Long orderId);
	
}