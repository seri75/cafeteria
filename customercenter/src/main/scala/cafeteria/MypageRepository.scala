package cafeteria

import org.springframework.data.repository.CrudRepository
import org.springframework.data.jpa.repository.JpaRepository

trait MypageRepository extends JpaRepository[Mypage, Long] {
  
  def findByOrderId(orderId :Long) :java.util.List[Mypage]
  def findByPhoneNumber(phoneNumber :String) :java.util.List[Mypage]
}