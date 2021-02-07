package cafeteria

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.Optional
import org.springframework.web.bind.annotation.RequestParam
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer


@RestController
@RequestMapping(Array("/mypages"))
class MypageController {
  
  @Autowired
  private val mypageRepository :MypageRepository = null
  
  @GetMapping(Array("/{id}"))
  def findMyPage(@PathVariable("id") id :Long):Mypage = {
    val mypage :Optional[Mypage] = mypageRepository.findById(id)
    mypage.orElse(null)
  }
  
  @GetMapping(Array("/search/findByPhoneNumber"))
  def search(@RequestParam phoneNumber :String):java.util.List[Mypage] = {
    mypageRepository.findByPhoneNumber(phoneNumber)
  }
}