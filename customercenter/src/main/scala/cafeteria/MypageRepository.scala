package cafeteria

import org.springframework.data.repository.CrudRepository

trait MypageRepository extends CrudRepository[Mypage, Long] {
  
  def findByOrderId(orderId :Long) :java.util.List[Mypage]
  def findByPhoneNumber(phoneNumber :String) :java.util.List[Mypage]
}