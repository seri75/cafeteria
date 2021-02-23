package cafeteriapoint;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Point_table")
public class Point {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String phoneNumber;
    private Integer point;

    @PostPersist
    public void onPostPersist(){
        Accumulated accumulated = new Accumulated();
        BeanUtils.copyProperties(this, accumulated);
        accumulated.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        PointCanceled pointCanceled = new PointCanceled();
        BeanUtils.copyProperties(this, pointCanceled);
        pointCanceled.publishAfterCommit();


    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }




}
