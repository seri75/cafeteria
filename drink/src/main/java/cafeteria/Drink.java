package cafeteria;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

import java.util.Date;

@Entity
@Table(name="Drink")
public class Drink {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String productName;
    private Integer qty;
    private String phoneNumber;
    private String status;
    private Date createTime = new Date();

    @PostPersist
    public void onPostPersist(){
        OrderInfoReceived orderInfoReceived = new OrderInfoReceived();
        BeanUtils.copyProperties(this, orderInfoReceived);
        orderInfoReceived.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){
    	
    	switch(status) {
    	case "Receipted" : 
    		Receipted receipted = new Receipted();
            BeanUtils.copyProperties(this, receipted);
            receipted.publishAfterCommit();
            break;
    	case "Made" : 
    		Made made = new Made();
            BeanUtils.copyProperties(this, made);
            made.publishAfterCommit();
            break;
    	case "DrinkCancled" : 
    		DrinkCanceled drinkCanceled = new DrinkCanceled();
            BeanUtils.copyProperties(this, drinkCanceled);
            drinkCanceled.publishAfterCommit();
            break;
    	}
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
    
    public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public String getPhoneNumber() {
    	return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
    	this.phoneNumber = phoneNumber;
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
