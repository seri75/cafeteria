package cafeteria;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface SaleRepository extends PagingAndSortingRepository<Sale, Long>{

	public List<Sale> findByPhoneNumberAndYyyymm(String phoneNumber, String yyyymm);

}