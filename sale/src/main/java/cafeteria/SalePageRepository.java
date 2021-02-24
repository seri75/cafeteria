package cafeteria;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface SalePageRepository extends CrudRepository<SalePage, Long> {

    List<SalePage> findByOrderId(Long orderId);


}
