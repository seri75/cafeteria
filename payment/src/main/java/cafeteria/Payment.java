package cafeteria;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

import cafeteria.external.Sale;
import cafeteria.external.SaleService;

@Entity
@Table(name="Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String phoneNumber;
    private Integer amt;
    private String status = "PaymentApproved";
    private Date createTime = new Date();


//    @PrePersist
//   public void onPrePersist() {
//        
//        try {
//            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        
//    }

    @PostPersist
    public void onPostPersist(){
        PaymentApproved paymentApproved = new PaymentApproved();
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();
        
        Sale sale = new Sale();
        sale.setPhoneNumber(this.phoneNumber);
        
        SimpleDateFormat yyyyMMFormat = new SimpleDateFormat("yyyyMM");
        String yyyymm = yyyyMMFormat.format(this.createTime);
        
        sale.setYyyymm(yyyymm);
        sale.setSumAmt(amt);
        
        PaymentApplication.applicationContext.getBean(SaleService.class).sumAmt(sale);
    }

    @PostUpdate
    public void onPostUpdate(){
        PaymentCanceled paymentCanceled = new PaymentCanceled();
        BeanUtils.copyProperties(this, paymentCanceled);
        paymentCanceled.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public String getPhoneNumber() {
    	return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
    	this.phoneNumber = phoneNumber;
    }
    public Integer getAmt() {
        return amt;
    }

    public void setAmt(Integer amt) {
        this.amt = amt;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
