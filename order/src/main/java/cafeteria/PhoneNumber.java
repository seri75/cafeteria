package cafeteria;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumber {
    @Column(name = "AREACODE")
    private String areaCode;
 
    @Column(name = "LOCALNUMBER")
    private String localNumber;
    
    public String toString() {
    	return new StringBuffer(areaCode).append(localNumber).toString();
    }
}
