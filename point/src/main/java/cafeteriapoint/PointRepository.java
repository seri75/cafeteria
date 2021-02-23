package cafeteriapoint;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface PointRepository extends PagingAndSortingRepository<Point, Long>{


	public List<Point> findByPhoneNumber(String phoneNumber);
}